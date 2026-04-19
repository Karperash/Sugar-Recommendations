package com.gk.diaguide.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.repository.CgmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HistoryViewModel @Inject constructor(
    repository: CgmRepository,
) : ViewModel() {
    val records = repository.observeAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<CgmRecord>())
}
