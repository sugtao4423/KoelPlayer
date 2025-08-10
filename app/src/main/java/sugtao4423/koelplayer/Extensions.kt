package sugtao4423.koelplayer

fun Long.millisToTimeFormat(padMinutes: Boolean = true): String {
    val second = this / 1000
    val hour = second / 60 / 60
    val min = (second / 60 % 60).toString().let {
        if (padMinutes) it.padStart(2, '0') else it
    }
    val sec = (second % 60).toString().padStart(2, '0')

    return if (hour == 0L) "$min:$sec" else "$hour:$min:$sec"
}

fun Int.secToTimeFormat(padMinutes: Boolean = true): String =
    (this.toLong() * 1000L).millisToTimeFormat(padMinutes)
