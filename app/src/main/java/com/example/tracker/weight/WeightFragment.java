package com.example.tracker.weight;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.tracker.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class WeightFragment extends Fragment {

    private WeightAdapter adapter;

    private WeightViewModel weightViewModel;

    public WeightFragment() {
        super(R.layout.fragment_weight);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> weightViewModel.loadWeights());

        final EditText weightEditText = view.findViewById(R.id.weight_edit_text);
        Button saveWeightButton = view.findViewById(R.id.save_weight_button);
        ListView weightListView = view.findViewById(R.id.weight_list_view);
        weightListView.setNestedScrollingEnabled(true);

        adapter = new WeightAdapter();
        weightListView.setAdapter(adapter);
        weightListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            final WeightDto weight = adapter.weights.get(position);
            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + position);
            final int positionToRemove = position; // needs to be final to be accessed in the onClick method below
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", (dialog, which) -> {
                weightViewModel.deleteWeight(weight);
                adapter.weights.remove(positionToRemove);
                adapter.notifyDataSetChanged();
            });
            adb.show();

            return false;
        });

        saveWeightButton.setOnClickListener(view12 -> {
            String weightString = weightEditText.getText().toString();
            if (weightString != null && !weightString.isEmpty()) {
                double weight = Double.parseDouble(weightString);

                long currentTimeMillis = System.currentTimeMillis();

                WeightDto weightDto = new WeightDto(currentTimeMillis, weight);
                adapter.weights.add(weightDto);
                Collections.sort(adapter.weights, Collections.reverseOrder());
                adapter.notifyDataSetChanged();

                weightViewModel.saveWeight(currentTimeMillis, weight);

                weightEditText.setText("");

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view12.getWindowToken(), 0);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Missing weight.", Toast.LENGTH_LONG).show();
            }
        });

        weightViewModel = new ViewModelProvider(this).get(WeightViewModel.class);
        weightViewModel.getWeights().observe(getViewLifecycleOwner(), weights -> {
            adapter.setWeights(weights);
            setupLineChart();
            swipeRefreshLayout.setRefreshing(false);
        });

        weightViewModel.loadWeights();
    }

    private void setupLineChart() {
        LineChart lineChart = getView().findViewById(R.id.line_chart);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setAxisMinimum(-0.25f);
        xAxis.setAxisMaximum(6.25f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            final String[] days = new String[]{"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};

            @Override
            public String getFormattedValue(float value) {
                return days[(int) value % 7];
            }
        });

        ArrayList<Entry> yVals = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int week = calendar.get(Calendar.WEEK_OF_YEAR);

        for (WeightDto dto : adapter.weights) {
            if (dto.getWeek() == week - 1) {
                calendar.setTimeInMillis(dto.getTimeInMillis());
                int weekday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; // because for some reason monday, the first day of the week, gets a 2, saturday is 7

                yVals.add(new Entry(weekday, (float) dto.getWeightInKgs()));
            }
        }

        yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));

        LineDataSet dataSet = new LineDataSet(yVals, "Weights");
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
//        lineChart.setXAxisRenderer();

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGranularity(0.5f);
//        leftAxis.setAxisMinimum(lineData.getYMin() - 0.25f);
        leftAxis.setAxisMaximum(lineData.getYMax() + 0.25f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setData(lineData);

        lineChart.setTouchEnabled(false);
        lineChart.invalidate(); // So the chart refreshes and you don't have to click it
    }
}
