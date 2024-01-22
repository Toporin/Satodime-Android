package org.satochip.satodime.services

import android.util.Log
import kotlinx.coroutines.delay
import org.satochip.client.SatochipCommandSet
import org.satochip.io.CardChannel
import org.satochip.io.CardListener
import org.satochip.satodime.data.NfcActionType
import org.satochip.satodime.data.NfcResultCode

private const val TAG = "SatodimeCardListener"

// deprecated, use SatodimeCardListenerForAction
//object SatodimeCardListener : CardListener {
//
//    override fun onConnected(cardChannel: CardChannel?) {
//        if (NFCCardService.actionType == NfcActionType.DoNothing) {
//            onDisconnected()
//            return
//        }
//
//        NFCCardService.isReadingFinished.postValue(false)
//        NFCCardService.isConnected.postValue(true)
//        Log.i(TAG, "Card is connected")
//        try {
//            val cmdSet = SatochipCommandSet(cardChannel)
//            NFCCardService.initialize(cmdSet)
//            NFCCardService.actionType = NfcActionType.DoNothing // todo: reset action after
//            NFCCardService.disableScanForAction()// Todo disable scanning once finished
//        } catch (e: Exception) {
//            Log.e(TAG, "An exception has been thrown during card init.")
//            Log.e(TAG, Log.getStackTraceString(e))
//        }
//    }
//
//    override fun onDisconnected() {
//        NFCCardService.isConnected.postValue(false)
//        NFCCardService.isReadingFinished.postValue(true)
//        Log.i(TAG, "Card disconnected")
//    }
//}

object SatodimeCardListenerForAction : CardListener {

    override fun onConnected(cardChannel: CardChannel?) {
//        if (NFCCardService.actionType == NfcActionType.DoNothing) { // todo: remove or check !=busy ?
//            Log.i(TAG, "SatodimeCardListenerForAction onConnected: NfcActionType.DoNothing => disconnect")
//            onDisconnected()
//            return
//        }

        NFCCardService.isListening.postValue(true)
        NFCCardService.isReadingFinished.postValue(false) // todo deprecate?
        NFCCardService.isConnected.postValue(true)
        Log.i(TAG, "SatodimeCardListenerForAction onConnected: Card is connected")
        try {
            val cmdSet = SatochipCommandSet(cardChannel)
            NFCCardService.initialize(cmdSet)

            // TODO: disconnect?
            Log.i(TAG, "SatodimeCardListenerForAction onConnected: trigger disconnection!")
            onDisconnected()
            Log.e(TAG, "SatodimeCardListenerForAction onConnected: resultAfterConnection: ${NFCCardService.resultCodeLive.value}")

            // Todo disable scanning once finished?
            // TODO: delay to let resultCodeLive update (race condition) ??
            Thread.sleep(50)
            Log.e(TAG, "SatodimeCardListenerForAction onConnected: resultAfterConnection delay: ${NFCCardService.resultCodeLive.value}")
            Thread.sleep(50)
            Log.e(TAG, "SatodimeCardListenerForAction onConnected: resultAfterConnection delay: ${NFCCardService.resultCodeLive.value}")
            if (NFCCardService.resultCodeLive.value != NfcResultCode.UnknownError) { //todo: refine condition?
                NFCCardService.disableScanForAction()
            }

        } catch (e: Exception) {
            Log.e(TAG, "SatodimeCardListenerForAction onConnected: an exception has been thrown during card init.")
            Log.e(TAG, Log.getStackTraceString(e))

            Log.i(TAG, "SatodimeCardListenerForAction onConnected: exception => trigger disconnection!")
            onDisconnected()
        }
        Log.i(TAG, "SatodimeCardListenerForAction onConnected: finished to communicate with card")
    }

    override fun onDisconnected() {
        NFCCardService.isListening.postValue(false)
        NFCCardService.isConnected.postValue(false)
        NFCCardService.isReadingFinished.postValue(true)//deprecated
        Log.i(TAG, "SatodimeCardListenerForAction onDisconnected Card disconnected!")
    }
}