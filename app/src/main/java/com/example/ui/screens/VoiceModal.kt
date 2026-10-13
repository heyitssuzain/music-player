package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.AuraViewModel
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraError
import com.example.ui.theme.AuraSurfaceCard
import com.example.ui.theme.AuraSurfaceElevated
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraViolet

@Composable
fun VoiceModal(
    viewModel: AuraViewModel,
    onDismiss: () -> Unit
) {
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val transcript by viewModel.voiceManager.lastTranscript.collectAsState()
    val errorMsg by viewModel.voiceManager.errorMessage.collectAsState()

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("voice_control_modal"),
            color = AuraSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AURA Voice Control",
                        style = MaterialTheme.typography.titleLarge,
                        color = AuraCyan
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AuraTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pulsing Mic
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AuraCyan.copy(alpha = if (isListening) 0.8f else 0.2f),
                                    AuraViolet.copy(alpha = if (isListening) 0.5f else 0.1f)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            if (isListening) AuraCyan else AuraTextMuted,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isListening) viewModel.stopVoiceControl()
                            else viewModel.startVoiceControl()
                        },
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Microphone",
                            tint = if (isListening) AuraDarkBackground else AuraCyan,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isListening) "Listening... speak now" else "Tap microphone to speak",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 17.sp),
                    color = if (isListening) AuraCyan else AuraTextSecondary
                )

                if (transcript.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "\"$transcript\"",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = AuraTextPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraError,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Supported Commands Cheat Sheet
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AuraSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Supported Commands (English & Hindi):",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• \"Play\" or \"Gana bajao\"\n• \"Pause\" or \"Rok do\"\n• \"Next\" or \"Agla gana\"\n• \"Previous\" or \"Pichhla gana\"\n• \"Favorite\" or \"Pasand karo\"\n• \"Play <song name>\" or \"<song> bajao\"",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 18.sp),
                            color = AuraTextSecondary
                        )
                    }
                }
            }
        }
    }
}
