package com.gk.diaguide.presentation.recommendations

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.ui.Dimens
import com.gk.diaguide.core.ui.SeverityChip
import com.gk.diaguide.core.util.formatDateTime
import com.gk.diaguide.domain.model.Recommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(recommendations: List<Recommendation>) {
    var showMap by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recommendations_title)) },
                actions = {
                    IconButton(onClick = { showMap = true }) {
                        Icon(
                            imageVector = Icons.Outlined.LocalHospital,
                            contentDescription = stringResource(R.string.recommendations_open_hospitals_map),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing),
        ) {
            if (recommendations.isEmpty()) {
                item { Text(stringResource(R.string.recommendations_empty)) }
            }
            items(recommendations) { recommendation ->
                Card {
                    Column(modifier = Modifier.padding(Dimens.cardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.chipSpacing)) {
                        Text(recommendation.displayTitle(), style = MaterialTheme.typography.titleMedium)
                        Text(recommendation.displayExplanation())
                        Text(stringResource(R.string.recommendations_pattern, recommendation.relatedDetectedPattern))
                        Text(recommendation.timestamp.formatDateTime(), style = MaterialTheme.typography.bodySmall)
                        SeverityChip(recommendation.severity)
                    }
                }
            }
        }
    }

    if (showMap) {
        Dialog(
            onDismissRequest = { showMap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.recommendations_hospitals_map_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    AndroidView(
                        modifier = Modifier.weight(1f),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                loadUrl("https://yandex.ru/maps/?text=%D0%B1%D0%BE%D0%BB%D1%8C%D0%BD%D0%B8%D1%86%D0%B0%20%D1%80%D1%8F%D0%B4%D0%BE%D0%BC")
                            }
                        },
                    )
                    TextButton(onClick = { showMap = false }) {
                        Text(stringResource(R.string.recommendations_close_map))
                    }
                }
            }
        }
    }
}
