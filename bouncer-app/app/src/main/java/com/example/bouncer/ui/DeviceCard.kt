package com.example.bouncer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bouncer.theme.BouncerAmber
import com.example.bouncer.theme.BouncerGreen
import com.example.bouncer.theme.BouncerRed
import com.example.bouncer.theme.DarkCard
import com.example.bouncer.theme.DarkSurfaceElevated
import com.example.bouncer.theme.TextMuted
import com.example.bouncer.theme.TextPrimary
import com.example.bouncer.theme.TextSecondary
import com.example.bouncer.viewmodel.DeviceUiModel
import kotlinx.coroutines.delay

@Composable
fun DeviceCard(
    deviceUiModel: DeviceUiModel,
    isActionInProgress: Boolean,
    onPauseClick: () -> Unit,
    onUnbanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBanned = deviceUiModel.isBanned
    val banRecord = deviceUiModel.banRecord

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live countdown update every second for banned devices
    if (isBanned) {
        LaunchedEffect(banRecord?.unbanAt) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    val remainingMillis = remember(banRecord?.unbanAt, currentTime) {
        if (banRecord != null) {
            (banRecord.unbanAt - currentTime).coerceAtLeast(0L)
        } else {
            0L
        }
    }

    val remainingText = remember(remainingMillis) {
        if (remainingMillis <= 0) {
            "Expiring soon..."
        } else {
            val totalSec = remainingMillis / 1000
            val hours = totalSec / 3600
            val minutes = (totalSec % 3600) / 60
            val seconds = totalSec % 60
            if (hours > 0) {
                "${hours}h ${minutes}m left"
            } else if (minutes > 0) {
                "${minutes}m ${seconds}s left"
            } else {
                "${seconds}s left"
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isBanned) {
                    Modifier.border(1.dp, BouncerRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                } else {
                    Modifier.border(1.dp, DarkSurfaceElevated, RoundedCornerShape(16.dp))
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Status Chip + Device Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isBanned) BouncerRed else BouncerGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = deviceUiModel.device.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status Badge
                if (isBanned) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BouncerRed.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Blocked • $remainingText",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BouncerRed
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BouncerGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BouncerGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details: IP and MAC address
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "IP: ${deviceUiModel.device.ipAddress}",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MAC: ${deviceUiModel.device.macAddress}",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Action Button
                if (isActionInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = if (isBanned) BouncerGreen else BouncerRed
                    )
                } else if (isBanned) {
                    Button(
                        onClick = onUnbanClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BouncerGreen
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Unban Now",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    Button(
                        onClick = onPauseClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BouncerRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Pause",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
