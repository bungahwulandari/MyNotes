package com.example.mynotes

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    fun format(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
        return sdf.format(Date(time))
    }
}

