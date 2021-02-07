package com.example.tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        String cringe = "" + timeBox.getDurationInMilliseconds();
        durationTextView.setText(cringe);
    }

    static TimeBoxViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new TimeBoxViewHolder(view);
    }
}