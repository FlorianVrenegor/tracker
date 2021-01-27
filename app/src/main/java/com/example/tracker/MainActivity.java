package com.example.tracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String FIRESTORE_LOG_TAG = "Firestore";

    private WeightAdapter adapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText weightEditText = findViewById(R.id.weight_edit_text);
        Button saveWeightButton = findViewById(R.id.save_weight_button);
        Button loadWeightButton = findViewById(R.id.load_weight_button);
        ListView weightListView = findViewById(R.id.weight_list_view);

        db = FirebaseFirestore.getInstance();
        adapter = new WeightAdapter();
        weightListView.setAdapter(adapter);

        saveWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weightString = weightEditText.getText().toString();
                if (weightString != null && !weightString.isEmpty()) {
                    double weight = Double.parseDouble(weightString);
                    long currentTimeMillis = System.currentTimeMillis();
                    Date currentDate = new Date(currentTimeMillis);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(currentDate);
                    int week = cal.get(Calendar.WEEK_OF_YEAR);
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY).format(currentDate);
                    String time = new SimpleDateFormat("HH:mm", Locale.GERMANY).format(currentDate);
                    saveWeight(db, week, date, time, weight);
                    Toast.makeText(getApplicationContext(), weightString, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Missing weight.", Toast.LENGTH_LONG).show();
                }
            }
        });

        loadWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readWeights();
            }
        });

        readWeights();

        setupLineChart();
    }

    private void setupLineChart() {
        LineChart lineChart = findViewById(R.id.line_chart);

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
            final String[] days = new String[] {"Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"};

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

    private void saveWeight(FirebaseFirestore db, int week, String date, String time, double weightKgs) {
        // Create a map of the object to save
        Map<String, Object> weight = new HashMap<>();
        weight.put("week", week);
        weight.put("date", date);
        weight.put("time", time);
        weight.put("weight in kilograms", weightKgs);

        // Add a new document with a generated ID
        db.collection("weights")
                .add(weight)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
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
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int week = Math.toIntExact((long) document.getData().get("week"));
                                String date = (String) document.getData().get("date");
                                String time = (String) document.getData().get("time");
                                double weightInKgs = (double) document.getData().get("weight in kilograms");
                                adapter.weights.add(new WeightDto(week, date, time, weightInKgs));
                                Log.d(FIRESTORE_LOG_TAG, document.getId() + " => " + document.getData());
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.w(FIRESTORE_LOG_TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    class WeightDto {
        private final int week;
        private final String date;
        private final String time;
        private final double weightInKgs;

        public WeightDto(int week, String date, String time, double weightInKgs) {
            this.week = week;
            this.date = date;
            this.time = time;
            this.weightInKgs = weightInKgs;
        }

        public int getWeek() {
            return week;
        }

        public String getDateString() {
            return date + " " + time;
        }

        public String getWeightString() {
            return weightInKgs + " kg";
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

            ((TextView) result.findViewById(R.id.date_text_view)).setText(item.getDateString());
            ((TextView) result.findViewById(R.id.weight_text_view)).setText(item.getWeightString());

            return result;
        }
    }
}
