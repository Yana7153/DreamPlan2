package com.example.dreamplan;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
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

import java.text.BreakIterator;
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

        // Set task icon
        try {
            holder.imgTaskIcon.setImageResource(task.getIconResId() != 0 ?
                    task.getIconResId() : R.drawable.ic_default_task);
        } catch (Resources.NotFoundException e) {
            holder.imgTaskIcon.setImageResource(R.drawable.ic_default_task);
        }

        // Set basic task info
        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(task.getNotes() != null ? task.getNotes() : "");

        // Set task type display - THIS IS THE CRUCIAL PART
        if (task.isRecurring()) {
            // Build the recurring task display string
            StringBuilder recurringText = new StringBuilder();
            recurringText.append("🔄 "); // Recurring icon

            if (!TextUtils.isEmpty(task.getStartDate())) {
                recurringText.append(task.getStartDate());
            }

            if (!TextUtils.isEmpty(task.getSchedule())) {
                recurringText.append(" • ").append(task.getSchedule());
            }

            if (!TextUtils.isEmpty(task.getTimePreference())) {
                recurringText.append(" • ").append(task.getTimePreference());
            }

            // If no details available, show default text
            if (recurringText.length() == "🔄 ".length()) {
                recurringText.append("Repeats regularly");
            }

            holder.tvTaskType.setText(recurringText.toString());
            holder.tvTaskType.setTextColor(ContextCompat.getColor(context, R.color.recurring_task_color));
            holder.tvTaskType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            // One-time task display
            String deadlineText = !TextUtils.isEmpty(task.getDeadline()) ?
                    task.getDeadline() : "No deadline";
            holder.tvTaskType.setText("📅 " + deadlineText);
            holder.tvTaskType.setTextColor(ContextCompat.getColor(context, R.color.one_time_task_color));
            holder.tvTaskType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        // Set background color
        try {
            GradientDrawable background = (GradientDrawable) holder.taskContainer.getBackground();
            background.setColor(getBackgroundColor(task.getColorResId()));
        } catch (Exception e) {
            holder.taskContainer.setBackgroundColor(Color.WHITE);
        }
    }

    private void setTaskIcon(@NonNull TaskViewHolder holder, Task task) {
        try {
            int iconResId = task.getIconResId() != 0 ? task.getIconResId() : R.drawable.star;
            holder.imgTaskIcon.setImageResource(iconResId);

            // Optional: Add some padding and scaling
            holder.imgTaskIcon.setPadding(8, 8, 8, 8);
            holder.imgTaskIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } catch (Resources.NotFoundException e) {
            holder.imgTaskIcon.setImageResource(R.drawable.star);
            Log.w("TaskAdapter", "Icon not found, using default", e);
        }
    }

    private void setTaskTypeDisplay(@NonNull TaskViewHolder holder, Task task) {
        // Set the text
        holder.tvTaskType.setText(task.getTaskTypeDisplay());

        // Style based on task type
        int textColor, bgColor;
        float textSize;

        if (task.isRecurring()) {
            textColor = ContextCompat.getColor(context, R.color.recurring_text_color);
            bgColor = ContextCompat.getColor(context, R.color.recurring_bg_color);
            textSize = 13f;
        } else {
            textColor = ContextCompat.getColor(context, R.color.one_time_text_color);
            bgColor = ContextCompat.getColor(context, R.color.one_time_bg_color);
            textSize = 13f;
        }

        holder.tvTaskType.setTextColor(textColor);
        holder.tvTaskType.setTextSize(textSize);

        // Optional: Add visual indicator
        GradientDrawable typeIndicator = new GradientDrawable();
        typeIndicator.setShape(GradientDrawable.RECTANGLE);
        typeIndicator.setCornerRadius(16f);
        typeIndicator.setColor(bgColor);
        typeIndicator.setStroke(1, textColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.tvTaskType.setBackground(typeIndicator);
        } else {
            holder.tvTaskType.setBackgroundDrawable(typeIndicator);
        }

        // Add some padding
        holder.tvTaskType.setPadding(8, 4, 8, 4);
    }

    private void setTaskBackground(@NonNull TaskViewHolder holder, Task task) {
        try {
            int backgroundColor = getBackgroundColor(task.getColorResId());

            // Create a rounded rectangle background
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(12f);
            background.setColor(backgroundColor);

            // Add a subtle border
            int borderColor = Color.argb(30, 0, 0, 0); // Semi-transparent black
            background.setStroke(1, borderColor);

            holder.taskContainer.setBackground(background);

            // Add some elevation for modern look
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.taskContainer.setElevation(2f);
            }
        } catch (Exception e) {
            Log.w("TaskAdapter", "Error setting background", e);
            holder.taskContainer.setBackgroundColor(Color.WHITE);
        }
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
        public TextView tvTaskType;
        ImageView imgTaskIcon;
        LinearLayout taskContainer;
        ImageView imgTaskColor;
        TextView tvTaskTitle, tvTaskDescription, tvTaskDeadline;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskContainer = itemView.findViewById(R.id.task_container);
            imgTaskIcon = itemView.findViewById(R.id.img_task_icon);
            //     imgTaskColor = itemView.findViewById(R.id.img_task_color);
            tvTaskTitle = itemView.findViewById(R.id.tv_task_title);
            tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
            tvTaskType = itemView.findViewById(R.id.tv_task_type);
        }
    }
}