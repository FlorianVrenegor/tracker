package com.example.tracker.todo;

public class TodoDto {

    private long createdInMilliseconds;
    private long doneInMilliseconds;
    private boolean done;
    private String description;

    public TodoDto(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // In case you make a typo or something
    public void setDescription(String description) {
        this.description = description;
    }
}
