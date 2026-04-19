package com.gk.diaguide.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.AppEvent
import com.gk.diaguide.domain.repository.CgmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class EventLogViewModel @Inject constructor(
    repository: CgmRepository,
) : ViewModel() {
    val events = repository.observeEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<AppEvent>())
}
