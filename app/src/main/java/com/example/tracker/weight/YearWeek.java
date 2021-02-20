package com.example.tracker.weight;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class YearWeek {

    private static final String DATE_PATTERN = "dd.MM.";

    private final Calendar calendar;

    public YearWeek(Calendar calendar) {
        int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        calendar.add(Calendar.DATE, -1 * dayOfWeek);
        this.calendar = calendar;
    }

    public static YearWeek now() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setMinimalDaysInFirstWeek(4);
        return new YearWeek(calendar);
    }

    public void plusWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, 1);
    }

    public void minusWeek() {
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
    }

    public String getDayRange() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.GERMANY);
        String firstDay = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.DATE, 6);
        String lastDay = simpleDateFormat.format(calendar.getTime());
        calendar.add(Calendar.DATE, -6);
        return firstDay + " - " + lastDay;
    }

    public int getWeek() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }
}
