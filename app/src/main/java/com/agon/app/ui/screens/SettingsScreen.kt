package com.agon.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.agon.app.data.FilterColor
import com.agon.app.viewmodel.PrivacyViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen() {
    val vm: PrivacyViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            "Advanced Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        SectionCard(title = "Dimming") {
            SliderRow(
                label = "Dim intensity",
                arabic = "مستوى التعتيم",
                value = state.dimIntensity,
                onChange = { vm.setDim(it) },
            )
        }

        SectionCard(title = "Privacy Filter") {
            SliderRow(
                label = "Filter density",
                arabic = "كثافة الفلتر",
                value = state.filterDensity,
                onChange = { vm.setDensity(it) },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Filter color  •  ألوان الفلتر",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterColor.entries.forEach { c ->
                    ColorDot(
                        color = c.color,
                        selected = state.filterColor == c,
                        onClick = { vm.setColor(c) },
                    )
                }
            }
        }

        SectionCard(title = "Techniques") {
            ToggleRow(
                label = "Directional contrast louvers",
                arabic = "خطوط تقليل التباين الجانبي",
                checked = state.louverPattern,
                onChange = { vm.setLouver(it) },
            )
            ToggleRow(
                label = "Adaptive brightness masking",
                arabic = "إخفاء السطوع التكيفي",
                checked = state.adaptiveMasking,
                onChange = { vm.setAdaptive(it) },
            )
        }

        SectionCard(title = "Automation") {
            ToggleRow(
                label = "Auto-start on launch",
                arabic = "التشغيل التلقائي",
                checked = state.autoStart,
                onChange = { vm.setAutoStart(it) },
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SliderRow(label: String, arabic: String, value: Float, onChange: (Float) -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(
                    arabic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text(
                "${(value * 100).roundToInt()}%",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Slider(value = value, onValueChange = onChange, valueRange = 0f..1f)
    }
}

@Composable
private fun ToggleRow(label: String, arabic: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(
                arabic,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (color == Color.Black) Color(0xFF222222) else color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
