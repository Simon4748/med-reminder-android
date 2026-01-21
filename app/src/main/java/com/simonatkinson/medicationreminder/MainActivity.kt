package com.simonatkinson.medicationreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.simonatkinson.medicationreminder.ui.theme.MedicationReminderTheme
import com.simonatkinson.medicationreminder.ui.medications.MedicationListScreen
import com.simonatkinson.medicationreminder.ui.navigation.AppNav
import com.simonatkinson.medicationreminder.ui.notifications.NotificationChannels
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationChannels.ensureCreated(this)
        setContent {
            MedicationReminderTheme {
                MedicationReminderTheme {
                    AppNav()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MedicationReminderTheme {
        Greeting("Android")
    }
}
