package com.example.tracker;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TimeFragment extends Fragment {

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

        final EditText timerEditText = view.findViewById(R.id.timer_edit_text);
        final TextView timerTextView = view.findViewById(R.id.timer_text_view);
        Button timerButton = view.findViewById(R.id.timer_button);
        timerButton.setOnClickListener(new View.OnClickListener() {
            private boolean running = false;

            @Override
            public void onClick(View v) {
                if (timerEditText.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Bitte Beschreibung eintragen", Toast.LENGTH_LONG).show();
                    return;
                }

                long hours = hoursNumberPicker.getValue() * 60 * 60 * 1000;
                long minutes = minutesNumberPicker.getValue() * 60 * 1000;
                long seconds = secondsNumberPicker.getValue() * 1000;

                long milliseconds = hours + minutes + seconds;

                if (!running) {
                    running = true;
                    new CountDownTimer(milliseconds, 1000) {

                        public void onTick(long millisUntilFinished) {
                            long seconds = millisUntilFinished / 1000;
                            long minutes = seconds / 60;
                            long hours = minutes / 60;

                            String secondsFormatted = String.format("%02d", seconds % 60);
                            String minutesFormatted = String.format("%02d", minutes % 60);
                            String hoursFormatted = String.format("%02d:", hours % 24);

                            timerTextView.setText(hoursFormatted + minutesFormatted + ":" + secondsFormatted);
                        }

                        public void onFinish() {
                            Vibrator v = (Vibrator) getActivity().getSystemService(getContext().VIBRATOR_SERVICE);
                            long[] pattern = {0, 300, 300, 300};
                            v.vibrate(VibrationEffect.createWaveform(pattern, -1)); // VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));

                            timerTextView.setText("Done!");
                            running = false;

                            Toast.makeText(getContext(), timerEditText.getText(), Toast.LENGTH_LONG).show();
                        }
                    }.start();
                }
            }
        });
    }
}
