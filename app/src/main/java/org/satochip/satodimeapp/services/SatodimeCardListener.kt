package org.satochip.satodimeapp.services

import android.util.Log
import org.satochip.client.SatochipCommandSet
import org.satochip.io.CardChannel
import org.satochip.io.CardListener
import org.satochip.satodimeapp.data.NfcResultCode

private const val TAG = "SatodimeCardListener"

object SatodimeCardListenerForAction : CardListener {

    override fun onConnected(cardChannel: CardChannel?) {

        NFCCardService.isConnected.postValue(true)
        SatoLog.d(TAG, "onConnected: Card is connected")
        try {
            val cmdSet = SatochipCommandSet(cardChannel)
            // start to interact with card
            NFCCardService.initialize(cmdSet)

            // TODO: disconnect?
            SatoLog.d(TAG, "onConnected: trigger disconnection!")
            onDisconnected()
            SatoLog.d(TAG, "onConnected: resultAfterConnection: ${NFCCardService.resultCodeLive.value}")

            // disable scanning once finished
            Thread.sleep(100) // delay to let resultCodeLive update (avoid race condition?)
            SatoLog.d(TAG, "onConnected: resultAfterConnection delay: ${NFCCardService.resultCodeLive.value}")
            if (NFCCardService.resultCodeLive.value != NfcResultCode.UnknownError) { //todo: refine condition?
                // if result is OK, or failed with an known reason, we stop polling for the card
                NFCCardService.disableScanForAction()
            }

        } catch (e: Exception) {
            SatoLog.e(TAG, "onConnected: an exception has been thrown during card init.")
            SatoLog.e(TAG, Log.getStackTraceString(e))
            onDisconnected()
        }
    }

    override fun onDisconnected() {
        NFCCardService.isConnected.postValue(false)
        SatoLog.d(TAG, "onDisconnected: Card disconnected!")
    }
}