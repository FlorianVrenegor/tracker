package com.example.tracker.todo;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoViewModel extends ViewModel {

    private final static String FIRESTORE_COLLECTION_TODOS = "todos";
    private final static String FIRESTORE_LOG_TAG = "Todo";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final List<TodoDto> todos = new ArrayList<>();
    private final MutableLiveData<List<TodoDto>> allTodos = new MutableLiveData<>();

    public MutableLiveData<List<TodoDto>> getAllTodos() {
        return allTodos;
    }

    public void loadTodos() {
        db.collection(FIRESTORE_COLLECTION_TODOS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        todos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            todos.add(from(document));
                            Log.d(FIRESTORE_LOG_TAG, document.getId() + " => " + document.getData());
                        }
                        allTodos.setValue(todos);
                    } else {
                        Log.w(FIRESTORE_LOG_TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    public void saveTodo(TodoDto dto) {
        todos.add(dto);
        allTodos.setValue(todos);

        db.collection(FIRESTORE_COLLECTION_TODOS)
                .document()
                .set(toMap(dto))
                .addOnSuccessListener(o -> Log.d(FIRESTORE_LOG_TAG, "DocumentSnapshot added with ID: " + dto))
                .addOnFailureListener(e -> Log.w(FIRESTORE_LOG_TAG, "Error adding document", e));
    }

    private Map<String, Object> toMap(TodoDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("timeCreatedInMilliseconds", dto.getTimeCreatedInMilliseconds());
        map.put("timeDoneInMilliseconds", dto.getTimeDoneInMilliseconds());
        map.put("done", dto.isDone());
        map.put("description", dto.getDescription());
        return map;
    }

    private TodoDto from(QueryDocumentSnapshot document) {
        long timeCreatedInMilliseconds = (long) document.getData().get("timeCreatedInMilliseconds");
        long timeDoneInMilliseconds = (long) document.getData().get("timeDoneInMilliseconds");
        boolean done = (boolean) document.getData().get("done");
        String description = (String) document.getData().get("description");
        return new TodoDto(timeCreatedInMilliseconds, timeDoneInMilliseconds, done, description);
    }
}
