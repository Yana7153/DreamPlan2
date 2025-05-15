package com.example.dreamplan;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Looper;
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

import java.lang.ref.WeakReference;
import java.util.logging.Handler;

public class IconSelectionFragment extends Fragment {
    private static final String ARG_CURRENT_ICON = "current_icon";
    private int currentIconResId;
    private IconSelectionListener listener;

    private WeakReference<IconSelectionListener> listenerRef;


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

    public static IconSelectionFragment newInstance(int currentIconResId) {
        IconSelectionFragment fragment = new IconSelectionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CURRENT_ICON, currentIconResId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentIconResId = getArguments().getInt(ARG_CURRENT_ICON, R.drawable.star);
        }
    }

    public interface IconSelectionListener {
        void onIconSelected(int iconResId, String iconName);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridView gridView = (GridView) view;
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            IconSelectionListener listener = listenerRef != null ? listenerRef.get() : null;
            if (listener != null) {
                IconPair selected = icons[position];

                // Add debug logging
                Log.d("ICON_DEBUG", "Icon selected - ID: " + selected.resId +
                        ", Name: " + selected.name);

                // Verify the drawable exists
                try {
                    Drawable d = ContextCompat.getDrawable(requireContext(), selected.resId);
                    Log.d("ICON_DEBUG", "Drawable exists: " + (d != null));
                } catch (Exception e) {
                    Log.e("ICON_DEBUG", "Drawable error", e);
                }

                listener.onIconSelected(selected.resId, selected.name);
                getParentFragmentManager().popBackStack();
            }
        });
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
                Log.d("ICON_FLOW", "User selected icon: " + selected.resId);
            }
        });

        return gridView;
    }


    public void setIconSelectionListener(IconSelectionListener listener) {
        this.listenerRef = new WeakReference<>(listener);
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