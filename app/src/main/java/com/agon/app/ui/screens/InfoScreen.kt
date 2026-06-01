package com.agon.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Tonality
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "How It Works",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            "Private Screen composites five visual techniques into one lightweight system overlay drawn above every app.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )

        TechniqueCard(
            Icons.Filled.Layers,
            "Privacy Overlay",
            "A FLAG_NOT_TOUCHABLE system window (TYPE_APPLICATION_OVERLAY) sits on top of all apps. Touches pass straight through so nothing is blocked.",
        )
        TechniqueCard(
            Icons.Filled.Brightness6,
            "Dynamic Dimming",
            "A radial gradient keeps the screen center bright for the direct viewer while progressively dimming outward.",
        )
        TechniqueCard(
            Icons.Filled.Contrast,
            "Directional Contrast Reduction",
            "A fine vertical micro-louver pattern lowers perceived contrast at oblique viewing angles — software mimicry of physical privacy film.",
        )
        TechniqueCard(
            Icons.Filled.Tonality,
            "Edge Darkening",
            "Strong left/right vignette bands attenuate exactly where a side-viewer's line of sight enters the panel.",
        )
        TechniqueCard(
            Icons.Filled.Bolt,
            "Adaptive Brightness Masking",
            "An optional slow shimmer subtly varies edge density over time to defeat steady off-axis adaptation by an onlooker.",
        )

        LimitationCard()
    }
}

@Composable
private fun TechniqueCard(icon: ImageVector, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.size(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                )
            }
        }
    }
}

@Composable
private fun LimitationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
        ),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.WarningAmber, null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.size(10.dp))
                Text(
                    "Honest Limitations",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "True privacy screens on Samsung/iPhone use a HARDWARE louver film bonded inside the display that physically limits the light emission angle. No app can replicate that, because the viewing angle is governed by the panel optics — outside software control.\n\n" +
                    "The best software-only approach (this app) reduces side-angle LEGIBILITY by lowering peripheral contrast and brightness. It deters casual shoulder-surfing but cannot fully black out the screen for a determined side viewer. It is also purely additive (it can only darken pixels, never re-collimate light).\n\n" +
                    "For maximum protection, combine this filter with the device's built-in brightness reduction and a physical privacy screen protector.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            )
        }
    }
}
