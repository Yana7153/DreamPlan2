package com.example.dreamplan.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        if (sectionList == null || sectionList.isEmpty()) {
            return; // Do nothing if the list is null or empty
        }
        Section section = sectionList.get(position);
        holder.sectionName.setText(section.getName());
        holder.sectionNotes.setText(section.getNotes());

        // Use a color from your palette
        holder.itemView.setBackgroundColor(Color.parseColor("#CCE1F2")); // Light blue
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    // Show the Edit/Delete dialog
    private void showEditDeleteDialog(Section section, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Options")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        // Edit option
                        homeFragment.showEditSectionDialog(section); // Open edit dialog in HomeFragment
                    } else if (which == 1) {
                        // Delete option
                        showDeleteConfirmationDialog(section, position); // Confirm before deleting
                    }
                })
                .show();
    }

    // Show a confirmation dialog before deleting the section
    private void showDeleteConfirmationDialog(Section section, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Section")
                .setMessage("Are you sure you want to delete this section?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    homeFragment.deleteSection(section, position);  // Handle deletion in HomeFragment
                })
                .setNegativeButton("No", null)
                .show();
    }

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
}
