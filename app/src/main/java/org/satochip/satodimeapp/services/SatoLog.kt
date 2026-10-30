package org.satochip.satodimeapp.services

import android.util.Log
import org.satochip.satodimeapp.BuildConfig.DEBUG
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import org.satochip.satodimeapp.data.LogItem

private const val TAG = "SatoLog"

// singleton
object SatoLog {

    var useVerboseLog: Boolean = false

    var logList = mutableListOf<LogItem>()

    fun setVerboseMode(useVerboseLog: Boolean){
        this.useVerboseLog = useVerboseLog
    }

    // Satochip-Java loggers (java.util.logging): 'org.satochip.client' (satochip-lib) and
    // 'org.satochip.android' (NFCCardManager) share this parent. Keep a reference so that its
    // configuration is not garbage collected.
    private val libraryLogger: Logger = Logger.getLogger("org.satochip")
    private var isLibraryHandlerSet = false

    /**
     * Routes Satochip-Java logs to SatoLog, only in debug (verbose) mode: logcat (debug builds)
     * and in-app log list. Library logs are disabled otherwise.
     *
     * Detailed library logs are at FINE level. Setting the logger level is not enough on Android:
     * the default JUL handler maps FINE to Log.DEBUG, which logcat drops by default.
     * Since Satochip-Java v0.3.6, no secret or personal data is logged at any level.
     */
    fun configureLibraryLogging() {
        if (!isLibraryHandlerSet) {
            libraryLogger.useParentHandlers = false // avoid duplicates through the default handler
            libraryLogger.addHandler(object : Handler() {
                override fun publish(record: LogRecord) {
                    val tag = record.loggerName ?: "satochip-java"
                    val msg = record.message ?: return
                    when {
                        record.level.intValue() >= Level.SEVERE.intValue() -> e(tag, msg)
                        record.level.intValue() >= Level.WARNING.intValue() -> w(tag, msg)
                        else -> d(tag, msg)
                    }
                }
                override fun flush() {}
                override fun close() {}
            })
            isLibraryHandlerSet = true
        }
        libraryLogger.level = if (useVerboseLog) Level.FINE else Level.OFF
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

//    fun i(tag: String, msg: String) {
//        if (DEBUG) {
//            Log.i(tag, msg)
//        }
//        if (useVerboseLog) {
//            this.addLog(level = Level.INFO, tag = tag, msg = msg)
//        }
//    }

    fun d(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
        if (useVerboseLog) {
            this.addLog(level = Level.CONFIG, tag = tag, msg = msg)
        }
    }

}