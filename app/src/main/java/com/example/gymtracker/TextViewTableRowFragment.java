package com.example.gymtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TextViewTableRowFragment extends Fragment {

    private static final String ARG_TEXT = "param1";

    private String text;

    public TextViewTableRowFragment() {
        // Required empty public constructor
    }

    public static TextViewTableRowFragment newInstance(String text) {
        TextViewTableRowFragment fragment = new TextViewTableRowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            text = getArguments().getString(ARG_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_text_view_table_row, container, false);

        ((TextView) view.findViewById(R.id.text_view_table_row)).setText(text);
        return view;
    }
}