package com.gk.diaguide.presentation.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.about_title)) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.about_research_prototype))
                    Text(stringResource(R.string.about_description))
                }
            }
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.about_safety_title))
                    Text(stringResource(R.string.about_not_medical_device))
                    Text(stringResource(R.string.about_no_diagnosis))
                    Text(stringResource(R.string.about_no_insulin))
                    Text(stringResource(R.string.about_thresholds))
                    Text(stringResource(R.string.about_emergency))
                }
            }
        }
    }
}
