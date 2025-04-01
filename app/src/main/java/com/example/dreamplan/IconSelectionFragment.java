package com.example.dreamplan;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

public class IconSelectionFragment extends Fragment {
    private IconSelectionListener listener;

    public interface IconSelectionListener {
        void onIconSelected(int iconResId);
    }

    public void setIconSelectionListener(IconSelectionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null; // Prevent leaks
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icon_selection, container, false);
        GridView grid = view.findViewById(R.id.grid_icons);

         //Your icon resources
        Integer[] icons = {
                R.drawable.ic_work,
                R.drawable.ic_study,
                R.drawable.ic_shopping,
                // Add all your icons
        };

//        grid.setAdapter(new ArrayAdapter<Integer>(requireContext(), R.layout.item_icon, icons) {
//            @Override
//            public View getView(int position, View convertView, ViewGroup parent) {
//                ImageView imageView = new ImageView(getContext());
//                imageView.setImageResource(icons[position]);
//                imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
//                return imageView;
//            }
//        });

        grid.setAdapter(new ArrayAdapter<Integer>(requireContext(), R.layout.item_icon, icons) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView = (ImageView) convertView;
                if (imageView == null) {
                    imageView = new ImageView(getContext());
                    imageView.setLayoutParams(new GridView.LayoutParams(200, 200)); // Bigger touch area
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    imageView.setPadding(16, 16, 16, 16);
                }
                imageView.setImageResource(icons[position]);
                return imageView;
            }
        });

        grid.setOnItemClickListener((parent, view1, position, id) -> {
            if (listener != null) {
                int selectedIcon = icons[position];

                // 1. Verify the icon exists
                try {
                    getResources().getDrawable(selectedIcon);
                    listener.onIconSelected(selectedIcon);
                } catch (Resources.NotFoundException e) {
                    Log.e("ICON_ERROR", "Icon not found: " + selectedIcon, e);
                    return;
                }

                // 2. Close immediately
                getParentFragmentManager().popBackStackImmediate();
            }
        });

        return view;
    }
}