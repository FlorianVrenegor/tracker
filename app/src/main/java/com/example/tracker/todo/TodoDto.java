package com.example.tracker.todo;

public class TodoDto {

    private final long timeCreatedInMilliseconds;
    private final long timeDoneInMilliseconds;
    private boolean done;
    private String description;

    public TodoDto(String description) {
        this(System.currentTimeMillis(), -1, false, description);
    }

    public TodoDto(long timeCreatedInMilliseconds, long timeDoneInMilliseconds, boolean done, String description) {
        this.timeCreatedInMilliseconds = timeCreatedInMilliseconds;
        this.timeDoneInMilliseconds = timeDoneInMilliseconds;
        this.done = done;
        this.description = description;
    }

    public long getTimeCreatedInMilliseconds() {
        return timeCreatedInMilliseconds;
    }

    public long getTimeDoneInMilliseconds() {
        return timeDoneInMilliseconds;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getDescription() {
        return description;
    }

    // In case you make a typo or something
    public void setDescription(String description) {
        this.description = description;
    }
}
