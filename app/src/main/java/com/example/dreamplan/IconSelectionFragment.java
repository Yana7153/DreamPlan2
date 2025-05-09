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
    private int currentIconResId = -1;
    private Integer[] icons = {
            R.drawable.star,
            R.drawable.ic_work,
            R.drawable.ic_study,
            R.drawable.ic_shopping
            // Add all your other icons here
    };

    public interface IconSelectionListener {
        void onIconSelected(int iconResId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icon_selection, container, false);
        GridView gridView = view.findViewById(R.id.grid_icons);

        gridView.setAdapter(new IconGridAdapter());

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            if (listener != null && position >= 0 && position < icons.length) {
                int selectedIcon = icons[position];
                if (isIconValid(selectedIcon)) {
                    currentIconResId = selectedIcon;
                    listener.onIconSelected(selectedIcon);
                    safelyDismiss();
                } else {
                    showIconError();
                }
            }
        });

        return view;
    }

    private boolean isIconValid(int iconResId) {
        try {
            return ContextCompat.getDrawable(requireContext(), iconResId) != null;
        } catch (Resources.NotFoundException e) {
            return false;
        }
    }

    private void showIconError() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Icon not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void safelyDismiss() {
        try {
            if (isAdded() && getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        } catch (Exception e) {
            Log.e("IconSelection", "Error dismissing fragment", e);
        }
    }

    private class IconGridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return icons.length;
        }

        @Override
        public Integer getItem(int position) {
            return icons[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = convertView != null ? (ImageView) convertView :
                    (ImageView) LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_icon, parent, false);

            int iconResId = getItem(position);
            try {
                imageView.setImageResource(iconResId);
                // Highlight if selected
                imageView.setBackgroundResource(
                        iconResId == currentIconResId ?
                                R.drawable.icon_selected_bg :
                                R.drawable.icon_normal_bg
                );
            } catch (Resources.NotFoundException e) {
                imageView.setImageResource(R.drawable.ic_default_task);
            }
            return imageView;
        }
    }

    public void setIconSelectionListener(IconSelectionListener listener) {
        this.listener = listener;
    }

    public void setCurrentIcon(int currentIconResId) {
        this.currentIconResId = currentIconResId;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}