package org.satochip.satodime

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.satochip.android.NFCCardManager
import org.satochip.satodime.services.SatodimeCardListener
import org.satochip.satodime.ui.theme.SatodimeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cardManager = NFCCardManager()
        cardManager.setCardListener(SatodimeCardListener)
        cardManager.start()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.enableReaderMode(
            this,
            cardManager,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
        setContent {
            SatodimeTheme {
                Navigation()
            }
        }
    }
}