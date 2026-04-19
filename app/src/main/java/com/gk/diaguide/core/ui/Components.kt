package com.gk.diaguide.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gk.diaguide.domain.model.RecommendationSeverity
import com.gk.diaguide.navigation.AppDestination
import com.gk.diaguide.navigation.bottomDestinations
import com.gk.diaguide.ui.theme.Critical
import com.gk.diaguide.ui.theme.Info
import com.gk.diaguide.ui.theme.Success
import com.gk.diaguide.ui.theme.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
            content = content,
        )
    }
}

@Composable
fun SeverityChip(severity: RecommendationSeverity) {
    val color = when (severity) {
        RecommendationSeverity.INFORMATIONAL -> Info
        RecommendationSeverity.WARNING -> Warning
        RecommendationSeverity.URGENT -> Critical
    }
    AssistChip(
        onClick = {},
        label = { Text(severity.name) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = color.copy(alpha = 0.12f),
            disabledLabelColor = color,
        ),
        enabled = false,
    )
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier.background(color.copy(alpha = 0.14f), CircleShape).padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, color = color, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigate: (AppDestination) -> Unit,
) {
    NavigationBar {
        bottomDestinations.forEach { destination ->
            val label = if (destination.titleResId != 0) stringResource(destination.titleResId) else destination.title
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination) },
                icon = {},
                label = { Text(label) },
            )
        }
    }
}
