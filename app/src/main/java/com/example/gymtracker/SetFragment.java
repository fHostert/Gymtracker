package com.example.gymtracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.gymtracker.datastructures.Set;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetFragment extends Fragment {

    private static final String ARG_SET = "param1";

    private Set set;

    public SetFragment() {
        // Required empty public constructor
    }

    public static SetFragment newInstance(Set set) {
        SetFragment fragment = new SetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SET, set);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            set = (Set) getArguments().getSerializable(ARG_SET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_set, container, false);

        //Fill data
        ((TextView) view.findViewById(R.id.set_index_text_view)).
                setText(String.valueOf(set.getIndex()));
        if (set.getReps() != 0) {
            ((EditText)view.findViewById(R.id.weight_edit_text)).
                    setText(String.valueOf(Formatter.formatFloat(set.getWeight())));
            ((EditText)view.findViewById(R.id.reps_edit_text)).setText(set.getReps());
        }

        //((TextView)view.findViewById(R.id.last_set_text_view))
        //        .setText(((MainActivity)getActivity()).getLastSet(exercise, index));

        return view;
    }
}