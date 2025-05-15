package com.example.dreamplan;

import static kotlin.text.Typography.section;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.R;
import com.example.dreamplan.database.Task;

import java.text.BreakIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;
    private OnTaskClickListener taskClickListener;


    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.taskClickListener = listener;
    }

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


        Log.d("TASK_DISPLAY", "Displaying task: " + task.getTitle() +
                "\nRecurring: " + task.isRecurring() +
                "\nStartDate: " + task.getStartDate() +
                "\nSchedule: " + task.getSchedule() +
                "\nDisplay: " + task.getTaskTypeDisplay());

        Log.d("TASK_LOAD", "Task loaded - " +
                "ID: " + task.getId() +
                ", Title: " + task.getTitle() +
                ", Recurring: " + task.isRecurring() +
                ", StartDate: " + task.getStartDate() +
                ", Schedule: " + task.getSchedule());

        Log.d("TASK_DEBUG", "Task ID: " + task.getId());
        Log.d("TASK_DEBUG", "Raw deadline: " + task.getDeadline());
        Log.d("TASK_DEBUG", "Formatted: " + task.getTaskTypeDisplay());

        int iconResId;
        try {
            // First try to get by name if available
            if (task.getIconResName() != null) {
                iconResId = context.getResources().getIdentifier(
                        task.getIconResName(),
                        "drawable",
                        context.getPackageName()
                );
            } else {
                // Fallback to direct ID
                iconResId = task.getIconResId();
            }

            if (iconResId == 0) {
                throw new Resources.NotFoundException();
            }
        } catch (Exception e) {
            iconResId = R.drawable.ic_default_task;
            Log.e("TASK_ICON", "Icon not found, using default", e);
        }


        holder.imgTaskIcon.setImageResource(iconResId);
        holder.imgTaskIcon.setContentDescription("Task icon");

        // Set basic task info
        holder.tvTaskType.setText(task.getTaskTypeDisplay());
        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(task.getNotes() != null ? task.getNotes() : "");

//        // Set task type display
//        if (task.isRecurring()) {
//
//            // Build the recurring task display string
//            StringBuilder recurringText = new StringBuilder();
//            recurringText.append("🔄 "); // Recurring icon
//
//            if (!TextUtils.isEmpty(task.getStartDate())) {
//                recurringText.append(task.getStartDate());
//            }
//
//            if (!TextUtils.isEmpty(task.getSchedule())) {
//                recurringText.append(" • ").append(task.getSchedule());
//            }
//
//            if (!TextUtils.isEmpty(task.getTimePreference())) {
//                recurringText.append(" • ").append(task.getTimePreference());
//            }
//
//            holder.tvTaskType.setText(recurringText.toString());
//            holder.tvTaskType.setTextColor(ContextCompat.getColor(context, R.color.recurring_task_color));
//        } else {
//            if (!TextUtils.isEmpty(task.getDeadline())) {
//                try {
//                    SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                    SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
//                    Date date = dbFormat.parse(task.getDeadline());
//                    holder.tvTaskType.setText("📅 " + displayFormat.format(date));
//                } catch (ParseException e) {
//                    holder.tvTaskType.setText("📅 " + task.getDeadline());
//                }
//            } else {
//                holder.tvTaskType.setText("📅 No date set");
//            }
//        }

        // Set task type display
        holder.tvTaskType.setText(task.getTaskTypeDisplay());

        if (task.isRecurring()) {
            holder.tvTaskType.setTextColor(ContextCompat.getColor(context, R.color.recurring_task_color));
        } else {
            holder.tvTaskType.setTextColor(ContextCompat.getColor(context, R.color.one_time_task_color));
        }

        // Set background color
        try {
            GradientDrawable background = (GradientDrawable) holder.taskContainer.getBackground();
            background.setColor(getBackgroundColor(task.getColorResId()));
        } catch (Exception e) {
            holder.taskContainer.setBackgroundColor(Color.WHITE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskClick(task);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskLongClick(task);
                return true;
            }
            return false;
        });
    }

    private void setTaskIcon(@NonNull TaskViewHolder holder, Task task) {
        int iconResId;
        try {
            // First try to load by resource ID if available
            if (task.getIconResId() != 0) {
                iconResId = task.getIconResId();
                holder.imgTaskIcon.setImageResource(iconResId);
                return;
            }

            // Fallback to loading by name
            if (task.getIconResName() != null) {
                iconResId = context.getResources().getIdentifier(
                        task.getIconResName(),
                        "drawable",
                        context.getPackageName()
                );
                if (iconResId != 0) {
                    holder.imgTaskIcon.setImageResource(iconResId);
                    return;
                }
            }

            // Ultimate fallback
            iconResId = R.drawable.ic_default_task;
            holder.imgTaskIcon.setImageResource(iconResId);

        } catch (Exception e) {
            Log.e("TaskAdapter", "Error loading icon", e);
            holder.imgTaskIcon.setImageResource(R.drawable.ic_default_task);
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

            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadius(12f);
            background.setColor(backgroundColor);
            background.setStroke(1, Color.argb(30, 0, 0, 0)); // subtle border

            holder.taskContainer.setBackground(background);

            // Add elevation for modern look
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
        if (colorResId == R.drawable.circle_background_1) {
            return ContextCompat.getColor(context, R.color.task_color_1);
        } else if (colorResId == R.drawable.circle_background_2) {
            return ContextCompat.getColor(context, R.color.task_color_2);
        } else if (colorResId == R.drawable.circle_background_3) {
            return ContextCompat.getColor(context, R.color.task_color_3);
        } else if (colorResId == R.drawable.circle_background_4) {
            return ContextCompat.getColor(context, R.color.task_color_4);
        } else if (colorResId == R.drawable.circle_background_5) {
            return ContextCompat.getColor(context, R.color.task_color_5);
        } else if (colorResId == R.drawable.circle_background_6) {
            return ContextCompat.getColor(context, R.color.task_color_6);
        } else if (colorResId == R.drawable.circle_background_7) {
            return ContextCompat.getColor(context, R.color.task_color_7);
        } else {
            return ContextCompat.getColor(context, R.color.task_default); // Default case
        }
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