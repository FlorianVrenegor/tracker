package com.example.tracker.time;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracker.R;

public class TimeBoxViewHolder extends RecyclerView.ViewHolder {

    private final TextView taskTextView;
    private final TextView durationTextView;

    public TimeBoxViewHolder(@NonNull View itemView) {
        super(itemView);
        taskTextView = itemView.findViewById(R.id.task_text_view);
        durationTextView = itemView.findViewById(R.id.duration_text_view);
    }

    public void bind(TimeBoxEntity timeBox) {
        taskTextView.setText(timeBox.getTask());

        long durationInMilliseconds = timeBox.getDurationInMilliseconds();
        long durationInSeconds = durationInMilliseconds / 1000 % 60;
        long durationInMinutes = durationInMilliseconds / 60 / 1000;
        String durationString = "" + durationInMinutes + "m " + (durationInSeconds == 0L ? "" : durationInSeconds + "s"); // Extracted to avoid warning
        durationTextView.setText(durationString);
    }

    static TimeBoxViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new TimeBoxViewHolder(view);
    }
}
