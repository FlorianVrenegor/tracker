package com.example.tracker.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tracker.R;

import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.TodoViewHolder> {

    private final List<TodoDto> todoDtos;

    public TodoListAdapter(@NonNull List<TodoDto> todoDtos) {
        this.todoDtos = todoDtos;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recyclerview_item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        holder.getTodoDescriptionTextView().setText(todoDtos.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return todoDtos.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {

        private final TextView todoDescriptionTextView;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todoDescriptionTextView = itemView.findViewById(R.id.todo_description_text_view);
            todoDescriptionTextView.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "Item at position: " + this.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            });
        }

        public TextView getTodoDescriptionTextView() {
            return todoDescriptionTextView;
        }
    }
}