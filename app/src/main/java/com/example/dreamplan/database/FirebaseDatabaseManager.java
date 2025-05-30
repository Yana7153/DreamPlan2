package com.example.dreamplan.database;

import android.util.Log;

import com.example.dreamplan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class FirebaseDatabaseManager {
    private static FirebaseDatabaseManager instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private List<DatabaseCallback<Void>> taskChangeListeners = new ArrayList<>();

    private FirebaseDatabaseManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseDatabaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseManager();
        }
        return instance;
    }

    public void getTasksForDate(String date, DatabaseCallback<List<Task>> callback) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("tasks")
                .get()
                .addOnSuccessListener(query -> {
                    List<Task> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Task task = doc.toObject(Task.class);
                        task.setId(doc.getId());

                        if (task.getDeadline().equals(date) ||
                                (task.isRecurring() && doesRecurOnDate(task, date))) {
                            tasks.add(task);
                        }
                    }
                    callback.onSuccess(tasks);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }


    public void getSections(DatabaseCallback<List<Section>> callback) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("sections")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Section> sections = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Manually map fields to handle conversion
                            Section section = new Section();
                            section.setId(doc.getId());
                            section.setName(doc.getString("name"));
                            section.setColor(doc.getString("color"));
                            section.setNotes(doc.getString("notes"));
                            section.setDefault(doc.getBoolean("isDefault"));
                            sections.add(section);
                        }
                        callback.onSuccess(sections);
                    } else {
                        Log.e("FIRESTORE", "Error getting sections", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void addSection(Section section, DatabaseCallback<String> callback) {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("name", section.getName());
        sectionData.put("createdAt", FieldValue.serverTimestamp());
        sectionData.put("color", section.getColor());
        sectionData.put("notes", section.getNotes());
        sectionData.put("isDefault", section.isDefault());

        db.collection("users").document(userId)
                .collection("sections")
                .add(sectionData)
                .addOnSuccessListener(docRef -> callback.onSuccess(docRef.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteSection(String sectionId, DatabaseCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("tasks")
                .whereEqualTo("sectionId", sectionId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }

                    db.collection("users").document(userId)
                            .collection("sections").document(sectionId)
                            .delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getTasksForSection(String sectionId, DatabaseCallback<List<Task>> callback) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("tasks")
                .whereEqualTo("sectionId", sectionId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> tasks = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Task taskObj = doc.toObject(Task.class);
                            if (doc.contains("isRecurring")) {
                                taskObj.setRecurring(doc.getBoolean("isRecurring"));
                            } else {
                                taskObj.setRecurring(false);
                            }
                            taskObj.setId(doc.getId());
                            tasks.add(taskObj);
                        }
                        callback.onSuccess(tasks);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void saveTask(Task task, DatabaseCallback<String> callback) {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("title", task.getTitle());
        taskData.put("notes", task.getNotes());
        taskData.put("deadline", task.getDeadline());
        taskData.put("colorResId", task.getColorResId());
        taskData.put("iconResId", task.getIconResId());
        taskData.put("iconResName", task.getIconResName());
        taskData.put("sectionId", task.getSectionId());
        taskData.put("isRecurring", task.isRecurring());
        taskData.put("startDate", task.getStartDate());
        taskData.put("schedule", task.getSchedule());
        taskData.put("timePreference", task.getTimePreference());
        taskData.put("createdAt", FieldValue.serverTimestamp());
        taskData.put("isCompleted", task.isCompleted());
        taskData.put("endDate", task.getEndDate());

        if (task.getIconResId() == 0) {
            task.setIconResId(R.drawable.star);
            task.setIconResName("star");
            Log.w("ICON_VALIDATION", "Fixing missing icon for task: " + task.getTitle());
        }

        if (task.getSectionId() == null || task.getSectionId().isEmpty()) {
            callback.onFailure(new Exception("Section ID is missing"));
            return;
        }

        if (task.getId() == null) {
            // New task
            db.collection("users").document(userId)
                    .collection("tasks")
                    .add(taskData)
                    .addOnSuccessListener(aVoid -> {
                        notifyTaskChangeListeners();
                        callback.onSuccess(task.getId());
                    })
                    .addOnFailureListener(callback::onFailure);
        } else {
            // Update existing task
            db.collection("users").document(userId)
                    .collection("tasks")
                    .document(task.getId())
                    .set(taskData)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(task.getId()))
                    .addOnFailureListener(callback::onFailure);
        }
    }

    public void getTaskCountForDate(String date, DatabaseCallback<Integer> callback) {
        String userId = auth.getCurrentUser().getUid();

        Log.d("FIREBASE_QUERY", "Querying tasks for date: " + date);

        db.collection("users").document(userId)
                .collection("tasks")
                .whereEqualTo("deadline", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        Log.d("FIREBASE_QUERY", "Found " + count + " tasks for " + date);
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Log.d("FIREBASE_QUERY", "Task: " + doc.getData());
                        }
                        callback.onSuccess(count);
                    } else {
                        Log.e("FIREBASE_QUERY", "Error getting tasks", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }

    private boolean doesRecurOnDate(Task task, String checkDate) {
        try {
            if (!task.isRecurring() || task.getStartDate() == null ||
                    task.getSchedule() == null || task.isCompleted()) {
                return false;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date startDate = sdf.parse(task.getStartDate());
            Date targetDate = sdf.parse(checkDate);

            if (task.getEndDate() != null && !task.getEndDate().isEmpty()) {
                Date endDate = sdf.parse(task.getEndDate());
                if (targetDate.after(endDate)) {
                    return false;
                }
            }

            if (targetDate.before(startDate)) {
                return false;
            }

            Calendar targetCal = Calendar.getInstance();
            targetCal.setTime(targetDate);
            int dayOfWeek = targetCal.get(Calendar.DAY_OF_WEEK);

            switch (task.getSchedule()) {
                case "Every day":
                    return true;
                case "Weekdays only":
                    return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
                case "Weekends only":
                    return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
                default:
                    return false;
            }
        } catch (ParseException e) {
            Log.e("DateCheck", "Error parsing dates", e);
            return false;
        }
    }

    public void deleteTask(String taskId, DatabaseCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    notifyTaskChangeListeners();
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getTaskCountForDateRange(String startDate, String endDate, DatabaseCallback<Integer> callback) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("tasks")
                .whereGreaterThanOrEqualTo("deadline", startDate)
                .whereLessThanOrEqualTo("deadline", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(task.getResult().size());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updateSection(Section section, DatabaseCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", section.getName());
        updates.put("notes", section.getNotes());
        updates.put("color", section.getColor());

        db.collection("users").document(userId)
                .collection("sections").document(section.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getTasksForDateRange(String startDate, String endDate, DatabaseCallback<List<Task>> callback) {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("tasks")
                .whereGreaterThanOrEqualTo("deadline", startDate)
                .whereLessThanOrEqualTo("deadline", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> tasks = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Task taskObj = doc.toObject(Task.class);
                            taskObj.setId(doc.getId());
                            tasks.add(taskObj);
                        }
                        callback.onSuccess(tasks);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void addTaskChangeListener(DatabaseCallback<Void> listener) {
        taskChangeListeners.add(listener);
    }

    public void removeTaskChangeListener(DatabaseCallback<Void> listener) {
        taskChangeListeners.remove(listener);
    }

    private void notifyTaskChangeListeners() {
        for (DatabaseCallback<Void> listener : taskChangeListeners) {
            listener.onSuccess(null);
        }
    }

    public void updateTask(Task task) {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("isCompleted", task.isCompleted());
        updates.put("title", task.getTitle());
        updates.put("deadline", task.getDeadline());

        db.collection("users").document(userId)
                .collection("tasks")
                .document(task.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> notifyTaskChangeListeners())
                .addOnFailureListener(e -> Log.e("Firebase", "Update failed", e));
    }

    private String getNextOccurrenceDate(Task task) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date currentDate = sdf.parse(task.getStartDate());
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);

            switch (task.getSchedule()) {
                case "Every day":
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case "Weekdays only":
                    do {
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    } while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                            cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
                    break;
                case "Weekends only":
                    do {
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    } while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                            cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
                    break;
                case "Weekly":
                    cal.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "Monthly":
                    cal.add(Calendar.MONTH, 1);
                    break;
                default:
                    cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            return sdf.format(cal.getTime());
        } catch (ParseException e) {
            Log.e("RecurringTask", "Error parsing date", e);
            return task.getStartDate(); // Fallback to original date
        }
    }
}