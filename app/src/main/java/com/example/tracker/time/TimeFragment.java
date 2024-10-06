package com.example.tracker.time;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracker.R;

import java.util.Locale;

public class TimeFragment extends Fragment {
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
        timerButton.setOnClickListener(v -> {
            long hours = hoursNumberPicker.getValue() * 60 * 60 * 1000;
            long minutes = minutesNumberPicker.getValue() * 60 * 1000;
            long seconds = secondsNumberPicker.getValue() * 1000;
            long milliseconds = hours + minutes + seconds;

            startTimer(milliseconds);
        });

        Button stopTimerButton = view.findViewById(R.id.stop_timer_button);
        stopTimerButton.setOnClickListener(v -> {
            if (timer != null) {
                timer.cancel();
                timerTextView.setText("00:00:00");
            }
        });

        Button fifteenMinuteButton = view.findViewById(R.id.fifteen_min_button);
        fifteenMinuteButton.setOnClickListener(v -> startTimer(15 * 60 * 1000));
        Button thirtyMinuteButton = view.findViewById(R.id.thirty_min_button);
        thirtyMinuteButton.setOnClickListener(v -> startTimer(30 * 60 * 1000));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        final TimeBoxListAdapter adapter = new TimeBoxListAdapter(new TimeBoxListAdapter.TimeBoxDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        timeViewModel = new ViewModelProvider(this).get(TimeViewModel.class);
        timeViewModel.getAllTimeBoxes().observe(getViewLifecycleOwner(), timeBoxes -> {
            adapter.submitList(timeBoxes);
        });
    }

    private void startTimer(final long milliseconds) {
        if (timerEditText.getText().toString().equals("")) {
            Toast.makeText(getContext(), "Bitte Beschreibung eintragen", Toast.LENGTH_LONG).show();
            return;
        }

        if (!running) {
            final long timerStartedInMillis = System.currentTimeMillis();
            running = true;

            Intent intent = new Intent(getActivity(), TimerBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(requireActivity().getApplicationContext(), 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + milliseconds, pendingIntent);

            timer = new CountDownTimer(milliseconds, 1000) {
                public void onTick(long millisUntilFinished) {
                    long seconds = millisUntilFinished / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;

                    String secondsFormatted = String.format(Locale.GERMANY, "%02d", seconds % 60);
                    String minutesFormatted = String.format(Locale.GERMANY, "%02d", minutes % 60);
                    String hoursFormatted = String.format(Locale.GERMANY, "%02d", hours % 24);
                    String timeString = hoursFormatted + ":" + minutesFormatted + ":" + secondsFormatted; // Extracted to avoid warning

                    timerTextView.setText(timeString);
                }

                public void onFinish() {
                    String task = timerEditText.getText().toString();
                    // Add the timeBox to the room database
                    timeViewModel.insert(new TimeBoxDto(timerStartedInMillis, milliseconds, task));

                    timerTextView.setText("Done!");
                    running = false;
                }
            }.start();
        }
    }
}
