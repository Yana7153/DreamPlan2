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

        Log.d("TASK_BIND", "Binding task: " + task.getTitle() +
                " | Icon: " + task.getIconResName() +
                " | Color: " + task.getColorResId());

        setTaskIcon(holder, task);

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(TextUtils.isEmpty(task.getNotes()) ? "" : task.getNotes());

        setTaskTypeDisplay(holder, task);

        setTaskBackground(holder, task);

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
        int iconResId = R.drawable.ic_default_task;

        try {
            if (!TextUtils.isEmpty(task.getIconResName())) {
                iconResId = context.getResources().getIdentifier(
                        task.getIconResName(),
                        "drawable",
                        context.getPackageName()
                );

                if (iconResId != 0) {
                    Drawable d = ContextCompat.getDrawable(context, iconResId);
                    if (d == null) {
                        throw new Resources.NotFoundException();
                    }
                }
            }

            if (iconResId == 0 && task.getIconResId() != 0) {
                iconResId = task.getIconResId();
                Drawable d = ContextCompat.getDrawable(context, iconResId);
                if (d == null) {
                    throw new Resources.NotFoundException();
                }
            }
        } catch (Exception e) {
            Log.e("TASK_ICON", "Error loading icon for task: " + task.getTitle(), e);
            iconResId = R.drawable.ic_default_task;
        }

        holder.imgTaskIcon.setImageResource(iconResId);
        holder.imgTaskIcon.setContentDescription(
                TextUtils.isEmpty(task.getTitle()) ?
                        "Task icon" :
                        task.getTitle() + " icon"
        );

        Log.d("ICON_DEBUG", "Setting icon for task: " + task.getTitle() +
                " | ResId: " + iconResId +
                " | ResName: " + task.getIconResName());
    }

    private void setTaskTypeDisplay(@NonNull TaskViewHolder holder, Task task) {
        holder.tvTaskType.setText(task.getTaskTypeDisplay());

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