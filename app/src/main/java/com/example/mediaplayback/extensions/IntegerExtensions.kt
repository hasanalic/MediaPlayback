package com.example.mediaplayback.extensions

fun Int.toTimeText(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%02d:%02d".format(minutes, seconds)
}