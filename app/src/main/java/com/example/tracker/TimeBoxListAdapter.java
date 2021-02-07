package com.example.tracker;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class TimeBoxListAdapter extends ListAdapter<TimeBoxEntity, TimeBoxViewHolder> {

    protected TimeBoxListAdapter(@NonNull DiffUtil.ItemCallback<TimeBoxEntity> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public TimeBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TimeBoxViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeBoxViewHolder holder, int position) {
        TimeBoxEntity timeBox = getItem(position);
        holder.bind(timeBox);
    }

    static class TimeBoxDiff extends DiffUtil.ItemCallback<TimeBoxEntity> {

        @Override
        public boolean areItemsTheSame(@NonNull TimeBoxEntity oldItem, @NonNull TimeBoxEntity newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TimeBoxEntity oldItem, @NonNull TimeBoxEntity newItem) {
            return oldItem.getTask().equals(newItem.getTask())
                    && oldItem.getDurationInMilliseconds() == newItem.getDurationInMilliseconds()
                    && oldItem.getTimeStartedInMilliseconds() == newItem.getTimeStartedInMilliseconds();
        }
    }
}
