package org.satochip.satodimeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.satochip.satodimeapp.ui.theme.SatodimeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SatodimeTheme {
                Navigation()
            }
        }
    }
}