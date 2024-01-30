package org.satochip.satodime.services

import android.util.Log
import org.satochip.client.SatochipCommandSet
import org.satochip.io.CardChannel
import org.satochip.io.CardListener
import org.satochip.satodime.data.NfcResultCode

private const val TAG = "SatodimeCardListener"

object SatodimeCardListenerForAction : CardListener {

    override fun onConnected(cardChannel: CardChannel?) {

        //NFCCardService.isListening.postValue(true)
        //NFCCardService.isReadingFinished.postValue(false) // todo deprecate?
        NFCCardService.isConnected.postValue(true)
        Log.d(TAG, "SatodimeCardListenerForAction onConnected: Card is connected")
        try {
            val cmdSet = SatochipCommandSet(cardChannel)
            // start to interact with card
            NFCCardService.initialize(cmdSet)

            // TODO: disconnect?
            Log.v(TAG, "SatodimeCardListenerForAction onConnected: trigger disconnection!")
            onDisconnected()
            Log.v(TAG, "SatodimeCardListenerForAction onConnected: resultAfterConnection: ${NFCCardService.resultCodeLive.value}")

            // disable scanning once finished
            Thread.sleep(100) // delay to let resultCodeLive update (avoid race condition?)
            Log.v(TAG, "SatodimeCardListenerForAction onConnected: resultAfterConnection delay: ${NFCCardService.resultCodeLive.value}")
            if (NFCCardService.resultCodeLive.value != NfcResultCode.UnknownError) { //todo: refine condition?
                // if result is OK, or failed with an known reason, we stop polling for the card
                NFCCardService.disableScanForAction()
            }

        } catch (e: Exception) {
            Log.e(TAG, "SatodimeCardListenerForAction onConnected: an exception has been thrown during card init.")
            Log.e(TAG, Log.getStackTraceString(e))
            Log.e(TAG, "SatodimeCardListenerForAction onConnected: exception => trigger disconnection!")
            onDisconnected()
        }
        Log.d(TAG, "SatodimeCardListenerForAction onConnected: finished to communicate with card")
    }

    override fun onDisconnected() {
        NFCCardService.isConnected.postValue(false)
        Log.d(TAG, "SatodimeCardListenerForAction onDisconnected Card disconnected!")
    }
}