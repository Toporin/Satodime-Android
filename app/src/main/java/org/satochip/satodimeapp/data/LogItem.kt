package org.satochip.satodimeapp.data

import java.util.Calendar
import java.util.logging.Level

data class LogItem(
    val level: Level,
    val tag: String,
    val msg: String,
    ) {

    //val date = getCurrentDateTime()
    val date = Calendar.getInstance().time

}