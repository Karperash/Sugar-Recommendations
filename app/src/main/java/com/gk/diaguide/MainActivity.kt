package com.gk.diaguide

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.gk.diaguide.navigation.AppNavHost
import com.gk.diaguide.ui.theme.DiaGuideTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiaGuideTheme {
                AppNavHost()
            }
        }
    }
}
