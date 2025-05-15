package com.example.dreamplan.adapters;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.HomeFragment;
import com.example.dreamplan.R;
import com.example.dreamplan.database.Section;

import java.util.List;

//public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {
//
//    private List<Section> sectionList;
//    private Context context;
//    private HomeFragment homeFragment;
//    private OnSectionActionListener actionListener;
//
//    public interface OnSectionActionListener {
//        void onEditSection(Section section);
//        void onDeleteSection(Section section);
//    }
//
//    public SectionAdapter(List<Section> sectionList, Context context, HomeFragment homeFragment) {
//        this.sectionList = sectionList;
//        this.context = context;
//        this.homeFragment = homeFragment;
//    }
//
//    public void setOnSectionActionListener(OnSectionActionListener listener) {
//        this.actionListener = listener;
//    }
//
//    @Override
//    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_section, parent, false);
//        return new SectionViewHolder(view, this);
//    }
//
//    @Override
//    public void onBindViewHolder(SectionViewHolder holder, int position) {
//        if (sectionList == null || sectionList.isEmpty()) return;
//
//        Section section = sectionList.get(position);
//        holder.sectionName.setText(section.getName());
//        holder.sectionNotes.setText(section.getNotes());
//
//        try {
//            int color = section.getSafeColor();
//            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.section_background);
//            if (background != null) {
//                background.setColor(color);
//                ViewCompat.setBackground(holder.itemView, background);
//            }
//        } catch (Exception e) {
//            Log.e("SectionAdapter", "Error setting section color", e);
//
//            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.section_background);
//            if (background != null) {
//                background.setColor(ContextCompat.getColor(context, R.color.purple_200));
//                ViewCompat.setBackground(holder.itemView, background);
//            }
//        }
//
//        holder.itemView.setOnLongClickListener(v -> {
//            showSectionOptionsDialog(section);
//            return true;
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return sectionList.size();
//    }
//
////    private void showEditDeleteDialog(Section section, int position) {
////        new AlertDialog.Builder(context)
////                .setTitle("Section Options")
////                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
////                    if (which == 0 && actionListener != null) {
////                        actionListener.onEditSection(section);
////                    } else if (which == 1 && actionListener != null) {
////                        actionListener.onDeleteSection(section);
////                    }
////                })
////                .show();
////    }
//
//    // Show a confirmation dialog before deleting the section
////    private void showDeleteConfirmationDialog(Section section, int position) {
////        new AlertDialog.Builder(context)
////                .setTitle("Delete Section")
////                .setMessage("Are you sure you want to delete this section?")
////                .setPositiveButton("Yes", (dialog, which) -> {
////                    homeFragment.deleteSection(section, position);  // Handle deletion in HomeFragment
////                })
////                .setNegativeButton("No", null)
////                .show();
////    }
//
//    private void showSectionOptionsDialog(Section section) {
//        new AlertDialog.Builder(context)
//                .setTitle("Section Options")
//                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
//                    if (which == 0) {
//                        // Edit selected
//                        showEditSectionDialog(section);
//                    } else {
//                        // Delete selected
//                        showDeleteConfirmationDialog(section);
//                    }
//                })
//                .show();
//    }
//
//
//
//    private void highlightSelectedColor(LinearLayout colorPicker, int color) {
//        for (int i = 0; i < colorPicker.getChildCount(); i++) {
//            ImageView colorCircle = (ImageView) colorPicker.getChildAt(i);
//            GradientDrawable drawable = (GradientDrawable) colorCircle.getBackground();
//            if (drawable.getColor().getDefaultColor() == color) {
//                colorCircle.setBackgroundResource(R.drawable.circle_selected_border);
//            } else {
//                colorCircle.setBackgroundResource(getCircleDrawableId(i + 1));
//            }
//        }
//    }
//
//    private int getSelectedColor(LinearLayout colorPicker) {
//        for (int i = 0; i < colorPicker.getChildCount(); i++) {
//            ImageView colorCircle = (ImageView) colorPicker.getChildAt(i);
//            if (colorCircle.getBackground() instanceof GradientDrawable) {
//                GradientDrawable drawable = (GradientDrawable) colorCircle.getBackground();
//                return drawable.getColor().getDefaultColor();
//            }
//        }
//        return ContextCompat.getColor(context, R.color.purple_500);
//    }
//
//    private int getColorPosition(int color) {
//        switch(colorResId) {
//            case R.drawable.circle_background_1: return 1;
//            case R.drawable.circle_background_2: return 2;
//            case R.drawable.circle_background_3: return 3;
//            case R.drawable.circle_background_4: return 4;
//            case R.drawable.circle_background_5: return 5;
//            case R.drawable.circle_background_6: return 6;
//            case R.drawable.circle_background_7: return 7;
//            default: return 1;
//        }
//    }
//
//
//    private int getCircleDrawableId(int position) {
//        switch (position) {
//            case 1: return R.drawable.circle_background_1;
//            case 2: return R.drawable.circle_background_2;
//            case 3: return R.drawable.circle_background_3;
//            case 4: return R.drawable.circle_background_4;
//            case 5: return R.drawable.circle_background_5;
//            case 6: return R.drawable.circle_background_6;
//            case 7: return R.drawable.circle_background_7;
//            default: return R.drawable.circle_background_1;
//        }
//    }
//
//
//
//    public static class SectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        TextView sectionName;
//        TextView sectionNotes;
//        private SectionAdapter adapter;
//
//        public SectionViewHolder(View itemView, SectionAdapter adapter) {
//            super(itemView);
//            sectionName = itemView.findViewById(R.id.sectionName);
//            sectionNotes = itemView.findViewById(R.id.sectionNotes);
//            this.adapter = adapter; // Initialize the adapter
//            itemView.setOnClickListener(this); // Set click listener
//        }
//
//        @Override
//        public void onClick(View v) {
//            int position = getAdapterPosition();
//            if (position != RecyclerView.NO_POSITION) {
//                Section section = adapter.sectionList.get(position);
//                Log.d("SectionAdapter", "Section clicked: " + section.getName());
//                adapter.homeFragment.openSectionDetail(section);
//            }
//        }
//
//        public void bind(Section section) {
//            sectionName.setText(section.getName());
//            sectionNotes.setText(section.getNotes());
//
//            try {
//                int color = section.getSafeColor();
//                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(itemView.getContext(), R.drawable.section_background);
//                if (background != null) {
//                    background.setColor(color);
//                    ViewCompat.setBackground(itemView, background);
//                }
//            } catch (Exception e) {
//                Log.e("SectionAdapter", "Error setting section color", e);
//                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(itemView.getContext(), R.drawable.section_background);
//                if (background != null) {
//                    background.setColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_200));
//                    ViewCompat.setBackground(itemView, background);
//                }
//            }
//        }
//    }
//}


public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {
    private List<Section> sectionList;
    private Context context;
    private OnSectionActionListener actionListener;


    public interface OnSectionActionListener {
        void onEditSection(Section section);
        void onDeleteSection(Section section);
        void onOpenSection(Section section);
    }

    public SectionAdapter(List<Section> sectionList, Context context) {
        this.sectionList = sectionList;
        this.context = context;
    }

    public void setOnSectionActionListener(OnSectionActionListener listener) {
        this.actionListener = listener;
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        Section section = sectionList.get(position);
        holder.bind(section);

        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onOpenSection(section);
            }
        });

// Long click goes straight to edit
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditSection(section);
                return true; // Consume the event
            }
            return false;
        });
    }

    private void showSectionOptionsDialog(Section section) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(section.getName())
                .setItems(new CharSequence[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        ((OnSectionActionListener) context).onEditSection(section);
                    } else {
                        ((OnSectionActionListener) context).onDeleteSection(section);
                    }
                });
        builder.show();
    }
    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionName;
        TextView sectionNotes;

        public SectionViewHolder(View itemView) {
            super(itemView);
            sectionName = itemView.findViewById(R.id.sectionName);
            sectionNotes = itemView.findViewById(R.id.sectionNotes);
        }

        public void bind(Section section) {
            sectionName.setText(section.getName());
            sectionNotes.setText(section.getNotes());

            try {
                int color = section.getSafeColor();
                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(
                        itemView.getContext(), R.drawable.section_background);
                if (background != null) {
                    background.setColor(color);
                    ViewCompat.setBackground(itemView, background);
                }
            } catch (Exception e) {
                Log.e("SectionAdapter", "Error setting color", e);
                // Fallback color
            }
        }
    }
}