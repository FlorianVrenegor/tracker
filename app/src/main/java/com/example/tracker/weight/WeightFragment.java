package com.example.tracker.weight;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class WeightFragment extends Fragment {

    enum DisplayMode {
        NONE, WEEK, MONTH
    }

    private static final String yearMonthPattern = "MMMM yyyy";

    private YearWeek yearWeek;
    private YearMonth yearMonth;
    private DisplayMode displayMode = DisplayMode.WEEK;

    private WeightAdapter adapter;

    private LineChart lineChart;

    private WeightViewModel weightViewModel;

    public WeightFragment() {
        super(R.layout.fragment_weight);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        yearWeek = YearWeek.now();
        yearMonth = YearMonth.now();

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> weightViewModel.loadWeights());

        ListView weightListView = view.findViewById(R.id.weight_list_view);
        weightListView.setNestedScrollingEnabled(true);

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

            LinearLayout layoutName = new LinearLayout(getContext());
            layoutName.setOrientation(LinearLayout.VERTICAL);
            layoutName.setPadding(60, 0, 60, 0);
            layoutName.addView(input);

            AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
            adb.setTitle("Gewicht eingeben");
            adb.setView(layoutName);
            adb.setPositiveButton("OK", (dialog, which) -> {
                String weightString = input.getText().toString();
                if (weightString != null && !weightString.isEmpty()) {
                    weightViewModel.saveWeight(new WeightDto(System.currentTimeMillis(), Double.parseDouble(weightString)));
                }
            });
            adb.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
            adb.show();
        });

        adapter = new WeightAdapter();
        weightListView.setAdapter(adapter);
        weightListView.setOnItemLongClickListener((parent, view1, position, id) -> {

            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete this entry?");
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", (dialog, which) -> {
                weightViewModel.deleteWeight(adapter.weights.get(position));
                adapter.weights.remove(position);
                adapter.notifyDataSetChanged();
            });
            adb.show();

            return false;
        });

        LinearLayout lineChartNavigateBar = view.findViewById(R.id.line_chart_navigate_bar);
        TextView lineChartDescription = view.findViewById(R.id.line_chart_description);
        lineChartDescription.setText(yearWeek.getDayRange());

        Button lineChartWeek = view.findViewById(R.id.button_chart_week);
        lineChartWeek.setOnClickListener(v -> {
            if (displayMode != DisplayMode.WEEK) {
                yearWeek = YearWeek.now();
                displayMode = DisplayMode.WEEK;
            }
            lineChartDescription.setText(yearWeek.getDayRange());
            lineChartNavigateBar.setVisibility(View.VISIBLE);
            setupLineChart();
        });
        Button lineChartMonth = view.findViewById(R.id.button_chart_month);
        lineChartMonth.setOnClickListener(v -> {
            if (displayMode != DisplayMode.MONTH) {
                yearMonth = YearMonth.now();
                displayMode = DisplayMode.MONTH;
            }
            lineChartDescription.setText(yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern)));
            lineChartNavigateBar.setVisibility(View.VISIBLE);
            setupLineChart();
        });
        Button lineChartAll = view.findViewById(R.id.button_chart_all);
        lineChartAll.setOnClickListener(v -> {
            displayMode = DisplayMode.NONE;
            lineChartNavigateBar.setVisibility(View.GONE);
            setupLineChartAll();
        });

        ImageView lineChartNextImageView = view.findViewById(R.id.line_chart_navigate_next);
        lineChartNextImageView.setOnClickListener(v -> {
            if (displayMode == DisplayMode.WEEK) {
                yearWeek.plusWeek();
                lineChartDescription.setText(yearWeek.getDayRange());
            } else if (displayMode == DisplayMode.MONTH) {
                yearMonth = yearMonth.plusMonths(1);
                lineChartDescription.setText(yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern)));
            }
            setupLineChart();
        });
        ImageView lineChartBeforeImageView = view.findViewById(R.id.line_chart_navigate_before);
        lineChartBeforeImageView.setOnClickListener(v -> {
            if (displayMode == DisplayMode.WEEK) {
                yearWeek.minusWeek();
                lineChartDescription.setText(yearWeek.getDayRange());
            } else if (displayMode == DisplayMode.MONTH) {
                yearMonth = yearMonth.minusMonths(1);
                lineChartDescription.setText(yearMonth.format(DateTimeFormatter.ofPattern(yearMonthPattern)));
            }
            setupLineChart();
        });

        weightViewModel = new ViewModelProvider(this).get(WeightViewModel.class);
        weightViewModel.getWeights().observe(getViewLifecycleOwner(), weights -> {
            adapter.setWeights(weights);
            setupLineChart();
            swipeRefreshLayout.setRefreshing(false);
        });

        weightViewModel.loadWeights();

        lineChart = view.findViewById(R.id.line_chart);
    }

    private void setupLineChart() {
        if (displayMode == DisplayMode.WEEK) {
            setupLineChartWeek(yearWeek);
        } else if (displayMode == DisplayMode.MONTH) {
            setupLineChartMonth(yearMonth);
        } else if (displayMode == DisplayMode.NONE) {
            setupLineChartAll();
        }
    }

    private void setupLineChartWeek(YearWeek yearWeek) {
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
            if (dto.getWeek() == yearWeek.getWeek()) {
                if (i > 0 && yVals.size() == 0) {
                    yVals.add(new Entry(-1, (float) adapter.weights.get(i - 1).getWeightInKgs()));
                    hasBefore = true;
                }

                yVals.add(new Entry(dto.getDayOfWeek(), (float) dto.getWeightInKgs()));

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
        leftAxis.setGranularity(1f);
//        leftAxis.setAxisMinimum(lineData.getYMin() - 0.25f);
//        leftAxis.setAxisMaximum(lineData.getYMax() + 0.25f);
        leftAxis.setAxisMinimum(85 - 0.25f);
        leftAxis.setAxisMaximum(90 + 0.25f);

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

    private void setupLineChartMonth(YearMonth yearMonth) {
        Collections.sort(adapter.weights);
        ArrayList<Entry> yVals = new ArrayList<>();

        for (int i = 0; i < adapter.weights.size(); i++) {
            WeightDto dto = adapter.weights.get(i);
            if (dto.getMonth() == yearMonth.getMonth().getValue() && dto.getYear() == yearMonth.getYear()) {
                if (i - 1 >= 0 && yVals.size() == 0) {
                    yVals.add(new Entry(0, (float) adapter.weights.get(i - 1).getWeightInKgs()));
                }
                yVals.add(new Entry(dto.getDayInMonth(), (float) dto.getWeightInKgs()));

                if (adapter.weights.size() >= i + 1 && yVals.size() == 28) {
                    yVals.add(new Entry(7, (float) adapter.weights.get(i + 1).getWeightInKgs()));
                }
            }
        }
//        yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(5);
        xAxis.setAxisMinimum(0.75f);
        float max = yearMonth.lengthOfMonth() + 0.25f;
        xAxis.setAxisMaximum(max);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Math.round(value) + "";
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
//        leftAxis.setAxisMaximum(lineData.getYMax() + 0.25f);
        leftAxis.setAxisMinimum(85 - 0.25f);
        leftAxis.setAxisMaximum(90 + 0.25f);

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

    private void setupLineChartAll() {
        Collections.sort(adapter.weights);
        ArrayList<Entry> yVals = new ArrayList<>();

        for (int i = 0; i < adapter.weights.size(); i++) {
            yVals.add(new Entry(i, (float) adapter.weights.get(i).getWeightInKgs()));
        }
//        yVals.sort((e1, e2) -> (int) (e1.getX() - e2.getX()));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(5);
        xAxis.setAxisMinimum(0.75f);
        float max = yVals.size() - 1 + 0.25f;
        xAxis.setAxisMaximum(max);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Math.round(value) + "";
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
//        leftAxis.setAxisMaximum(lineData.getYMax() + 0.25f);
        leftAxis.setAxisMinimum(85 - 0.25f);
        leftAxis.setAxisMaximum(90 + 0.25f);

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
