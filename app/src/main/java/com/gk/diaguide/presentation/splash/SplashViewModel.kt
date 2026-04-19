package com.gk.diaguide.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map

@HiltViewModel
class SplashViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val onboardingCompleted = settingsRepository.observeSettings()
        .map { it.onboardingCompleted && it.disclaimerAccepted }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
