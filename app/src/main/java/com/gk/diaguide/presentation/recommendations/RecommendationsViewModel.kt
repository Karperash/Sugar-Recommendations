package com.gk.diaguide.presentation.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.usecase.GetRecommendationHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    getRecommendationHistoryUseCase: GetRecommendationHistoryUseCase,
) : ViewModel() {
    val recommendations = getRecommendationHistoryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Recommendation>())
}
