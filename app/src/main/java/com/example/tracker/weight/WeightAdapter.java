package com.example.tracker.weight;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tracker.R;

import java.util.ArrayList;
import java.util.List;

class WeightAdapter extends BaseAdapter {

    public List<WeightDto> weights = new ArrayList<>();

    @Override
    public int getCount() {
        return weights.size();
    }

    @Override
    public WeightDto getItem(int position) {
        return weights.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;
        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.weight, parent, false);
        } else {
            result = convertView;
        }

        WeightDto item = getItem(position);

        ((TextView) result.findViewById(R.id.date_text_view)).setText(item.getDate());
        ((TextView) result.findViewById(R.id.weight_text_view)).setText(item.getWeightString());

        return result;
    }

    public void setWeights(List<WeightDto> weights) {
        this.weights = weights;
        notifyDataSetChanged();
    }
}