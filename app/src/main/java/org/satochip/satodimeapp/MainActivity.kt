package org.satochip.satodimeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.satochip.satodimeapp.ui.components.shared.SatoToast
import org.satochip.satodimeapp.ui.theme.SatoGreen
import com.google.android.play.core.review.ReviewManagerFactory
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.internetconnection.ConnectionChecker

class MainActivity : ComponentActivity() {

    private lateinit var connectionChecker: ConnectionChecker
    private fun showFeedbackDialog() {
        val reviewManager = ReviewManagerFactory.create(applicationContext)
        reviewManager.requestReviewFlow().addOnCompleteListener {
            if(it.isSuccessful) {
                reviewManager.launchReviewFlow(this, it.result)
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionChecker = ConnectionChecker(applicationContext)
        showFeedbackDialog()

        setContent {
            SatodimeTheme {
                val status by connectionChecker.observe().collectAsState(
                    initial = ConnectionChecker.InternetStatus.Available
                )
                var prevStatus by remember {
                    mutableStateOf<ConnectionChecker.InternetStatus?>(null)
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Navigation()
                    if (prevStatus == ConnectionChecker.InternetStatus.Lost && status == ConnectionChecker.InternetStatus.Available) {
                        SatoToast(
                            title = R.string.networkConnected,
                            text = R.string.networkConnectedMessage,
                            icon = R.drawable.contactless_24px,
                            iconColor = SatoGreen
                        )
                    }
                    if (status == ConnectionChecker.InternetStatus.Lost ||
                        status == ConnectionChecker.InternetStatus.Unavailable
                    ) {
                        SatoToast(
                            title = R.string.networkError,
                            text = R.string.networkErrorMessage,
                            icon = R.drawable.error_cross
                        )
                        prevStatus = status
                    }
                }
            }
        }
    }
}