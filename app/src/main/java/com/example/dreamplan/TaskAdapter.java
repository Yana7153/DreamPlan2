package com.example.dreamplan;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.R;
import com.example.dreamplan.database.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        // Set task details
        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(task.getNotes());
        holder.tvTaskDeadline.setText("Deadline: " +
                (task.getDeadline() != null ? task.getDeadline() : "Not set"));

        // Set color circle
        holder.imgTaskColor.setImageResource(task.getColorResId());

        // Set background color
        int backgroundColor = getBackgroundColor(task.getColorResId());
        GradientDrawable background = (GradientDrawable)holder.taskContainer.getBackground();
        background.setColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateTasks(List<Task> newTasks) {
        taskList = newTasks;
        notifyDataSetChanged();
    }

    private int getBackgroundColor(int colorResId) {
        // Create a mapping between circle drawables and background colors
        Map<Integer, Integer> colorMap = new HashMap<>();
        colorMap.put(R.drawable.circle_background_1, ContextCompat.getColor(context, R.color.task_bg_1));
        colorMap.put(R.drawable.circle_background_2, ContextCompat.getColor(context, R.color.task_bg_2));
        colorMap.put(R.drawable.circle_background_3, ContextCompat.getColor(context, R.color.task_bg_3));
        colorMap.put(R.drawable.circle_background_4, ContextCompat.getColor(context, R.color.task_bg_4));
        colorMap.put(R.drawable.circle_background_5, ContextCompat.getColor(context, R.color.task_bg_5));
        colorMap.put(R.drawable.circle_background_6, ContextCompat.getColor(context, R.color.task_bg_6));
        colorMap.put(R.drawable.circle_background_7, ContextCompat.getColor(context, R.color.task_bg_7));

        // Return the mapped color or default white
        return colorMap.getOrDefault(colorResId, Color.WHITE);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        LinearLayout taskContainer;
        ImageView imgTaskColor;
        TextView tvTaskTitle, tvTaskDescription, tvTaskDeadline;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskContainer = itemView.findViewById(R.id.task_container);
            imgTaskColor = itemView.findViewById(R.id.img_task_color);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskDeadline = itemView.findViewById(R.id.tv_task_deadline);
        }
    }
}