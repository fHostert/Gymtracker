package com.fhostert.gymtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewTableRowFragment extends Fragment {

    private static final String ARG_TEXT = "param1";
    private static final String ARG_CENTER = "param2";

    private String text;
    private Boolean center;

    public TextViewTableRowFragment() {
        // Required empty public constructor
    }

    public static TextViewTableRowFragment newInstance(String text, boolean center) {
        TextViewTableRowFragment fragment = new TextViewTableRowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putBoolean(ARG_CENTER, center);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            text = getArguments().getString(ARG_TEXT);
            center = getArguments().getBoolean(ARG_CENTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_view_table_row, container, false);

        TextView textView =  view.findViewById(R.id.text_view_table_row);
        textView.setText(text);
        if (center) {
            textView.setGravity(Gravity.CENTER);
        }
        return view;
    }
}