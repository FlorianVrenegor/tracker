package com.example.tracker.todo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracker.R;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends Fragment {

    private TodoListAdapter adapter;

    public TodoFragment () {
        super(R.layout.fragment_todo);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<TodoDto> todoDtos = new ArrayList<>();

        todoDtos.add(new TodoDto("Müll rausbringen"));
        todoDtos.add(new TodoDto("Spülen"));
        todoDtos.add(new TodoDto("Saugen"));

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TodoListAdapter(todoDtos);
        recyclerView.setAdapter(adapter);
    }
}
