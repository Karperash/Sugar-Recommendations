package com.gk.diaguide.data.local

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromEpoch(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun instantToEpoch(value: Instant?): Long? = value?.toEpochMilli()
}
