package com.example.tracker.weight

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeightDto(val timeInMillis: Long, weightInKgs: Double) : Comparable<WeightDto> {

    val week: Int
    val month: Int
    val dayInMonth: Int
    val year: Int
    val date: String
    val weightInKgs: Double

    // calculates dayOf Week, monday = 0, sunday = 6
    val dayOfWeek: Int
        get() {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            // because for some reason monday, the first day of the week, gets a 2, saturday is 7
            return (calendar[Calendar.DAY_OF_WEEK] + 5) % 7
        }

    val weightString: String
        get() = "$weightInKgs kg"

    override fun compareTo(other: WeightDto): Int {
        val result = timeInMillis - other.timeInMillis
        return if (result < 0) -1 else if (result == 0L) 0 else 1
    }

    companion object {
        private const val DATE_PATTERN = "dd.MM HH:mm" // "yyyy-MM-dd HH:mm"
    }

    init {
        val date = Date(timeInMillis)
        this.date = SimpleDateFormat(DATE_PATTERN, Locale.GERMANY).format(date)
        val calendar = Calendar.getInstance(Locale.GERMANY)
        calendar.time = date
        week = calendar[Calendar.WEEK_OF_YEAR]
        month = calendar[Calendar.MONTH] + 1
        dayInMonth = calendar[Calendar.DAY_OF_MONTH]
        year = calendar[Calendar.YEAR]
        this.weightInKgs = weightInKgs
    }
}