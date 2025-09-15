package org.satochip.satodimeapp

import android.app.PendingIntent
import android.content.IntentFilter
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFiltersArray: Array<IntentFilter>
    private lateinit var techListsArray: Array<Array<String>>

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
                }
            }
        }

        // intercept NDEF tag
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            //SatoLog.e(TAG, "NFC not supported on this device")
            return
        }

        // PendingIntent for NFC intents
        pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        // Intent filters for NDEF and TECH
        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            addDataScheme("http")
            addDataScheme("https")
            addDataAuthority("satodime.io", null)
            addDataAuthority("*.satodime.io", null)
        }
        val techFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        intentFiltersArray = arrayOf(ndefFilter, techFilter)

        // Tech list for IsoDep
        techListsArray = arrayOf(arrayOf(IsoDep::class.java.name))

    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
        // If you want to start scanning immediately, call scanCardForAction here
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        // Ensure reader mode is disabled to avoid conflicts
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                // Handle tag (e.g., for APDU or ignore NDEF)
                handleNfcTag(tag)
            }
        }
    }

    private fun handleNfcTag(tag: Tag) {
        SatoLog.d(TAG, "Tag detected: ${tag.techList.joinToString()}")
        // Ignore NDEF tags entirely
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action) {
            SatoLog.d(TAG, "Ignoring NDEF tag to prevent browser launch")
            return
        }

//        // Handle IsoDep for APDU (if needed outside reader mode
//        // Disabled currently, as it interferes with nfc scan for action
//        Thread {
//            try {
//                val isoDep = IsoDep.get(tag)
//                if (isoDep != null) {
//                    isoDep.connect()
//                    val cardManager = NFCCardManager() // force loading of BouncyCastle, avoid
//                    val cardChannel = NFCCardChannel(isoDep)
//                    NFCCardService.isConnected.postValue(true)
//                    SatoLog.d(TAG, "handleNfcTag: Card is connected")
//                    val cmdSet = SatochipCommandSet(cardChannel)
//                    // start to interact with card
//                    SatoLog.d(TAG, "handleNfcTag: before initialize()")
//                    NFCCardService.actionType = NfcActionType.ScanCard
//                    NFCCardService.initialize(cmdSet)
//                    SatoLog.d(TAG, "handleNfcTag: after initialize()")
//                } else {
//                    NFCCardService.resultCodeLive.postValue(NfcResultCode.UnknownError)
//                    SatoLog.e(TAG, "Tag is not IsoDep")
//                }
//            } catch (e: Exception) {
//                NFCCardService.resultCodeLive.postValue(NfcResultCode.UnknownError)
//                SatoLog.e(TAG, "Error handling tag: $e")
//                SatoLog.e(TAG, Log.getStackTraceString(e))
//            }
//        }.start()
    }

}