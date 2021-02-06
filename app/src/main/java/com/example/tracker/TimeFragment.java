package com.example.tracker;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

public class TimeFragment extends Fragment {

    private List<TimeBox> completedTimeBoxes = new ArrayList<>();

    private boolean running = false;

    private CountDownTimer timer;

    private TextView timerTextView;
    private EditText timerEditText;

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
