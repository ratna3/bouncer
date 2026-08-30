package com.example.bouncer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bouncer.data.ConnectedDevice
import com.example.bouncer.theme.AccentBlue
import com.example.bouncer.theme.BouncerAmber
import com.example.bouncer.theme.BouncerRed
import com.example.bouncer.theme.DarkCard
import com.example.bouncer.theme.DarkSurface
import com.example.bouncer.theme.DarkSurfaceElevated
import com.example.bouncer.theme.TextMuted
import com.example.bouncer.theme.TextPrimary
import com.example.bouncer.theme.TextSecondary
import com.example.bouncer.viewmodel.DeviceListViewModel
import com.example.bouncer.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    onChangeCredentials: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeviceListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }
    var selectedDeviceForBan by remember { mutableStateOf<ConnectedDevice?>(null) }

    // Listen for one-shot UI events (snackbars)
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bouncer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        val activeBans = uiState.devices.count { it.isBanned }
                        val statusText = if (activeBans > 0) {
                            "${uiState.devices.size} devices • $activeBans banned"
                        } else {
                            "${uiState.devices.size} devices online"
                        }
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = if (activeBans > 0) BouncerRed else TextMuted
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDevices() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Device List",
                            tint = TextPrimary
                        )
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = TextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Change Router Credentials", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                onChangeCredentials()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh Device List", color = TextPrimary) },
                            onClick = {
                                showMenu = false
                                viewModel.refreshDevices()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkSurface
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkSurface)
        ) {
            // Loading bar
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentBlue,
                    trackColor = DarkSurfaceElevated
                )
            }

            // Error banner if any
            if (uiState.errorMessage != null && !uiState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BouncerAmber.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = BouncerAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Pull to refresh box wrapping the content list
            val pullRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshDevices() },
                state = pullRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.devices.isEmpty() && !uiState.isLoading) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🛡️",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No devices detected",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ensure your phone is connected to the router's Wi-Fi network and check your router credentials.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.refreshDevices() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Try Again", color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.devices,
                            key = { it.device.macAddress }
                        ) { deviceUiModel ->
                            DeviceCard(
                                deviceUiModel = deviceUiModel,
                                isActionInProgress = uiState.actionInProgressMac == deviceUiModel.device.macAddress,
                                onPauseClick = {
                                    selectedDeviceForBan = deviceUiModel.device
                                },
                                onUnbanClick = {
                                    viewModel.unbanDevice(deviceUiModel)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Ban duration popup dialog
    selectedDeviceForBan?.let { device ->
        BanDurationDialog(
            device = device,
            onDismiss = { selectedDeviceForBan = null },
            onConfirm = { durationHours ->
                viewModel.banDevice(device, durationHours)
                selectedDeviceForBan = null
            }
        )
    }
}
