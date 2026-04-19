package com.gk.diaguide.presentation.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.SeverityChip
import com.gk.diaguide.core.util.formatDateTime
import com.gk.diaguide.domain.model.Recommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(recommendations: List<Recommendation>) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.recommendations_title)) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (recommendations.isEmpty()) {
                item { Text(stringResource(R.string.recommendations_empty)) }
            }
            items(recommendations) { recommendation ->
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(recommendation.title, style = MaterialTheme.typography.titleMedium)
                        Text(recommendation.shortExplanation)
                        Text(stringResource(R.string.recommendations_pattern, recommendation.relatedDetectedPattern))
                        Text(recommendation.timestamp.formatDateTime(), style = MaterialTheme.typography.bodySmall)
                        SeverityChip(recommendation.severity)
                    }
                }
            }
        }
    }
}
