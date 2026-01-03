package com.simonatkinson.medicationreminder.ui.medications

data class MedicationListItemUi(
    val id: String,
    val name: String,
    val dose: String,          // e.g., "500 mg"
    val scheduleSummary: String, // e.g., "Once daily" / "Twice daily" / "Custom"
    val daysSummary: String,     // e.g., "Every day" / "Mon–Fri"
    val timesSummary: String,    // e.g., "8:00 AM" / "8:00 AM, 8:00 PM"
    val nextDose: String? = null // e.g., "Next: Today 8:00 PM" (optional UI polish)
)
