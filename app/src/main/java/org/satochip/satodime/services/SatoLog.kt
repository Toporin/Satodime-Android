package org.satochip.satodime.services

import android.util.Log
import org.satochip.satodime.BuildConfig.DEBUG
import java.util.logging.Level
import org.satochip.satodime.data.LogItem

private const val TAG = "SatoLog"

// singleton
object SatoLog {

    var useVerboseLog: Boolean = false

    var logList = mutableListOf<LogItem>()

    fun setVerboseMode(useVerboseLog: Boolean){
        this.useVerboseLog = useVerboseLog
    }

    fun addLog(level: Level, tag: String = "", msg: String) {
        val log = LogItem(level= level, tag= tag, msg= msg)
        logList.add(log)
    }

    fun e(tag: String, msg: String) {
        if (DEBUG) {
            Log.e(tag, msg)
        }
        this.addLog(level= Level.SEVERE, tag= tag, msg= msg)
    }

    fun w(tag: String, msg: String) {
        if (DEBUG) {
            Log.w(tag, msg)
        }
        this.addLog(level= Level.WARNING, tag= tag, msg= msg)
    }

    fun i(tag: String, msg: String) {
        if (DEBUG) {
            Log.i(tag, msg)
        }
        if (useVerboseLog) {
            this.addLog(level = Level.INFO, tag = tag, msg = msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
        if (useVerboseLog) {
            this.addLog(level = Level.CONFIG, tag = tag, msg = msg)
        }
    }

}