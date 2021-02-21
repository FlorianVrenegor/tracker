package com.example.tracker.weight;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeightViewModel extends AndroidViewModel {

    private static final String FIRESTORE_LOG_TAG = "Firestore";
    private static final String FIRESTORE_COLLECTION_WEIGHTS = "weights";

    private RestRepository restRepository = new RestRepository();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final List<WeightDto> weightDtos = new ArrayList<>();
    private final MutableLiveData<List<WeightDto>> weights = new MutableLiveData<>();

    public WeightViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<WeightDto>> getWeights() {
        return weights;
    }

    public void deleteWeight(WeightDto dto) {
        weightDtos.remove(dto);
        weights.setValue(weightDtos);

        db.collection(FIRESTORE_COLLECTION_WEIGHTS).document(FirestoreWeightRepository.getId(dto))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.w(FIRESTORE_LOG_TAG, "Error deleting document", e));
    }

    public void saveWeight(WeightDto dto) {
        weightDtos.add(dto);
        weightDtos.sort(Collections.reverseOrder());
        weights.setValue(weightDtos);

        db.collection(FIRESTORE_COLLECTION_WEIGHTS).document(FirestoreWeightRepository.getId(dto))
                .set(FirestoreWeightRepository.toMap(dto))
                .addOnSuccessListener(o -> Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot added with ID: " + FirestoreWeightRepository.getId(dto)))
                .addOnFailureListener(e -> Log.w(FIRESTORE_LOG_TAG, "Error adding document", e));
    }

    public void loadWeights() {
//        weights.setValue(restRepository.loadWeights());

        db.collection(FIRESTORE_COLLECTION_WEIGHTS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        weightDtos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            weightDtos.add(FirestoreWeightRepository.from(document));
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
