package com.simonatkinson.medicationreminder.ui.medications

object MedicationListDemoData {
    val items = listOf(
        MedicationListItemUi(
            id = "metformin",
            name = "Metformin",
            dose = "500 mg",
            scheduleSummary = "Twice daily",
            daysSummary = "Every day",
            timesSummary = "8:00 AM, 8:00 PM",
            nextDose = "Next: Today 8:00 PM"
        ),
        MedicationListItemUi(
            id = "lisinopril",
            name = "Lisinopril",
            dose = "10 mg",
            scheduleSummary = "Once daily",
            daysSummary = "Every day",
            timesSummary = "8:00 AM",
            nextDose = "Next: Tomorrow 8:00 AM"
        ),
        MedicationListItemUi(
            id = "vitd",
            name = "Vitamin D3",
            dose = "2,000 IU",
            scheduleSummary = "Custom times",
            daysSummary = "Mon, Wed, Fri",
            timesSummary = "9:00 AM",
            nextDose = "Next: Wed 9:00 AM"
        ),
        MedicationListItemUi(
            id = "amoxicillin",
            name = "Amoxicillin",
            dose = "500 mg",
            scheduleSummary = "Twice daily",
            daysSummary = "Mon–Fri",
            timesSummary = "8:00 AM, 8:00 PM",
            nextDose = "Next: Today 8:00 PM"
        )
    )
}
