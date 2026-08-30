package com.example.bouncer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bouncer.data.ConnectedDevice
import com.example.bouncer.theme.BouncerRed
import com.example.bouncer.theme.DarkCard
import com.example.bouncer.theme.DarkSurfaceElevated
import com.example.bouncer.theme.TextMuted
import com.example.bouncer.theme.TextPrimary
import com.example.bouncer.theme.TextSecondary

@Composable
fun BanDurationDialog(
    device: ConnectedDevice,
    onDismiss: () -> Unit,
    onConfirm: (durationHours: Double) -> Unit
) {
    var selectedPresetHours by remember { mutableStateOf<Double?>(1.0) }
    var customHoursText by remember { mutableStateOf("") }
    var customSelected by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val presetOptions = listOf(1.0, 2.0, 4.0, 8.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Column {
                Text(
                    text = "Pause Wi-Fi Access",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = device.name,
                    fontSize = 14.sp,
                    color = BouncerRed,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${device.ipAddress} • ${device.macAddress}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select ban duration. Access will automatically restore after this period.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Preset chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetOptions.forEach { hours ->
                        val isSelected = !customSelected && selectedPresetHours == hours
                        val label = if (hours == 1.0) "1 hr" else "${hours.toInt()} hrs"

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BouncerRed else DarkSurfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) BouncerRed else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    customSelected = false
                                    selectedPresetHours = hours
                                    inputError = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom hours section
                Text(
                    text = "Or custom duration (hours):",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = customHoursText,
                    onValueChange = {
                        customHoursText = it
                        customSelected = true
                        selectedPresetHours = null
                        inputError = null
                    },
                    placeholder = { Text("e.g. 0.5 or 12", color = TextMuted, fontSize = 14.sp) },
                    singleLine = true,
                    isError = inputError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (inputError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = inputError ?: "",
                        color = BouncerRed,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalHours = if (customSelected) {
                        val parsed = customHoursText.toDoubleOrNull()
                        if (parsed == null || parsed <= 0) {
                            inputError = "Enter a valid positive number of hours"
                            return@Button
                        }
                        if (parsed > 168) { // max 1 week
                            inputError = "Duration cannot exceed 168 hours (1 week)"
                            return@Button
                        }
                        parsed
                    } else {
                        selectedPresetHours ?: 1.0
                    }
                    onConfirm(finalHours)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BouncerRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ban Device", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
