package com.example.tracker.time;

import androidx.annotation.NonNull;

public class TimeBoxDto implements Comparable<TimeBoxDto> {

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

    @Override
    public int compareTo(@NonNull TimeBoxDto other) {
        long result = getTimeStartedInMilliseconds() - other.getTimeStartedInMilliseconds();
        return result < 0 ? -1 : result == 0 ? 0 : 1;
    }
}
