package com.example.tracker.weight;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightViewModel extends AndroidViewModel {

    private static final String FIRESTORE_LOG_TAG = "Firestore";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final MutableLiveData<List<WeightDto>> weights = new MutableLiveData<>();

    public WeightViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<WeightDto>> getWeights() {
        return weights;
    }

    public void deleteWeight(WeightDto weightDto) {
        db.collection("weights").document(weightDto.getTimeInMillis() + "_" + weightDto.getWeightInKgs())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w(FIRESTORE_LOG_TAG, "Error deleting document", e));
    }

    public void saveWeight(long timeInMillis, double weightKgs) {
        weights.getValue().add(new WeightDto(timeInMillis, weightKgs));
        weights.getValue().sort(Collections.reverseOrder());

        Map<String, Object> weight = new HashMap<>();
        weight.put("timeInMillis", timeInMillis);
        weight.put("weight in kilograms", weightKgs);

        final String documentId = timeInMillis + "_" + weightKgs;

        db.collection("weights").document(documentId)
                .set(weight)
                .addOnSuccessListener(o -> Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot added with ID: " + documentId))
                .addOnFailureListener(e -> Log.w(FIRESTORE_LOG_TAG, "Error adding document", e));
    }

    public void loadWeights() {
        db.collection("weights")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<WeightDto> weightDtos = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            long timeInMillis = (long) document.getData().get("timeInMillis");
                            double weightInKgs = (double) document.getData().get("weight in kilograms");
                            weightDtos.add(new WeightDto(timeInMillis, weightInKgs));
                            Log.d(FIRESTORE_LOG_TAG, document.getId() + " => " + document.getData());
                        }
                        weightDtos.sort(Collections.reverseOrder());
                        weights.setValue(weightDtos);
                    } else {
                        Log.w(FIRESTORE_LOG_TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}
