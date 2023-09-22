package com.hifi.redeal.memo.utils

import android.content.Context
import android.media.MediaPlayer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

fun dpToPx(context:Context, dp: Int): Int {
    val scale = context.resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}

fun getTotalDuration(mediaPlayer:MediaPlayer?):String{
    val totalDuration = mediaPlayer?.duration ?: 0

    val minutes = (totalDuration / 1000) / 60
    val seconds = (totalDuration / 1000) % 60

    return String.format("%02d:%02d", minutes, seconds)
}

fun getCurrentDuration(currentPosition:Int):String{
    val minutes = (currentPosition / 1000) / 60
    val seconds = (currentPosition / 1000) % 60

    return String.format("%02d:%02d", minutes, seconds)
}

fun intervalBetweenDateText(beforeDate: String): String {
    val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(getTime())
    val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beforeDate)

    val diffMilliseconds = nowFormat.time - beforeFormat.time
    val diffSeconds = diffMilliseconds / 1000
    val diffMinutes = diffMilliseconds / (60 * 1000)
    val diffHours = diffMilliseconds / (60 * 60 * 1000)
    val diffDays = diffMilliseconds / (24 * 60 * 60 * 1000)

    val nowCalendar = Calendar.getInstance().apply { time = nowFormat }
    val beforeCalendar = Calendar.getInstance().apply { time = beforeFormat }

    val diffYears = nowCalendar.get(Calendar.YEAR) - beforeCalendar.get(Calendar.YEAR)
    var diffMonths = diffYears * 12 + nowCalendar.get(Calendar.MONTH) - beforeCalendar.get(
        Calendar.MONTH)
    if (nowCalendar.get(Calendar.DAY_OF_MONTH) < beforeCalendar.get(Calendar.DAY_OF_MONTH)) {
        diffMonths--
    }

    if (diffYears > 0) {
        return "${diffYears}년 전"
    }
    if (diffMonths > 0) {
        return "${diffMonths}개월 전"
    }
    if (diffDays > 0) {
        return "${diffDays}일 전"
    }
    if (diffHours > 0) {
        return "${diffHours}시간 전"
    }
    if (diffMinutes > 0) {
        return "${diffMinutes}분 전"
    }
    if (diffSeconds > 0) {
        return "${diffSeconds}초 전"
    }
    if(diffSeconds > -1){
        return "방금"
    }
    return ""
}

fun getTime(): String {
    val now = System.currentTimeMillis()
    val date = Date(now)

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    return dateFormat.format(date)
}
