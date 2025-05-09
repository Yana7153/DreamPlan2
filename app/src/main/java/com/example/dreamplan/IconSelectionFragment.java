package com.example.dreamplan;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class IconSelectionFragment extends Fragment {
    private IconSelectionListener listener;
    private int currentIconResId = R.drawable.star;

    private Integer[] icons = {
            R.drawable.star,
            R.drawable.ic_work,
            R.drawable.ic_study,
            R.drawable.ic_shopping
            // Add all your icons
    };

    public interface IconSelectionListener {
        void onIconSelected(int iconResId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        GridView gridView = (GridView) inflater.inflate(R.layout.fragment_icon_selection, container, false);

        gridView.setAdapter(new ArrayAdapter<Integer>(requireContext(), R.layout.item_icon, icons) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                ImageView imageView = (ImageView) (convertView != null ? convertView :
                        LayoutInflater.from(getContext()).inflate(R.layout.item_icon, parent, false));

                // Just set the icon, no background or selection effect
                imageView.setImageResource(icons[position]);
                imageView.setBackground(null);  // Remove any background

                return imageView;
            }
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                listener.onIconSelected(icons[position]);
                getParentFragmentManager().popBackStack();
            }
        });

        return gridView;
    }

    public void setIconSelectionListener(IconSelectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setCurrentIcon(int iconResId) {
        if (iconResId != 0) { // Only set if valid resource ID
            this.currentIconResId = iconResId;
        }
    }
}