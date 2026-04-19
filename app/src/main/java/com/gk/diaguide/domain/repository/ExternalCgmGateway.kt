package com.gk.diaguide.domain.repository

import com.gk.diaguide.domain.model.CgmRecord

interface ExternalCgmGateway {
    suspend fun fetchFromApi(): List<CgmRecord>
    suspend fun fetchFromBluetooth(): List<CgmRecord>
}
