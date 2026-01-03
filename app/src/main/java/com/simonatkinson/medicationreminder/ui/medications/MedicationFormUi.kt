package com.simonatkinson.medicationreminder.ui.medications

enum class FrequencyUi {
    ONCE_DAILY,
    TWICE_DAILY,
    CUSTOM
}

data class MedicationFormUi(
    val name: String = "",
    val doseAmount: String = "",
    val doseUnit: String = "mg",
    val notes: String = "",
    val frequency: FrequencyUi = FrequencyUi.ONCE_DAILY,
    val repeatEveryDay: Boolean = true,
    val selectedDays: Set<String> = emptySet(),
    val times: List<String> = listOf("8:00 AM")
)
