package com.sameh.chatme.utils

import java.text.SimpleDateFormat
import java.util.*

fun getCurrentDate(): String {
    val calendar = Calendar.getInstance()
    val myFormat = SimpleDateFormat("d, MMM HH:mm", Locale.ENGLISH)
    return myFormat.format(calendar.time)
}