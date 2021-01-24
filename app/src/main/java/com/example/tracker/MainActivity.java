package com.example.tracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String FIRESTORE_LOG_TAG = "Firestore";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText weightEditText = findViewById(R.id.weight_edit_text);
        Button saveWeightButton = findViewById(R.id.save_weight_button);
        Button loadWeightButton = findViewById(R.id.load_weight_button);

        db = FirebaseFirestore.getInstance();

        saveWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String weightString = weightEditText.getText().toString();
                if (weightString != null && !weightString.isEmpty()) {
                    double weight = Double.parseDouble(weightString);
                    saveWeight(db, System.currentTimeMillis(), weight);
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
    }

    private void saveWeight(FirebaseFirestore db, long currentTimeMillis, double weightKgs) {
        // Create a map of the object to save
        Map<String, Object> user = new HashMap<>();
        user.put("time in milliseconds", currentTimeMillis);
        user.put("weight in kilograms", weightKgs);

        // Add a new document with a generated ID
        db.collection("weights")
                .add(user)
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
        db.collection("weights")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(FIRESTORE_LOG_TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(FIRESTORE_LOG_TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }
}
