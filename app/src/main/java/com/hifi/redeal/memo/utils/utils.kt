package com.hifi.redeal.memo.utils

import android.content.Context
import android.media.MediaPlayer

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
