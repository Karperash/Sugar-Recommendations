package com.gk.diaguide.data.repository

import com.gk.diaguide.domain.model.CgmRecord
import com.gk.diaguide.domain.repository.ExternalCgmGateway
import javax.inject.Inject

class ExternalCgmGatewayPlaceholder @Inject constructor() : ExternalCgmGateway {
    override suspend fun fetchFromApi(): List<CgmRecord> = emptyList()
    override suspend fun fetchFromBluetooth(): List<CgmRecord> = emptyList()
}
