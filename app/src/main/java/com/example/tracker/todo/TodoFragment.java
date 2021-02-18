package com.example.tracker.todo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracker.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TodoFragment extends Fragment {

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
        TodoListAdapter adapter = new TodoListAdapter(todoDtos);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_todo);
        fab.setOnClickListener(v -> {
            final EditText input = new EditText(getContext());

            LinearLayout layoutName = new LinearLayout(getContext());
            layoutName.setOrientation(LinearLayout.VERTICAL);
            layoutName.setPadding(60, 0, 60, 0);
            layoutName.addView(input);

            AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
            adb.setTitle("Beschreibung eingeben");
            adb.setView(layoutName);
            adb.setPositiveButton("OK", (dialog, which) -> {
                String weightString = input.getText().toString();
                if (weightString != null && !weightString.isEmpty()) {
                    todoDtos.add(new TodoDto(weightString));
                }
            });
            adb.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
            adb.show();
        });
    }
}
