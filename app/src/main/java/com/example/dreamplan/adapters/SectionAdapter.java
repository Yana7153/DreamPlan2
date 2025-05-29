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

import java.util.Collections;
import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {
    private List<Section> sectionList;
    private Context context;
    private OnSectionActionListener actionListener;
    private List<Section> sections;

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

        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditSection(section);
                return true;
            }
            return false;
        });
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
            }
        }
    }
}