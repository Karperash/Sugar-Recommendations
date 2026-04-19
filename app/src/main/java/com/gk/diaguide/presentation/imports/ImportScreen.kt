package com.gk.diaguide.presentation.imports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gk.diaguide.R
import com.gk.diaguide.data.mock.ScenarioType

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ImportScreen(
    state: ImportUiState,
    onImportCsv: (java.io.InputStream) -> Unit,
    onImportJson: (java.io.InputStream) -> Unit,
    onLoadScenario: (ScenarioType) -> Unit,
) {
    val context = LocalContext.current
    val csvLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        uri?.let { selected ->
            context.contentResolver.openInputStream(selected)?.use(onImportCsv)
        }
    }
    val jsonLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        uri?.let { selected ->
            context.contentResolver.openInputStream(selected)?.use(onImportJson)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.import_title)) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.import_description))
                    Button(onClick = { csvLauncher.launch(arrayOf("text/*")) }) {
                        Text(stringResource(R.string.import_csv))
                    }
                    Button(onClick = { jsonLauncher.launch(arrayOf("application/json", "text/*")) }) {
                        Text(stringResource(R.string.import_json))
                    }
                }
            }

            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.import_synthetic_title))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScenarioType.entries.forEach { scenario ->
                            Button(onClick = { onLoadScenario(scenario) }) {
                                Text(scenario.name)
                            }
                        }
                    }
                }
            }

            state.message?.let {
                Text(it)
            }
        }
    }
}
