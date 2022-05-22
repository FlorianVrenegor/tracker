package com.example.tracker.weight

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class YearWeek(calendar: Calendar) {

    private val calendar: Calendar

    val dayRange: String
        get() {
            val simpleDateFormat = SimpleDateFormat(DATE_PATTERN, Locale.GERMANY)
            val firstDay = simpleDateFormat.format(calendar.time)
            calendar.add(Calendar.DATE, 6)
            val lastDay = simpleDateFormat.format(calendar.time)
            calendar.add(Calendar.DATE, -6)
            return "$firstDay - $lastDay"
        }

    val week: Int
        get() = calendar[Calendar.WEEK_OF_YEAR]

    val year: Int
        get() = calendar[Calendar.YEAR]

    fun plusWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, 1)
    }

    fun minusWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, -1)
    }

    companion object {
        private const val DATE_PATTERN = "dd.MM."

        fun now(): YearWeek {
            val calendar = Calendar.getInstance()
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.minimalDaysInFirstWeek = 4
            return YearWeek(calendar)
        }
    }

    init {
        val dayOfWeek = (calendar[Calendar.DAY_OF_WEEK] + 5) % 7
        calendar.add(Calendar.DATE, -1 * dayOfWeek)
        this.calendar = calendar
    }
}