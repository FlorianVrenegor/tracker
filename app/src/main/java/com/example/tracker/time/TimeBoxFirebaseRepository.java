package com.example.tracker.time;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeBoxFirebaseRepository {
    private final static String FIREBASE_COLLECTION = "timeBoxes";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    MutableLiveData<List<TimeBoxDto>> timeBoxDtos = new MutableLiveData<>();

    public TimeBoxFirebaseRepository() {
        loadData();
    }

    public LiveData<List<TimeBoxDto>> getAllTimeBoxes() {
        return timeBoxDtos;
    }

    public void saveTimeBox(TimeBoxDto dto) {
//        timeBoxDtos.getValue().sort(Collections.reverseOrder());

        Map<String, Object> timeBox = new HashMap<>();
        timeBox.put("timeStartedInMilliseconds", dto.getTimeStartedInMilliseconds());
        timeBox.put("durationInMilliseconds", dto.getDurationInMilliseconds());
        timeBox.put("description", dto.getDescription());

        final String documentId = dto.getTimeStartedInMilliseconds() + "_" + dto.getDescription();

        db.collection(FIREBASE_COLLECTION).document(documentId).set(timeBox);
    }

    public void loadData() {
        db.collection(FIREBASE_COLLECTION)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<TimeBoxDto> timeBoxDtos = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        long timeStartedInMilliseconds = (long) document.getData().get("timeStartedInMilliseconds");
                        long durationInMilliseconds = (long) document.getData().get("durationInMilliseconds");
                        String description = document.getData().get("description").toString();

                        timeBoxDtos.add(new TimeBoxDto(timeStartedInMilliseconds, durationInMilliseconds, description));
                    }
                    timeBoxDtos.sort(Collections.reverseOrder());

                    this.timeBoxDtos.setValue(timeBoxDtos);
                }
            });
    }
}
