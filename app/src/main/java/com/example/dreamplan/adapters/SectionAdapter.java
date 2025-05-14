package com.example.dreamplan.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dreamplan.HomeFragment;
import com.example.dreamplan.R;
import com.example.dreamplan.database.Section;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private List<Section> sectionList;
    private Context context;
    private HomeFragment homeFragment;

    public SectionAdapter(List<Section> sectionList, Context context, HomeFragment homeFragment) {
        this.sectionList = sectionList;
        this.context = context;
        this.homeFragment = homeFragment;
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view, this); // Pass 'this' (adapter instance)
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        if (sectionList == null || sectionList.isEmpty()) return;

        Section section = sectionList.get(position);
        holder.sectionName.setText(section.getName());
        holder.sectionNotes.setText(section.getNotes());

        try {
            int color = section.getSafeColor();
            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.section_background);
            if (background != null) {
                background.setColor(color);
                ViewCompat.setBackground(holder.itemView, background);
            }
        } catch (Exception e) {
            Log.e("SectionAdapter", "Error setting section color", e);

            // Fallback to default color
            GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.section_background);
            if (background != null) {
                background.setColor(ContextCompat.getColor(context, R.color.purple_200));
                ViewCompat.setBackground(holder.itemView, background);
            }
        }
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    private void showEditDeleteDialog(Section section, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Section Options")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0 && actionListener != null) {
                        actionListener.onEditSection(section);
                    } else if (which == 1 && actionListener != null) {
                        actionListener.onDeleteSection(section);
                    }
                })
                .show();
    }

    // Show a confirmation dialog before deleting the section
//    private void showDeleteConfirmationDialog(Section section, int position) {
//        new AlertDialog.Builder(context)
//                .setTitle("Delete Section")
//                .setMessage("Are you sure you want to delete this section?")
//                .setPositiveButton("Yes", (dialog, which) -> {
//                    homeFragment.deleteSection(section, position);  // Handle deletion in HomeFragment
//                })
//                .setNegativeButton("No", null)
//                .show();
//    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView sectionName;
        TextView sectionNotes;
        private SectionAdapter adapter;

        public SectionViewHolder(View itemView, SectionAdapter adapter) {
            super(itemView);
            sectionName = itemView.findViewById(R.id.sectionName);
            sectionNotes = itemView.findViewById(R.id.sectionNotes);
            this.adapter = adapter; // Initialize the adapter
            itemView.setOnClickListener(this); // Set click listener
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Section section = adapter.sectionList.get(position);
                Log.d("SectionAdapter", "Section clicked: " + section.getName());
                adapter.homeFragment.openSectionDetail(section); // Open SectionDetailFragment
            }
        }


    }

    public interface OnSectionActionListener {
        void onEditSection(Section section);
        void onDeleteSection(Section section);
    }

    private OnSectionActionListener actionListener;

    public void setOnSectionActionListener(OnSectionActionListener listener) {
        this.actionListener = listener;
    }
}
