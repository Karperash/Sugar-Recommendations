package com.gk.diaguide.domain.model

enum class GlucoseUnit {
    MG_DL,
    MMOL_L,
}

enum class TrendDirection {
    DOUBLE_UP,
    UP,
    FLAT,
    DOWN,
    DOUBLE_DOWN,
    UNKNOWN,
}

enum class EntrySource {
    MANUAL,
    CSV_IMPORT,
    JSON_IMPORT,
    MOCK,
    API_PLACEHOLDER,
    BLUETOOTH_PLACEHOLDER,
}

enum class RecommendationSeverity {
    INFORMATIONAL,
    WARNING,
    URGENT,
}

enum class PatternType {
    LOW_VALUE,
    HIGH_VALUE,
    RAPID_RISE,
    RAPID_FALL,
    PROLONGED_OUT_OF_RANGE,
    REPEATED_DEVIATIONS,
    MORNING_HIGHS,
    POST_MEAL_SPIKE,
    NIGHT_LOW,
    CRITICAL_HIGH,
    CRITICAL_LOW,
}

enum class EventType {
    NOTE,
    MEAL,
    INSULIN,
    ACTIVITY,
    SLEEP,
    STRESS,
    SYMPTOM,
    IMPORT,
}
