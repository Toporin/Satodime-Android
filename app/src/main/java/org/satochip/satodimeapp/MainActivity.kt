package org.satochip.satodimeapp

import UnlockCodeDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.play.core.review.ReviewManagerFactory
import org.satochip.satodimeapp.ui.components.shared.SatoToast
import org.satochip.satodimeapp.ui.components.shared.NfcCheckDialog
import org.satochip.satodimeapp.ui.theme.SatoGreen
import org.satochip.satodimeapp.ui.theme.SatodimeTheme
import org.satochip.satodimeapp.util.internetconnection.ConnectionChecker
import org.satochip.satodimeapp.viewmodels.SharedViewModel
import org.satochip.satodimeapp.services.SatoLog
import androidx.lifecycle.viewmodel.compose.viewModel
import org.satochip.satodimeapp.services.NFCCardService

private const val TAG = "MainActivity"

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
        // Draw behind system bars on all API levels (enforced from API 35).
        // System bar icons follow the system dark mode, like SatodimeTheme.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        connectionChecker = ConnectionChecker(applicationContext)
        showFeedbackDialog()

        setContent {
            SatodimeTheme {
                val sharedViewModel: SharedViewModel = viewModel()
                val status by connectionChecker.observe().collectAsState(
                    initial = ConnectionChecker.InternetStatus.Available
                )
                var prevStatus by remember {
                    mutableStateOf<ConnectionChecker.InternetStatus?>(null)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.primaryVariant),
                ) {
                    // Keep content clear of status/navigation bars, display cutout and keyboard,
                    // while the background above stays full screen.
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                    ) {
                        Navigation()
                    }
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
                    
                    // NFC Dialog
                    val showNfcCheckDialog by sharedViewModel.showNfcCheckDialog.collectAsState()
                    if (showNfcCheckDialog) {
                        NfcCheckDialog(
                            onDismiss = { sharedViewModel.dismissNfcDialog() },
                            onOpenSettings = {
                                // Open NFC settings
                                val intent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                                startActivity(intent)
                                sharedViewModel.dismissNfcDialog()
                            }
                        )
                    }

                    // Unlock Code Dialog
                    val showUnlockCodeDialog by sharedViewModel.showUnlockCodeDialog.collectAsState()
                    if (showUnlockCodeDialog) {
                        UnlockCodeDialog(
                            onDismiss = { sharedViewModel.dismissUnlockCodeDialog() },
                            onCodeEntered = { code ->
                                // Handle the entered code here (never log it: it is the card unlock code)
                                // convert to bytes
                                val unlockCodeBytes = code.toByteArray()

                                // pad/truncate with '00' bytes to reach 20 bytes
                                val paddedUnlockCodeBytes = unlockCodeBytes.copyOf(20)

                                // check validity, then update code
                                NFCCardService.updateUnlockCode(paddedUnlockCodeBytes)

                                // do something if code is invalid?

                                // check code validity
                                // TODO!
                                // if valid, update cached value
                                // TODO!
                                // if valid, update preferences
                                // TODO!


                                sharedViewModel.dismissUnlockCodeDialog()
                            }
                        )
                    }


                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // keep NFC reader mode enabled while the app is in the foreground, so that tapping a card
        // does not launch other NFC apps or the browser (tags are ignored when no action is pending)
        NFCCardService.enableReaderMode(this)
    }

    override fun onPause() {
        super.onPause()
        NFCCardService.disableReaderMode(this)
    }
}
