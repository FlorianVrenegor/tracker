package com.example.tracker.time;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracker.R;

public class TimeBoxViewHolder extends RecyclerView.ViewHolder {

    private final TextView startedTextView;
    private final TextView descriptionTextView;
    private final TextView durationTextView;

    public TimeBoxViewHolder(@NonNull View itemView) {
        super(itemView);
        startedTextView = itemView.findViewById(R.id.started_text_view);
        descriptionTextView = itemView.findViewById(R.id.description_text_view);
        durationTextView = itemView.findViewById(R.id.duration_text_view);
    }

    public void bind(TimeBoxDto timeBox) {
        startedTextView.setText(timeBox.getTimeStartedFormatted());
        descriptionTextView.setText(timeBox.getDescription());
        durationTextView.setText(timeBox.getDurationFormatted());
    }

    static TimeBoxViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        return new TimeBoxViewHolder(view);
    }
}
