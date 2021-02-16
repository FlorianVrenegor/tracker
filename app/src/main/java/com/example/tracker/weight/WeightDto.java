package com.example.tracker.weight;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

class WeightDto implements Comparable<WeightDto> {

    private final static String DATE_PATTERN = "dd.MM, HH:mm"; // "yyyy-MM-dd HH:mm"

    private final long timeInMillis;
    private final int week;
    private final int month;
    private final int dayInMonth;
    private final int year;
    private final String date;
    private final double weightInKgs;

    public WeightDto(long timeInMillis, double weightInKgs) {
        this.timeInMillis = timeInMillis;

        Date date = new Date(timeInMillis);
        this.date = new SimpleDateFormat(DATE_PATTERN, Locale.GERMANY).format(date);

        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        calendar.setTime(date);
        this.week = calendar.get(Calendar.WEEK_OF_YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.dayInMonth = calendar.get(Calendar.DAY_OF_MONTH);
        this.year = calendar.get(Calendar.YEAR);

        this.weightInKgs = weightInKgs;
    }

    public int getWeek() {
        return week;
    }

    public int getMonth() {
        return month;
    }

    public int getDayInMonth() {
        return dayInMonth;
    }

    public int getYear() {
        return year;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public String getDate() {
        return date;
    }

    public double getWeightInKgs() {
        return weightInKgs;
    }

    public String getWeightString() {
        return weightInKgs + " kg";
    }

    @Override
    public int compareTo(WeightDto o) {
        if (o == null) {
            return 0;
        }
        long result = getTimeInMillis() - o.getTimeInMillis();
        return result < 0 ? -1 : result == 0 ? 0 : 1;
    }
}