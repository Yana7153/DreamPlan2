package com.example.dreamplan.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.R;
import com.example.dreamplan.database.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;
    private OnTaskClickListener taskClickListener;
    private static OnTaskDeleteListener taskDeleteListener;
    private boolean isPendingUpdate = false;
  //  private List<Task> taskList = new ArrayList<>();
    private boolean isUpdateInProgress = false;
  // private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    public void setOnTaskDeleteListener(OnTaskDeleteListener listener) {
        this.taskDeleteListener = listener;
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
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

        int iconResId = task.getIconResId(holder.itemView.getContext());
        holder.imgTaskIcon.setImageResource(iconResId);

        // Debug log
        Log.d("TASK_ICON", "Displaying icon for task: " + task.getTitle() +
                " | Icon ID: " + iconResId +
                " | Icon Name: " + task.getIconResName());

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(TextUtils.isEmpty(task.getNotes()) ? "" : task.getNotes());

        setTaskTypeDisplay(holder, task);

        setTaskBackground(holder, task);

        holder.itemView.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskClick(task);
            }
        });

//        holder.itemView.setOnLongClickListener(v -> {
//            if (taskClickListener != null) {
//                taskClickListener.onTaskLongClick(task);
//                return true;
//            }
//            return false;
//        });
    }


    private void setTaskIcon(@NonNull TaskViewHolder holder, Task task) {
        try {
            int iconResId = task.getIconResId(holder.itemView.getContext());
            if (iconResId != 0) {
                holder.imgTaskIcon.setImageResource(iconResId);
            } else {
                holder.imgTaskIcon.setImageResource(R.drawable.ic_default_task);
            }
        } catch (Exception e) {
            holder.imgTaskIcon.setImageResource(R.drawable.ic_default_task);
        }
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
        if (isPendingUpdate) {
            return;
        }

        isPendingUpdate = true;

        new AsyncTask<List<Task>, Void, DiffUtil.DiffResult>() {
            private final List<Task> oldTasks = new ArrayList<>(taskList);

            @SafeVarargs
            @Override
            protected final DiffUtil.DiffResult doInBackground(List<Task>... lists) {
                return DiffUtil.calculateDiff(new TaskDiffCallback(oldTasks, lists[0]));
            }

            @Override
            protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                taskList.clear();
                taskList.addAll(newTasks);
                diffResult.dispatchUpdatesTo(TaskAdapter.this);
                isPendingUpdate = false;
            }
        }.execute(newTasks);
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

    class TaskViewHolder extends RecyclerView.ViewHolder {
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

            itemView.setOnLongClickListener(v -> {
                if (taskDeleteListener != null &&
                        getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Task task = taskList.get(getAdapterPosition());
                    if (task != null && task.getId() != null) {
                        taskDeleteListener.onTaskDelete(task);
                        return true;
                    }
                }
                return false;
            });
        }
    }

    private static class TaskDiffCallback extends DiffUtil.Callback {
        private final List<Task> oldTasks;
        private final List<Task> newTasks;

        public TaskDiffCallback(List<Task> oldTasks, List<Task> newTasks) {
            this.oldTasks = oldTasks;
            this.newTasks = newTasks;
        }

        @Override
        public int getOldListSize() {
            return oldTasks.size();
        }

        @Override
        public int getNewListSize() {
            return newTasks.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldTasks.get(oldItemPosition).getId().equals(newTasks.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldTasks.get(oldItemPosition).equals(newTasks.get(newItemPosition));
        }
    }

    public void removeTaskById(String taskId) {
        int position = -1;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(taskId)) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            List<Task> newList = new ArrayList<>(taskList);
            newList.remove(position);
            updateTasks(newList);
        }
    }

    public void removeTaskImmediately(String taskId) {
        int position = -1;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(taskId)) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            taskList.remove(position);
            notifyItemRemoved(position);

            if (position < taskList.size()) {
                notifyItemRangeChanged(position, taskList.size() - position);
            }
        }
    }

    public void safeUpdateTasks(List<Task> newTasks) {
        if (isUpdateInProgress) {
            return;
        }

        isUpdateInProgress = true;

        new AsyncTask<List<Task>, Void, DiffUtil.DiffResult>() {
            private final List<Task> oldTasks = new ArrayList<>(taskList);

            @Override
            protected DiffUtil.DiffResult doInBackground(List<Task>... lists) {
                return DiffUtil.calculateDiff(new TaskDiffCallback(oldTasks, lists[0]));
            }

            @Override
            protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                taskList.clear();
                taskList.addAll(newTasks);
                diffResult.dispatchUpdatesTo(TaskAdapter.this);
                isUpdateInProgress = false;
            }
        }.execute(newTasks);
    }

    public void safeRemoveTask(String taskId) {
        int position = -1;
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getId().equals(taskId)) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            List<Task> newList = new ArrayList<>(taskList);
            newList.remove(position);
            safeUpdateTasks(newList);
        }
    }

//    public void removeTaskSafely(String taskId) {
//        if (isUpdateInProgress) {
//            mainHandler.postDelayed(() -> removeTaskSafely(taskId), 100);
//            return;
//        }
//
//        isUpdateInProgress = true;
//
//        int position = -1;
//        for (int i = 0; i < taskList.size(); i++) {
//            if (taskList.get(i).getId().equals(taskId)) {
//                position = i;
//                break;
//            }
//        }
//
//        if (position != -1) {
//            List<Task> newList = new ArrayList<>(taskList);
//            newList.remove(position);
//            updateWithDiffUtil(newList);
//        }
//    }

    private void updateWithDiffUtil(List<Task> newTasks) {
        new AsyncTask<List<Task>, Void, DiffUtil.DiffResult>() {
            @Override
            protected DiffUtil.DiffResult doInBackground(List<Task>... lists) {
                return DiffUtil.calculateDiff(new TaskDiffCallback(taskList, lists[0]));
            }

            @Override
            protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                taskList.clear();
                taskList.addAll(newTasks);
                diffResult.dispatchUpdatesTo(TaskAdapter.this);
                isUpdateInProgress = false;
            }
        }.execute(newTasks);
    }

    public void removeTask(Task task) {
        int position = taskList.indexOf(task);
        if (position != -1) {
            taskList.remove(position);
            notifyItemRemoved(position);
        }
    }
}