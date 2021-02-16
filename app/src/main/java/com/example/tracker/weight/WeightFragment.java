package com.example.tracker.weight;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class WeightFragment extends Fragment {

    enum DisplayMode {
        WEEK, MONTH
    }

    private int week = 0;
    private int month = 0;
    private DisplayMode displayMode = DisplayMode.WEEK;

    private WeightAdapter adapter;

    private WeightViewModel weightViewModel;

    public WeightFragment() {
        super(R.layout.fragment_weight);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // this causes trouble if not explicitly set
        calendar.setMinimalDaysInFirstWeek(4); // this needs to be set aswell
        calendar.setTimeInMillis(System.currentTimeMillis());
        week = calendar.get(Calendar.WEEK_OF_YEAR);
        month = calendar.get(Calendar.MONTH);

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> weightViewModel.loadWeights());

        ListView weightListView = view.findViewById(R.id.weight_list_view);
        weightListView.setNestedScrollingEnabled(true);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Gewicht eingeben");

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

            LinearLayout layoutName = new LinearLayout(getContext());
            layoutName.setOrientation(LinearLayout.VERTICAL);
            layoutName.setPadding(60, 0, 60, 0);
            layoutName.addView(input);

            builder.setView(layoutName);
            builder.setPositiveButton("OK", (dialog, which) -> {
                String weightString = input.getText().toString();
                if (weightString != null && !weightString.isEmpty()) {
                    double weight = Double.parseDouble(weightString);
                    long currentTimeMillis = System.currentTimeMillis();

                    weightViewModel.saveWeight(currentTimeMillis, weight);
                }
            });
            builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
            builder.show();
        });

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

        Button lineChartWeek = view.findViewById(R.id.button_chart_week);
        lineChartWeek.setOnClickListener(v -> {
            if (displayMode != DisplayMode.WEEK) {
                calendar.setTimeInMillis(System.currentTimeMillis());
                week = calendar.get(Calendar.WEEK_OF_YEAR);
                setupLineChartWeek(week);
            }
            displayMode = DisplayMode.WEEK;
        });
        Button lineChartMonth = view.findViewById(R.id.button_chart_month);
        lineChartMonth.setOnClickListener(v -> {
            if (displayMode != DisplayMode.MONTH) {
                calendar.setTimeInMillis(System.currentTimeMillis());
                month = calendar.get(Calendar.MONTH);
                setupLineChartMonth(month);
            }
            displayMode = DisplayMode.MONTH;
        });

        Button lineChartPrev = view.findViewById(R.id.button_chart_prev);
        lineChartPrev.setOnClickListener(v -> {
            if (displayMode == DisplayMode.WEEK) {
                if (week > 0) {
                    week -= 1;
                }
                setupLineChartWeek(week);
            } else if (displayMode == DisplayMode.MONTH) {
                if (month > 0) {
                    month -= 1;
                }
                setupLineChartMonth(month);
            }
        });
        Button lineChartNext = view.findViewById(R.id.button_chart_next);
        lineChartNext.setOnClickListener(v -> {
            if (displayMode == DisplayMode.WEEK) {
                week += 1;
                setupLineChartWeek(week);
            } else if (displayMode == DisplayMode.MONTH) {
                month += 1;
                setupLineChartMonth(month);
            }
        });

        weightViewModel = new ViewModelProvider(this).get(WeightViewModel.class);
        weightViewModel.getWeights().observe(getViewLifecycleOwner(), weights -> {
            adapter.setWeights(weights);
//            setupLineChart();
            setupLineChartWeek(week);
//            setupLineChartMonth();
            swipeRefreshLayout.setRefreshing(false);
        });

        weightViewModel.loadWeights();
    }

    private void setupLineChartBase() {
        LineChart lineChart = getView().findViewById(R.id.line_chart);
//        lineChart.setViewPortOffsets(-40f, 0f, 0f, 0f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        ArrayList<Entry> yVals = new ArrayList<>();

        LineDataSet dataSet = new LineDataSet(yVals, "Weights");
//        dataSet.setDrawCircles(false);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // For smooth curve
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.gradient_linechart_background);
        dataSet.setFillDrawable(drawable);
//        dataSet.setFillAlpha(0);

        LineData lineData = new LineData(dataSet);
//        lineChart.setXAxisRenderer();

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGranularity(0.5f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);
//        rightAxis.setAxisLineWidth(0);
//        rightAxis.setXOffset(-5f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setData(lineData);
        lineChart.setTouchEnabled(false);
        lineChart.invalidate(); // So the chart refreshes and you don't have to click it

        lineChart.setExtraOffsets(5, 10, 0, 10);

    }

    private void setupLineChartWeek(int week) {
        LineChart lineChart = getView().findViewById(R.id.line_chart);
//        lineChart.setViewPortOffsets(-40f, 0f, 0f, 0f);

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

        Collections.sort(adapter.weights);

        boolean hasBefore = false;

        for (int i = 0; i < adapter.weights.size(); i++) {
            WeightDto dto = adapter.weights.get(i);
            if (dto.getWeek() == week) {
                if (i > 0 && yVals.size() == 0) {
                    yVals.add(new Entry(-1, (float) adapter.weights.get(i - 1).getWeightInKgs()));
                    hasBefore = true;
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(dto.getTimeInMillis());
                int weekday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; // because for some reason monday, the first day of the week, gets a 2, saturday is 7

                yVals.add(new Entry(weekday, (float) dto.getWeightInKgs()));

                if (adapter.weights.size() > i + 1 && yVals.size() == (hasBefore ? 8 : 7)) {
                    WeightDto dto2 = adapter.weights.get(i + 1);
                    yVals.add(new Entry(7, (float) dto2.getWeightInKgs()));
                }
            }
        }

        adapter.weights.sort(Collections.reverseOrder());

        yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));

        LineDataSet dataSet = new LineDataSet(yVals, "Weights");
//        dataSet.setDrawCircles(false);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // For smooth curve
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.gradient_linechart_background);
        dataSet.setFillDrawable(drawable);
//        dataSet.setFillAlpha(0);

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
//        rightAxis.setAxisLineWidth(0);
//        rightAxis.setXOffset(-5f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setData(lineData);
        lineChart.setTouchEnabled(false);
        lineChart.invalidate(); // So the chart refreshes and you don't have to click it

        lineChart.setExtraOffsets(5, 10, 0, 10);
    }

    private void setupLineChartMonth(int month) {
        LineChart lineChart = getView().findViewById(R.id.line_chart);
//        lineChart.setViewPortOffsets(-40f, 0f, 0f, 0f);

        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(month); // months starts at 0

        Collections.sort(adapter.weights);
        int counter = 0;

        ArrayList<Entry> yVals = new ArrayList<>();

        for (int i = 0; i < adapter.weights.size(); i++) {
            WeightDto dto = adapter.weights.get(i);
            if (dto.getMonth() == month) {
                if (i - 1 >= 0 && yVals.size() == 0) {
                    yVals.add(new Entry(-1, (float) adapter.weights.get(i - 1).getWeightInKgs()));
                }
                yVals.add(new Entry(counter++, (float) dto.getWeightInKgs()));

                if (adapter.weights.size() >= i + 1 && yVals.size() == 28) {
                    yVals.add(new Entry(7, (float) adapter.weights.get(i + 1).getWeightInKgs()));
                }
            }
        }
//        yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(5);
        xAxis.setLabelCount(daysInMonth);
        xAxis.setAxisMinimum(-0.25f);
        float max = yVals.size() - 1 + 0.25f;
        xAxis.setAxisMaximum(max);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value + "";
            }
        });

        LineDataSet dataSet = new LineDataSet(yVals, "Weights");
//        dataSet.setDrawCircles(false);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // For smooth curve
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.gradient_linechart_background);
        dataSet.setFillDrawable(drawable);
//        dataSet.setFillAlpha(0);

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
//        rightAxis.setAxisLineWidth(0);
//        rightAxis.setXOffset(-5f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setData(lineData);
        lineChart.setTouchEnabled(false);
        lineChart.invalidate(); // So the chart refreshes and you don't have to click it

        lineChart.setExtraOffsets(5, 10, 0, 10);
    }

    private void setupLineChart() {
        LineChart lineChart = getView().findViewById(R.id.line_chart);
//        lineChart.setViewPortOffsets(-40f, 0f, 0f, 0f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(5); // only intervals of 1 day
        xAxis.setLabelCount(28);
        xAxis.setAxisMinimum(-0.25f);
        xAxis.setAxisMaximum(12.25f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
//            final String[] days = new String[]{"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};

            @Override
            public String getFormattedValue(float value) {
                return value + ""; // days[(int) value % 7];
            }
        });

        ArrayList<Entry> yVals = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        int month = calendar.get(Calendar.MONTH);

        Collections.sort(adapter.weights);
        int counter = 0;

        for (int i = 0; i < adapter.weights.size(); i++) {
            WeightDto dto = adapter.weights.get(i);
            if (dto.getMonth() == month) {
                if (i - 1 >= 0 && yVals.size() == 0) {
                    yVals.add(new Entry(-1, (float) adapter.weights.get(i - 1).getWeightInKgs()));
                }

                calendar.setTimeInMillis(dto.getTimeInMillis());
//                int weekday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; // because for some reason monday, the first day of the week, gets a 2, saturday is 7

                yVals.add(new Entry(counter++, (float) dto.getWeightInKgs()));

                if (adapter.weights.size() >= i + 1 && yVals.size() == 28) {
                    yVals.add(new Entry(7, (float) adapter.weights.get(i + 1).getWeightInKgs()));
                }
            }
        }

        yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));

        LineDataSet dataSet = new LineDataSet(yVals, "Weights");
//        dataSet.setDrawCircles(false);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // For smooth curve
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.gradient_linechart_background);
        dataSet.setFillDrawable(drawable);
//        dataSet.setFillAlpha(0);

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
//        rightAxis.setAxisLineWidth(0);
//        rightAxis.setXOffset(-5f);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setData(lineData);
        lineChart.setTouchEnabled(false);
        lineChart.invalidate(); // So the chart refreshes and you don't have to click it

        lineChart.setExtraOffsets(5, 10, 0, 10);
    }
}
