package com.agon.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agon.app.data.PrivacyLevel
import com.agon.app.ui.components.PrivacyPreview
import com.agon.app.viewmodel.PrivacyViewModel

@Composable
fun HomeScreen() {
    val vm: PrivacyViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasOverlay by remember { mutableStateOf(vm.hasOverlayPermission()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        HeaderBlock()

        if (!hasOverlay) {
            PermissionCard(onGrant = {
                openOverlaySettings(context)
            }, onRefresh = { hasOverlay = vm.hasOverlayPermission() })
        }

        // Master toggle hero card
        val animatedScale by animateFloatAsState(
            targetValue = if (state.enabled) 1f else 0.95f,
            animationSpec = tween(300),
            label = "scale",
        )
        val cardColor by animateColorAsState(
            targetValue = if (state.enabled) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            animationSpec = tween(300),
            label = "card",
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
        ) {
            Column(Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = if (state.enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp),
                    )
                    Spacer(Modifier.size(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (state.enabled) "Protection Active" else "Protection Off",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (state.enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            if (state.enabled) "Side viewing is being blurred"
                            else "Tap the switch to start",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.enabled) Color.White.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { want ->
                            if (want && !vm.hasOverlayPermission()) {
                                openOverlaySettings(context)
                            } else {
                                vm.toggle(want)
                            }
                            hasOverlay = vm.hasOverlayPermission()
                        },
                    )
                }
            }
        }

        // Live preview
        Text(
            "Live Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.7f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2F45)),
        ) {
            Box(Modifier.fillMaxSize()) {
                // Simulated screen content under the filter
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(5) { i ->
                        Box(
                            Modifier
                                .fillMaxWidth(if (i % 2 == 0) 0.9f else 0.6f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.55f)),
                        )
                    }
                }
                PrivacyPreview(
                    settings = state.copy(enabled = true),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Level selector
        Text(
            "Protection Strength",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PrivacyLevel.entries.forEach { level ->
                LevelChip(
                    level = level,
                    selected = state.level == level,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.setLevel(level) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HeaderBlock() {
    Column {
        Text(
            "Private Screen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Reduce what others see from the side",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit, onRefresh: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.size(10.dp))
                Text(
                    "Overlay permission required",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "To draw the privacy filter on top of all apps, allow \"Display over other apps\".",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onGrant) { Text("Grant permission") }
                FilledTonalButton(onClick = onRefresh) { Text("I've granted it") }
            }
        }
    }
}

@Composable
private fun LevelChip(
    level: PrivacyLevel,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
        label = "chipbg",
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bg,
        onClick = onClick,
    ) {
        Column(
            Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(visible = selected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                level.label,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                level.arabic,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Color.White.copy(alpha = 0.85f)
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

private fun openOverlaySettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
