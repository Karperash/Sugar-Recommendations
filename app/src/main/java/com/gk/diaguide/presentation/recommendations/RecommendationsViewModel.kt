package com.gk.diaguide.presentation.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gk.diaguide.domain.model.Recommendation
import com.gk.diaguide.domain.repository.CgmRepository
import com.gk.diaguide.domain.usecase.GetRecommendationHistoryUseCase
import com.gk.diaguide.domain.usecase.InsightHistoryGate
import com.gk.diaguide.domain.usecase.RefreshInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecommendationsUiState(
    val recommendations: List<Recommendation> = emptyList(),
    /** Достаточно ли истории для формирования рекомендаций (порог совпадает с [RefreshInsightsUseCase]). */
    val historySufficient: Boolean = false,
)

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    getRecommendationHistoryUseCase: GetRecommendationHistoryUseCase,
    cgmRepository: CgmRepository,
    private val refreshInsightsUseCase: RefreshInsightsUseCase,
) : ViewModel() {

    val state = combine(
        getRecommendationHistoryUseCase(),
        cgmRepository.observeAllEntries(),
    ) { recommendations, allEntries ->
        RecommendationsUiState(
            recommendations = recommendations,
            historySufficient = InsightHistoryGate.isEnoughHistory(
                allEntries,
                InsightHistoryGate.MIN_RECOMMENDATION_DAYS,
                ZoneId.systemDefault(),
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RecommendationsUiState())

    init {
        viewModelScope.launch { refreshInsightsUseCase() }
    }
}
