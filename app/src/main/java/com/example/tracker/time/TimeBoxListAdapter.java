package com.example.tracker.time;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class TimeBoxListAdapter extends ListAdapter<TimeBoxDto, TimeBoxViewHolder> {

    protected TimeBoxListAdapter(@NonNull DiffUtil.ItemCallback<TimeBoxDto> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public TimeBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TimeBoxViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeBoxViewHolder holder, int position) {
        TimeBoxDto timeBox = getItem(position);
        holder.bind(timeBox);
    }

    static class TimeBoxDiff extends DiffUtil.ItemCallback<TimeBoxDto> {

        @Override
        public boolean areItemsTheSame(@NonNull TimeBoxDto oldItem, @NonNull TimeBoxDto newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TimeBoxDto oldItem, @NonNull TimeBoxDto newItem) {
            return oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getDurationInMilliseconds() == newItem.getDurationInMilliseconds()
                    && oldItem.getTimeStartedInMilliseconds() == newItem.getTimeStartedInMilliseconds();
        }
    }
}
