package com.example.bouncer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bouncer.data.AOT4221NKRepository
import com.example.bouncer.data.BanRecord
import com.example.bouncer.data.ConnectedDevice
import com.example.bouncer.data.RouterRepository
import com.example.bouncer.data.local.BouncerDatabase
import com.example.bouncer.data.local.CredentialStore
import com.example.bouncer.worker.UnbanWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

data class DeviceUiModel(
    val device: ConnectedDevice,
    val banRecord: BanRecord? = null
) {
    val isBanned: Boolean get() = banRecord != null
}

data class DeviceListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val devices: List<DeviceUiModel> = emptyList(),
    val errorMessage: String? = null,
    val actionInProgressMac: String? = null
)

class DeviceListViewModel(application: Application) : AndroidViewModel(application) {

    private val credentialStore = CredentialStore(application)
    private val database = BouncerDatabase.getInstance(application)
    private val banDao = database.banRecordDao()
    private val workManager = WorkManager.getInstance(application)

    private val repository: RouterRepository = AOT4221NKRepository(credentialStore.getRouterIp())

    private val _rawDevices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _actionInProgressMac = MutableStateFlow<String?>(null)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // 1. Combine raw discovered devices with active ban records from Room
    private val deviceUiModelsFlow: Flow<List<DeviceUiModel>> = combine(
        _rawDevices,
        banDao.getAllBanRecords()
    ) { rawDevices: List<ConnectedDevice>, banRecords: List<BanRecord> ->
        val banMap = banRecords.associateBy { it.macAddress.lowercase() }
        val knownMacs = mutableSetOf<String>()
        val deviceUiModels = mutableListOf<DeviceUiModel>()

        for (device in rawDevices) {
            val mac = device.macAddress.lowercase()
            knownMacs.add(mac)
            deviceUiModels.add(
                DeviceUiModel(
                    device = device,
                    banRecord = banMap[mac]
                )
            )
        }

        // Include banned devices that may no longer appear in the DHCP table
        for (ban in banRecords) {
            val mac = ban.macAddress.lowercase()
            if (mac !in knownMacs) {
                deviceUiModels.add(
                    DeviceUiModel(
                        device = ConnectedDevice(
                            name = ban.deviceName,
                            ipAddress = "Offline (Banned)",
                            macAddress = ban.macAddress
                        ),
                        banRecord = ban
                    )
                )
            }
        }
        deviceUiModels
    }

    // 2. Combine with UI status flags (up to 5 flows)
    val uiState: StateFlow<DeviceListUiState> = combine(
        deviceUiModelsFlow,
        _isLoading,
        _isRefreshing,
        _errorMessage,
        _actionInProgressMac
    ) { devices: List<DeviceUiModel>, loading: Boolean, refreshing: Boolean, error: String?, actionMac: String? ->
        DeviceListUiState(
            isLoading = loading,
            isRefreshing = refreshing,
            devices = devices,
            errorMessage = error,
            actionInProgressMac = actionMac
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DeviceListUiState(isLoading = true)
    )

    init {
        // Clean up expired bans in DB on launch
        viewModelScope.launch(Dispatchers.IO) {
            banDao.deleteExpired(System.currentTimeMillis())
        }
        if (credentialStore.hasCredentials()) {
            fetchDevices(isRefresh = false)
        }
    }

    fun refreshDevices() {
        fetchDevices(isRefresh = true)
    }

    fun fetchDevices(isRefresh: Boolean = false) {
        val username = credentialStore.getUsername() ?: return
        val password = credentialStore.getPassword() ?: return

        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true else _isLoading.value = true
            _errorMessage.value = null

            try {
                val loginSuccess = repository.login(username, password)
                if (!loginSuccess) {
                    _errorMessage.value = "Couldn't authenticate with router. Check your credentials and Wi-Fi connection."
                    _eventFlow.emit(UiEvent.ShowSnackbar("Router login failed. Check connection to router."))
                } else {
                    val devices = repository.getConnectedDevices()
                    _rawDevices.value = devices
                    if (devices.isEmpty()) {
                        _errorMessage.value = "No connected devices found or failed to parse router table."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to connect to router: ${e.localizedMessage ?: "Unknown error"}"
                _eventFlow.emit(UiEvent.ShowSnackbar("Network error: Could not reach router."))
            } finally {
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
    }

    fun banDevice(device: ConnectedDevice, durationHours: Double) {
        val username = credentialStore.getUsername() ?: return
        val password = credentialStore.getPassword() ?: return
        val routerIp = credentialStore.getRouterIp()
        val mac = device.macAddress

        if (_actionInProgressMac.value == mac) return // Debounce

        viewModelScope.launch {
            _actionInProgressMac.value = mac
            try {
                val success = repository.setMacBanStatus(mac, ban = true)
                if (success) {
                    val durationMillis = (durationHours * 3600 * 1000).toLong()
                    val now = System.currentTimeMillis()
                    val unbanAt = now + durationMillis

                    // Create scheduled unban WorkRequest
                    val inputData = Data.Builder()
                        .putString(UnbanWorker.KEY_TARGET_MAC, mac)
                        .putString(UnbanWorker.KEY_USERNAME, username)
                        .putString(UnbanWorker.KEY_PASSWORD, password)
                        .putString(UnbanWorker.KEY_BASE_URL, routerIp)
                        .build()

                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<UnbanWorker>()
                        .setInputData(inputData)
                        .setInitialDelay(durationMillis, TimeUnit.MILLISECONDS)
                        .setConstraints(constraints)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                        .build()

                    workManager.enqueue(workRequest)

                    // Persist ban in Room
                    val banRecord = BanRecord(
                        macAddress = mac,
                        deviceName = device.name,
                        bannedAt = now,
                        unbanAt = unbanAt,
                        workRequestId = workRequest.id.toString()
                    )
                    banDao.insert(banRecord)

                    _eventFlow.emit(UiEvent.ShowSnackbar("Banned ${device.name} for ${formatDuration(durationHours)}"))
                } else {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Failed to ban device on router."))
                }
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error applying ban: ${e.localizedMessage}"))
            } finally {
                _actionInProgressMac.value = null
            }
        }
    }

    fun unbanDevice(deviceUiModel: DeviceUiModel) {
        val mac = deviceUiModel.device.macAddress
        if (_actionInProgressMac.value == mac) return

        viewModelScope.launch {
            _actionInProgressMac.value = mac
            try {
                // Cancel scheduled background worker if exists
                deviceUiModel.banRecord?.workRequestId?.let { idStr ->
                    try {
                        workManager.cancelWorkById(UUID.fromString(idStr))
                    } catch (_: Exception) {}
                }

                val success = repository.setMacBanStatus(mac, ban = false)
                if (success) {
                    banDao.deleteByMac(mac)
                    _eventFlow.emit(UiEvent.ShowSnackbar("Unbanned ${deviceUiModel.device.name}"))
                    fetchDevices(isRefresh = true)
                } else {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Failed to unban device on router."))
                }
            } catch (e: Exception) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Error unbanning device: ${e.localizedMessage}"))
            } finally {
                _actionInProgressMac.value = null
            }
        }
    }

    private fun formatDuration(hours: Double): String {
        return if (hours == hours.toInt().toDouble()) {
            val h = hours.toInt()
            if (h == 1) "1 hour" else "$h hours"
        } else {
            val totalMinutes = (hours * 60).toInt()
            val h = totalMinutes / 60
            val m = totalMinutes % 60
            if (h > 0) "${h}h ${m}m" else "${m}m"
        }
    }
}
