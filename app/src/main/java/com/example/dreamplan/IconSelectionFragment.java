package com.example.dreamplan;

import android.os.Bundle;
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

        grid.setAdapter(new ArrayAdapter<Integer>(requireContext(), R.layout.item_icon, icons) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(icons[position]);
                imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
                return imageView;
            }
        });

        grid.setAdapter(new ArrayAdapter<Integer>(requireContext(), R.layout.item_icon, icons) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView = (ImageView) (convertView != null ? convertView :
                        LayoutInflater.from(getContext()).inflate(R.layout.item_icon, parent, false));

                imageView.setImageResource(icons[position]);
                return imageView;
            }
        });

        return view;
    }
}