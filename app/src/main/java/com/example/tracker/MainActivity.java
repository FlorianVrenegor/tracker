package com.example.tracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Double> weights = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText weightEditText = findViewById(R.id.weight_edit_text);
        Button weightButton = findViewById(R.id.weight_button);

        weightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weightString = weightEditText.getText().toString();
                if(weightString != null && !weightString.isEmpty()) {
                    weights.add(Double.parseDouble(weightString));
                    Toast.makeText(getApplicationContext(), weightString, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Missing weight.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
