package com.example.dreamplan;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable; // Import LayerDrawable
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dreamplan.database.DatabaseManager;
import com.example.dreamplan.database.Section;

public class SectionEditDialogFragment extends DialogFragment {

    private static final String ARG_SECTION = "section";

    private Section section;
    // Corrected IDs to match task_input_dialog.xml
    private EditText etSectionName, etNotes;
    private Button btnDelete;
    private Button btnSave; // This refers to btn_save_section in the layout

    // Assuming these colors and IDs are from task_input_dialog.xml
    private static final int[] COLOR_CIRCLE_IDS = {
            R.id.color_circle_1, R.id.color_circle_2, R.id.color_circle_3,
            R.id.color_circle_4, R.id.color_circle_5, R.id.color_circle_6,
            R.id.color_circle_7
    };
    private static final String[] COLORS = {
            "#CCE1F2", "#C6F8E5", "#FBF7D5",
            "#F9DED7", "#F5CDDE", "#E2BEF1", "#D3D3D3"
    };
    private ImageView[] colorCircles;
    private String selectedColor;

    // Listener interface
    public interface SectionEditListener {
        void onSectionEdited(Section section);
        void onSectionDeleted(Section section);
        // Removed onSectionEditCancelled as Save/Cancel buttons are now explicitly handled or rely on dialog dismiss
    }

    private SectionEditListener listener;

    // Use onAttach to get the listener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Try casting the parent fragment first
        if (getParentFragment() instanceof SectionEditListener) {
            listener = (SectionEditListener) getParentFragment();
        } else if (context instanceof SectionEditListener) {
            // Otherwise, try casting the activity
            listener = (SectionEditListener) context;
        } else {
            // Optional: Throw an exception if the listener is not implemented
            Log.w("SectionEditDialog", "Host does not implement SectionEditListener");
            // You might want to throw a RuntimeException here in a real app
        }
    }

    public static SectionEditDialogFragment newInstance(Section section) {
        SectionEditDialogFragment fragment = new SectionEditDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SECTION, section);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Inflate task_input_dialog.xml
        View view = inflater.inflate(R.layout.task_input_dialog, null);

        // Initialize views from task_input_dialog.xml
        TextView dialogTitle = view.findViewById(R.id.dialog_title); // Assuming you added a title TextView
        etSectionName = view.findViewById(R.id.et_section_name);
        etNotes = view.findViewById(R.id.et_notes); // Correct ID from HomeFragment/task_input_dialog.xml usage

        // Get buttons from the layout
        btnSave = view.findViewById(R.id.btn_save_section); // Save button ID from task_input_dialog.xml
        btnDelete = view.findViewById(R.id.btn_delete_section); // Delete button ID from task_input_dialog.xml

        // Initialize color circles from task_input_dialog.xml
        colorCircles = new ImageView[COLORS.length];
        for (int i = 0; i < COLORS.length; i++) {
            colorCircles[i] = view.findViewById(COLOR_CIRCLE_IDS[i]);
            if (colorCircles[i] == null) {
                Log.e("SectionEditDialog", "Color circle view not found for ID: " + COLOR_CIRCLE_IDS[i]);
                // Handle missing views - maybe disable color selection
            }
        }


        // Load section data if editing
        if (getArguments() != null && getArguments().containsKey(ARG_SECTION)) {
            section = (Section) getArguments().getSerializable(ARG_SECTION);
            // >>> CHANGE THE CONDITION HERE TO CHECK section != null AND section.getId() > 0 <<<
            if (section != null && section.getId() > 0) { // It's an existing section being edited
                if (dialogTitle != null) dialogTitle.setText("Edit Section"); // Set title for editing
                etSectionName.setText(section.getName());
                etNotes.setText(section.getNotes());
                selectedColor = section.getColor();

                // Show delete button only when editing an existing section
                if (btnDelete != null) {
                    btnDelete.setVisibility(View.VISIBLE);
                }

                // Highlight current color
                updateColorSelectionUI();

            } else { // It's a new section being added (section is not null but ID is 0) OR section is null
                Log.d("SectionEditDialog", "Handling Add Section mode or null section.");
                if (dialogTitle != null) dialogTitle.setText("Add Section"); // Set title for adding
                // Handle the case where the passed section is null (shouldn't happen if newInstance(section) is always called)
                if (section == null) {
                    Log.e("SectionEditDialog", "Section object is null in arguments when expected new section.");
                    // Set default empty values
                    etSectionName.setText("");
                    etNotes.setText("");
                } else {
                    // Section is not null but ID is 0 - this is the 'Add Section' case
                    etSectionName.setText(section.getName()); // Should be empty string
                    etNotes.setText(section.getNotes()); // Should be empty string
                }

                if (btnDelete != null) {
                    btnDelete.setVisibility(View.GONE); // Hide delete for new sections
                }
                selectedColor = COLORS[COLORS.length - 1]; // Default color (e.g., gray)
                // Highlight default color
                updateColorSelectionUI();
            }
        } else { // This original else block is likely not needed anymore if newInstance(section) is always used
            // This case would only happen if the fragment was created without arguments or ARG_SECTION
            Log.w("SectionEditDialog", "Fragment created without expected ARG_SECTION argument.");
            if (dialogTitle != null) dialogTitle.setText("Add Section (No Args)");
            if (btnDelete != null) {
                btnDelete.setVisibility(View.GONE);
            }
            selectedColor = COLORS[COLORS.length - 1]; // Default color
            updateColorSelectionUI();
            section = null; // Ensure section is null if no args
        }


        // >>> REMOVE AlertDialog.Builder buttons - These lines are now removed <<<
        // builder.setPositiveButton("Save", (dialog, id) -> saveSection());
        // builder.setNegativeButton("Cancel", (dialog, id) -> dismiss());


        // Set up color selection listeners (Only if color circles were found)
        if (colorCircles != null) {
            for (int i = 0; i < COLORS.length; i++) {
                final int index = i;
                if (colorCircles[i] != null) { // Check if view was found
                    colorCircles[i].setOnClickListener(v -> {
                        selectedColor = COLORS[index];
                        updateColorSelectionUI();
                    });
                }
            }
        }


        // Set up click listeners for the buttons IN THE LAYOUT
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveSection());
        } else {
            Log.e("SectionEditDialog", "Save button (btn_save_section) not found in layout task_input_dialog.xml");
            // Handle this error - maybe disable save or show a message
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        } else {
            Log.e("SectionEditDialog", "Delete button (btn_delete_section) not found in layout task_input_dialog.xml");
            // Handle this error (less critical as it's hidden for add)
        }


        // Set the view to the builder
        builder.setView(view);

        // Return the created dialog
        return builder.create();
    }

    // Helper to update the selection border on color circles
    private void updateColorSelectionUI() {
        if (colorCircles != null && COLORS != null) {
            for (int i = 0; i < COLORS.length; i++) {
                // Check if both the color circle view and the color value are valid
                if (colorCircles[i] != null && i < COLORS.length) {
                    colorCircles[i].setBackground(getCircleDrawable(COLORS[i], COLORS[i].equals(selectedColor)));
                } else if (colorCircles[i] == null) {
                    Log.w("SectionEditDialog", "Color circle view is null at index: " + i);
                } else {
                    Log.w("SectionEditDialog", "Color value is missing at index: " + i + " in COLORS array.");
                }
            }
        } else {
            Log.w("SectionEditDialog", "colorCircles or COLORS array is null. Cannot update color selection UI.");
        }
    }


    // Helper method to get the drawable for a color circle with border
    private Drawable getCircleDrawable(String color, boolean isSelected) {
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        try {
            circle.setColor(Color.parseColor(color));
        } catch (IllegalArgumentException e) {
            Log.e("SectionEditDialog", "Invalid color hex for drawable: " + color, e);
            circle.setColor(Color.GRAY); // Fallback color
        }
        circle.setSize(48, 48); // Increased size slightly for better tapping

        if (isSelected) {
            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.OVAL);
            border.setSize(48, 48); // Match circle size
            border.setStroke(6, Color.BLACK); // Thicker black border

            LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{circle, border});
            // Adjust padding to center the circle within the border
            layerDrawable.setLayerInset(0, 3, 3, 3, 3); // Example inset, adjust as needed
            return layerDrawable;
        } else {
            return circle;
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Section")
                .setMessage("Are you sure you want to delete '" + section.getName() + "' and all its tasks?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteSection(); // Call the delete method
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveSection() {
        String name = etSectionName.getText().toString().trim();
        String notes = etNotes.getText().toString().trim(); // Get notes from etNotes

        if (TextUtils.isEmpty(name)) {
            etSectionName.setError("Section Name is required");
            etSectionName.requestFocus(); // Request focus to show error
            return;
        }

        DatabaseManager dbManager = null;
        boolean success = false;
        Section savedOrUpdatedSection = null;

        try {
            dbManager = new DatabaseManager(requireContext());
            if (section == null || section.getId() == 0) {
                // This branch might not be fully needed if HomeFragment handles 'Add Section'
                // Create new
                Section newSection = new Section(0, name, selectedColor, notes);
                long newRowId = dbManager.insertSection(newSection); // Now insertSection returns long
                if (newRowId > 0) {
                    success = true;
                    // Retrieve the inserted section to get its generated ID
                    savedOrUpdatedSection = dbManager.getSection((int) newRowId); // Requires getSection(id) in DatabaseManager
                } else {
                    success = false; // Indicate insertion failure
                }

            } else {
                // Update existing
                section.setName(name);
                section.setNotes(notes);
                section.setColor(selectedColor);
                success = dbManager.updateSection(section);
                if (success) {
                    savedOrUpdatedSection = section; // Use the updated section object
                }
            }

            if (success) {
                Toast.makeText(getContext(), "Section saved successfully", Toast.LENGTH_SHORT).show();
                // Notify listener about the edit/save
                if (listener != null && savedOrUpdatedSection != null) {
                    listener.onSectionEdited(savedOrUpdatedSection);
                }
                dismiss(); // Dismiss the dialog on success
            } else {
                Toast.makeText(getContext(), "Failed to save section", Toast.LENGTH_SHORT).show();
                // Keep dialog open or handle the failure UI
            }
        } catch (Exception e) {
            Log.e("SectionEditDialog", "Error saving section", e);
            Toast.makeText(getContext(), "An error occurred while saving", Toast.LENGTH_SHORT).show();
        } finally {
            if (dbManager != null) {
                // dbManager.close(); // DatabaseManager manages its own closing
            }
        }
    }


    private void deleteSection() {
        if (section == null || section.getId() == 0) {
            Toast.makeText(getContext(), "Cannot delete unsaved or invalid section", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseManager dbManager = null;
        boolean deleted = false;
        try {
            dbManager = new DatabaseManager(requireContext());
            deleted = dbManager.deleteSection(section.getId());

            if (deleted) {
                Toast.makeText(getContext(), "Section deleted successfully", Toast.LENGTH_SHORT).show();
                // Notify listener about the delete
                if (listener != null) {
                    listener.onSectionDeleted(section); // Pass the deleted section object
                }
                dismiss(); // Dismiss dialog after deletion
            } else {
                Toast.makeText(getContext(), "Failed to delete section", Toast.LENGTH_SHORT).show();
                // Keep dialog open or show specific error
            }
        } catch (Exception e) {
            Log.e("SectionEditDialog", "Error deleting section", e);
            Toast.makeText(getContext(), "An error occurred while deleting", Toast.LENGTH_SHORT).show();
        } finally {
            if (dbManager != null) {
                // dbManager.close(); // DatabaseManager manages its own closing
            }
        }
    }

    // Add this method to DatabaseManager.java if it doesn't exist
    /*
    public Section getSection(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Section section = null;
        try {
            cursor = db.query(TABLE_SECTIONS, null,
                    COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                section = new Section(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close(); // Ensure db is closed
        }
        return section;
    }
     */

    // Ensure your Section class has an appropriate equals() method if you need reliable List.indexOf()
    // If using ID as the unique identifier, the equals method should compare IDs.
    /*
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return id == section.id; // Compare based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash code based on ID
    }
     */
}