package com.gk.diaguide.presentation.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.core.util.formatDateTime
import com.gk.diaguide.domain.model.AppEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventLogScreen(events: List<AppEvent>) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.events_title)) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (events.isEmpty()) {
                item { Text(stringResource(R.string.events_empty)) }
            }
            items(events) { event ->
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(event.title)
                        Text(event.description)
                        Text(stringResource(R.string.events_type, event.type.name))
                        Text(event.timestamp.formatDateTime())
                    }
                }
            }
        }
    }
}
