package com.example.tracker.weight;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class WeightViewModel extends AndroidViewModel {

    private static final String FIRESTORE_LOG_TAG = "Firestore";

    private FirebaseFirestore db;

    public WeightViewModel(@NonNull Application application) {
        super(application);

        db = FirebaseFirestore.getInstance();
    }

    public void deleteWeight(WeightDto weightDto) {
        db.collection("weights").document(weightDto.getTimeInMillis() + "_" + weightDto.getWeightInKgs())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w(FIRESTORE_LOG_TAG, "Error deleting document", e));
    }

    public void saveWeight(long timeInMillis, double weightKgs) {
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

    public void loadWeights(WeightAdapter adapter, Callable<Void> success, Callable<Void> after) {
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

                        try {
                            success.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.w(FIRESTORE_LOG_TAG, "Error getting documents.", task.getException());
                    }

                    try {
                        after.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
