package com.example.tracker.time;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeBoxDto implements Comparable<TimeBoxDto> {

    private final static String DATE_PATTERN = "dd.MM HH:mm";

    private final long timeStartedInMilliseconds;
    private final long durationInMilliseconds;
    private final String description;

    public TimeBoxDto(long timeStartedInMilliseconds, long durationInMilliseconds, String description) {
        this.timeStartedInMilliseconds = timeStartedInMilliseconds;
        this.durationInMilliseconds = durationInMilliseconds;
        this.description = description;
    }

    public long getTimeStartedInMilliseconds() {
        return timeStartedInMilliseconds;
    }

    public long getDurationInMilliseconds() {
        return durationInMilliseconds;
    }

    public String getDescription() {
        return description;
    }

    public String getTimeStartedFormatted() {
        Date date = new Date(timeStartedInMilliseconds);
        return new SimpleDateFormat(DATE_PATTERN, Locale.GERMANY).format(date);
    }

    public String getDurationFormatted() {
        long durationInSeconds = durationInMilliseconds / 1000 % 60;
        long durationInMinutes = durationInMilliseconds / 60 / 1000;
        return durationInMinutes + "m " + (durationInSeconds == 0L ? "" : durationInSeconds + "s"); // Extracted to avoid warning
    }

    @Override
    public int compareTo(@NonNull TimeBoxDto other) {
        long result = getTimeStartedInMilliseconds() - other.getTimeStartedInMilliseconds();
        return result < 0 ? -1 : result == 0 ? 0 : 1;
    }
}
