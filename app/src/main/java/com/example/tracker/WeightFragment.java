package com.example.tracker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WeightFragment extends Fragment {

    private static final String FIRESTORE_LOG_TAG = "Firestore";

    private WeightAdapter adapter;

    private FirebaseFirestore db;

    public WeightFragment() {
        super(R.layout.fragment_weight);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText weightEditText = view.findViewById(R.id.weight_edit_text);
        Button saveWeightButton = view.findViewById(R.id.save_weight_button);
        Button loadWeightButton = view.findViewById(R.id.load_weight_button);
        ListView weightListView = view.findViewById(R.id.weight_list_view);

        db = FirebaseFirestore.getInstance();
        adapter = new WeightAdapter();
        weightListView.setAdapter(adapter);
        weightListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            final WeightDto weight = adapter.weights.get(position);
            Toast.makeText(getActivity().getApplicationContext(), weight.getWeightString(), Toast.LENGTH_LONG).show();

            AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + position);
            final int positionToRemove = position; // needs to be final to be accessed in the onClick method below
            adb.setNegativeButton("Cancel", null);
            adb.setPositiveButton("Ok", (dialog, which) -> {
                db.collection("weights").document(weight.timeInMillis + "_" + weight.weightInKgs)
                        .delete()
                        .addOnSuccessListener(aVoid -> Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot successfully deleted!"))
                        .addOnFailureListener(e -> Log.w(FIRESTORE_LOG_TAG, "Error deleting document", e));
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

                saveWeight(db, currentTimeMillis, weight);

                weightEditText.setText("");

                // Hide keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view12.getWindowToken(), 0);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Missing weight.", Toast.LENGTH_LONG).show();
            }
        });

        loadWeightButton.setOnClickListener(v -> readWeights());

        readWeights();

        setupLineChart();
    }

    private void setupLineChart() {
        LineChart lineChart = getView().findViewById(R.id.line_chart);

//        ArrayList<String> dates = new ArrayList<>();
//        ArrayList<Entry> weights = new ArrayList<>();
//
//
//        for (WeightDto w : adapter.weights) {
//            dates.add(w.date);
//            weights.add(new Entry(w.date, (float) w.weightInKgs));
//        }
//
//        for (int i = 0; i < adapter.weights.size(); i++) {
//
//        }
//
//        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();

        try {
            String dateString = "2021-01-25";
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int weekNumber = cal.get(Calendar.WEEK_OF_YEAR);
        } catch (ParseException e) {
            Log.d("Exception", e.getMessage());
        }

        final ArrayList<String> xVals = new ArrayList<>();
        xVals.add("2021-01-25");
        xVals.add("2021-01-26");
        xVals.add("2021-01-27");
        xVals.add("2021-01-28");
        xVals.add("2021-01-29");
        xVals.add("2021-01-30");
        xVals.add("2021-01-31");
        xVals.add("2021-02-01");

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
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
        yVals.add(new Entry(0f, 85.5f));
        yVals.add(new Entry(1f, 84.5f));
        yVals.add(new Entry(2f, 84.5f));
        yVals.add(new Entry(3f, 83.5f));
        yVals.add(new Entry(4f, 82.5f));
        yVals.add(new Entry(5f, 83.5f));
        yVals.add(new Entry(6f, 82.5f));
        yVals.add(new Entry(7f, 81.5f));

        LineDataSet dataSet = new LineDataSet(yVals, "Weights");
        LineData lineData = new LineData(dataSet);
//        lineChart.setXAxisRenderer();

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGranularity(1f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setData(lineData);
    }

    private List<WeightDto> getWeightsForWeek(int week) {
        List<WeightDto> result = new ArrayList<>();
        for (WeightDto w : adapter.weights) {
            if (w.week == week) {
                result.add(w);
            }
        }
        return result;
    }

    private void saveWeight(FirebaseFirestore db, long timeInMillis, double weightKgs) {
        // Create a map of the object to save
        Map<String, Object> weight = new HashMap<>();
        weight.put("timeInMillis", timeInMillis);
        weight.put("weight in kilograms", weightKgs);

        final String documentId = timeInMillis + "_" + weightKgs;

        // Add a new document with a generated ID
        db.collection("weights").document(documentId)
                .set(weight)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot added with ID: " + documentId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FIRESTORE_LOG_TAG, "Error adding document", e);
                    }
                });
    }

    private void readWeights() {
        adapter.weights.clear();
        db.collection("weights")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            long timeInMillis = (long) document.getData().get("timeInMillis");
                            double weightInKgs = (double) document.getData().get("weight in kilograms");
                            adapter.weights.add(new WeightDto(timeInMillis, weightInKgs));
                            Log.d(FIRESTORE_LOG_TAG, document.getId() + " => " + document.getData());
                        }
                        Collections.sort(adapter.weights, Collections.reverseOrder());
//                            adapter.weights.sort();
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(FIRESTORE_LOG_TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    class WeightDto implements Comparable<WeightDto> {
        private final long timeInMillis;
        private final int week;
        private final String date;
        private final double weightInKgs;

        public WeightDto(long timeInMillis, double weightInKgs) {
            this.timeInMillis = timeInMillis;

            Date date = new Date(timeInMillis);
            this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.GERMANY).format(date);

            Calendar calendar = Calendar.getInstance(Locale.GERMANY);
            calendar.setTime(date);
            this.week = calendar.get(Calendar.WEEK_OF_YEAR);


            this.weightInKgs = weightInKgs;
        }

        public int getWeek() {
            return week;
        }

        public long getTimeInMillis() {
            return timeInMillis;
        }

        public String getDate() {
            return date;
        }

        public String getWeightString() {
            return weightInKgs + " kg";
        }

        @Override
        public int compareTo(WeightDto o) {
            if (o == null) {
                return 0;
            }
            long result = getTimeInMillis() - o.getTimeInMillis();
            return result < 0 ? -1 : result == 0 ? 0 : 1;
        }
    }

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
    }
}
