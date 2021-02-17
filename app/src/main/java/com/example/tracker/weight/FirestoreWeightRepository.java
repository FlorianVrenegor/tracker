package com.example.tracker.weight;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreWeightRepository {

    public static WeightDto from(QueryDocumentSnapshot document) {
        long timeInMillis = (long) document.getData().get("timeInMillis");
        double weightInKgs = (double) document.getData().get("weight in kilograms");
        return new WeightDto(timeInMillis, weightInKgs);
    }

    public static Map<String, Object> toMap(WeightDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("timeInMillis", dto.getTimeInMillis());
        map.put("weight in kilograms", dto.getWeightInKgs());
        return map;
    }

    public static String getId(WeightDto dto) {
        return dto.getTimeInMillis() + "_" + dto.getWeightInKgs();
    }
}
