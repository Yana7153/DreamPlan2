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


    private final IconPair[] icons = {
            new IconPair(R.drawable.star, "star"),
            new IconPair(R.drawable.ic_work, "ic_work"),
            new IconPair(R.drawable.ic_study, "ic_study"),
            new IconPair(R.drawable.ic_shopping, "ic_shopping")
    };

    private static class IconPair {
        final int resId;
        final String name;

        IconPair(int resId, String name) {
            this.resId = resId;
            this.name = name;
        }
    }

    public interface IconSelectionListener {
        void onIconSelected(int iconResId, String iconName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        GridView gridView = (GridView) inflater.inflate(R.layout.fragment_icon_selection, container, false);

        gridView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return icons.length;
            }

            @Override
            public Object getItem(int position) {
                return icons[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                ImageView imageView = (ImageView) (convertView != null ? convertView :
                        LayoutInflater.from(getContext()).inflate(R.layout.item_icon, parent, false));

                imageView.setImageResource(icons[position].resId);
                return imageView;
            }
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (listener != null) {
                IconPair selected = icons[position];
                listener.onIconSelected(selected.resId, selected.name);
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
        listener = null;
        super.onDetach();
    }

    public void setCurrentIcon(int iconResId) {
        if (iconResId != 0) {
            this.currentIconResId = iconResId;
        }
    }
}