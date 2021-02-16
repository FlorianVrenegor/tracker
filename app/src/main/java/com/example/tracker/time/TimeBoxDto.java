package com.example.tracker.time;

public class TimeBoxDto {

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
}
