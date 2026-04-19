package com.gk.diaguide.presentation.history

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
import com.gk.diaguide.core.util.asArrow
import com.gk.diaguide.core.util.formatDateTime
import com.gk.diaguide.core.util.formatGlucose
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.model.GlucoseUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(records: List<CgmRecord>) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.history_title)) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (records.isEmpty()) {
                item { Text(stringResource(R.string.history_no_readings)) }
            }
            items(records) { record ->
                Card {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(record.timestamp.formatDateTime(), style = MaterialTheme.typography.titleSmall)
                        Text("${record.glucoseValue.formatGlucose(record.unit)} ${record.trendDirection.asArrow()}")
                        Text(stringResource(R.string.history_source, record.source.name))
                        if (!record.note.isNullOrBlank()) {
                            Text(stringResource(R.string.history_note, record.note!!))
                        }
                        val tags = buildList {
                            if (record.meal) add(stringResource(R.string.tag_meal))
                            if (record.insulin) add(stringResource(R.string.tag_insulin))
                            if (record.activity) add(stringResource(R.string.tag_activity))
                            if (record.sleep) add(stringResource(R.string.tag_sleep))
                            if (record.stress) add(stringResource(R.string.tag_stress))
                            if (record.symptom) add(stringResource(R.string.tag_symptom))
                        }
                        if (tags.isNotEmpty()) {
                            Text(stringResource(R.string.history_tags, tags.joinToString()))
                        }
                    }
                }
            }
        }
    }
}
