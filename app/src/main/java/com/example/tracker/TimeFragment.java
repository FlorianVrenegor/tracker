package com.example.tracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

public class TimeFragment extends Fragment {

    public static final int NEW_TIMEBOX_FRAGMENT_REQUEST_CODE = 1;

    private List<TimeBox> completedTimeBoxes = new ArrayList<>();

    private boolean running = false;

    private CountDownTimer timer;

    private TextView timerTextView;
    private EditText timerEditText;

    private TimeViewModel timeViewModel;

    public TimeFragment() {
        super(R.layout.fragment_time);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NumberPicker hoursNumberPicker = view.findViewById(R.id.hours_number_picker);
        hoursNumberPicker.setMaxValue(23);
        hoursNumberPicker.setMinValue(0);

        final NumberPicker minutesNumberPicker = view.findViewById(R.id.minutes_number_picker);
        minutesNumberPicker.setMaxValue(59);
        minutesNumberPicker.setMinValue(0);

        final NumberPicker secondsNumberPicker = view.findViewById(R.id.seconds_number_picker);
        secondsNumberPicker.setMaxValue(59);
        secondsNumberPicker.setMinValue(0);

        timerEditText = view.findViewById(R.id.timer_edit_text);
        timerTextView = view.findViewById(R.id.timer_text_view);
        Button timerButton = view.findViewById(R.id.start_timer_button);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long hours = hoursNumberPicker.getValue() * 60 * 60 * 1000;
                long minutes = minutesNumberPicker.getValue() * 60 * 1000;
                long seconds = secondsNumberPicker.getValue() * 1000;
                long milliseconds = hours + minutes + seconds;

                startTimer(milliseconds);
            }
        });

        Button stopTimerButton = view.findViewById(R.id.stop_timer_button);
        stopTimerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                timerTextView.setText("00:00:00");
            }
        });

        Button fifteenMinuteButton = view.findViewById(R.id.fifteen_min_button);
        fifteenMinuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(15 * 60 * 1000);
            }
        });
        Button thirtyMinuteButton = view.findViewById(R.id.thirty_min_button);
        thirtyMinuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer(30 * 60 * 1000);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final TimeBoxListAdapter adapter = new TimeBoxListAdapter(new TimeBoxListAdapter.TimeBoxDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        timeViewModel = new ViewModelProvider(this).get(TimeViewModel.class);
        timeViewModel.getAllTimeBoxes().observe(getViewLifecycleOwner(), timeBoxes -> {
            adapter.submitList(timeBoxes);
        });
    }

    private void startTimer (final long milliseconds) {
        if (timerEditText.getText().toString().equals("")) {
            Toast.makeText(getContext(), "Bitte Beschreibung eintragen", Toast.LENGTH_LONG).show();
            return;
        }

        if (!running) {
            final long timerStartedInMillis = System.currentTimeMillis();
            running = true;
            timer = new CountDownTimer(milliseconds, 1000) {

                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;

                    String secondsFormatted = String.format("%02d", seconds % 60);
                    String minutesFormatted = String.format("%02d", minutes % 60);
                    String hoursFormatted = String.format("%02d", hours % 24);

                    timerTextView.setText(hoursFormatted + ":" + minutesFormatted + ":" + secondsFormatted);
                }

                public void onFinish() {
                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 300, 300, 300};
                    v.vibrate(VibrationEffect.createWaveform(pattern, -1)); // VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

                    timerTextView.setText("Done!");
                    running = false;

                    String task = timerEditText.getText().toString();
                    completedTimeBoxes.add(new TimeBox(timerStartedInMillis, milliseconds, task));
                    Toast.makeText(getContext(), "Completed '" + task + "'", Toast.LENGTH_LONG).show();

                    Log.d("TIME", "Task '" + task + "', started at: " + timerStartedInMillis + " with duration " + milliseconds + ", finished. Total completed: " + completedTimeBoxes.size());

                    // Add the timeBox to the room database
                    timeViewModel.insert(new TimeBoxEntity(timerStartedInMillis, milliseconds, task));
                }
            }.start();
        }
    }

    private class TimeBox {
        private long timeStartedInMilliseconds;
        private long durationInMilliseconds;
        private String task;

        public TimeBox(long timeStartedInMilliseconds, long durationInMilliseconds, String task) {
            this.timeStartedInMilliseconds = timeStartedInMilliseconds;
            this.durationInMilliseconds = durationInMilliseconds;
            this.task = task;
        }
    }
}
