package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElectricBlue
import com.example.ui.theme.AuraSurface
import com.example.ui.theme.AuraSurfaceCard
import com.example.ui.theme.AuraSurfaceElevated
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraViolet
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onEnterAura: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Breathing and ambient particle animations
    val infiniteTransition = rememberInfiniteTransition(label = "aura_landing_transition")
    val characterBreathScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "character_breath"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
            .testTag("landing_screen")
    ) {
        // Ambient background glowing particle canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Violet halo behind character
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AuraViolet.copy(alpha = 0.22f * glowAlpha),
                        AuraCyan.copy(alpha = 0.12f * glowAlpha),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.32f),
                    radius = width * 0.7f
                )
            )

            // Animated floating ambient particles
            val particleCount = 18
            for (i in 0 until particleCount) {
                val angle = (particlePhase + i * (360f / particleCount)) * (Math.PI / 180.0)
                val radX = width * (0.2f + 0.3f * ((i * 17) % 10) / 10f)
                val radY = height * (0.15f + 0.35f * ((i * 23) % 10) / 10f)
                val x = (width * 0.5f + Math.cos(angle) * radX).toFloat()
                val y = (height * 0.32f + Math.sin(angle) * (radY * 0.5f)).toFloat()
                val particleColor = if (i % 2 == 0) AuraCyan else AuraViolet
                drawCircle(
                    color = particleColor.copy(alpha = 0.45f),
                    radius = (3f + (i % 4) * 1.5f),
                    center = Offset(x, y)
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // Header Section: Wordmark & Music-Wave Icon
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AuraViolet, AuraCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = AuraDarkBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AURA",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            ),
                            color = AuraTextPrimary
                        )
                    }

                    // Direct Skip / Enter
                    Text(
                        text = "Skip to Player",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraCyan,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, AuraCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("landing_skip_button"),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Anime Character Hero Centerpiece
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer atmospheric glow ring
                    Box(
                        modifier = Modifier
                            .size(310.dp)
                            .scale(characterBreathScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AuraViolet.copy(alpha = 0.35f * glowAlpha),
                                        AuraCyan.copy(alpha = 0.2f * glowAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Hero Character Asset Card
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = AuraSurfaceCard),
                        modifier = Modifier
                            .size(280.dp)
                            .scale(characterBreathScale)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        AuraViolet.copy(alpha = 0.8f),
                                        AuraCyan.copy(alpha = 0.8f)
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            )
                            .testTag("landing_character_art")
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.aura_anime_character),
                            contentDescription = "AURA Anime Music Protagonist",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Hero Copywriting & Actions
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR MUSIC. YOUR UNIVERSE.",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = AuraTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Discover your next obsession.",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AuraCyan,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Search, play, and organize the music you love in one immersive experience.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = AuraTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary CTA: ENTER AURA
                    Button(
                        onClick = onEnterAura,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("cta_enter_aura"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(AuraViolet, AuraElectricBlue, AuraCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = AuraDarkBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ENTER AURA",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = AuraDarkBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary CTA: EXPLORE FEATURES
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 3)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("cta_explore_features"),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AuraCyan.copy(alpha = 0.6f), AuraViolet.copy(alpha = 0.6f))
                            ),
                            width = 1.dp
                        )
                    ) {
                        Text(
                            text = "EXPLORE FEATURES",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = AuraCyan
                        )
                    }
                }
            }

            // Feature Preview Showcase
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "CORE CAPABILITIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Built for Pure Music Enjoyment",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AuraTextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verified Feature Cards
                    FeatureItemCard(
                        icon = Icons.Default.Headphones,
                        title = "YouTube Music Discovery",
                        description = "Direct online search and streaming across global tracks without sign-in."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureItemCard(
                        icon = Icons.Default.Storage,
                        title = "Local Music Library",
                        description = "Instant on-device scanning for your MP3s, FLACs, and audio files."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureItemCard(
                        icon = Icons.Default.QueueMusic,
                        title = "Smart Playlists & Queue",
                        description = "Curate custom playlists, reorder tracks, shuffle, and favorite instantly."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureItemCard(
                        icon = Icons.Default.Download,
                        title = "Offline Audio Downloads",
                        description = "Save permitted tracks to local storage for zero-connectivity listening."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureItemCard(
                        icon = Icons.Default.Mic,
                        title = "Hands-Free Voice Commands",
                        description = "Natural English & Hindi/Hinglish speech recognition without paid AI APIs."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureItemCard(
                        icon = Icons.Default.Radio,
                        title = "Native Media3 & Background Play",
                        description = "Seamless lock-screen notifications, audio focus, and low-latency audio."
                    )
                }
            }

            // Footer Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AuraSurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AuraCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AURA Music Player v1.0",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuraTextPrimary
                    )
                    Text(
                        text = "100% Privacy-First • No Login • No Tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = AuraTextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onEnterAura,
                        colors = ButtonDefaults.buttonColors(containerColor = AuraSurfaceElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Get Started Now",
                            color = AuraCyan,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureItemCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AuraSurfaceElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AuraViolet.copy(alpha = 0.25f), AuraCyan.copy(alpha = 0.25f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AuraCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = AuraTextSecondary
                )
            }
        }
    }
}

