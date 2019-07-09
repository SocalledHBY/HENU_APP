package com.hby.henu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hby.henu.R;
import com.hby.henu.model.ScoreQueryItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScoreQueryItemAdapter extends ArrayAdapter {

    private final int resourceId;

    public ScoreQueryItemAdapter(Context context, int textViewResourceId, List<ScoreQueryItem> items) {
        super(context, textViewResourceId, items);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ScoreQueryItem item = (ScoreQueryItem) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);

        TextView courseTV = (TextView) view.findViewById(R.id.courseTV);
        TextView scoreTV = (TextView) view.findViewById(R.id.scoreTV);

        courseTV.setText(item.getCourse());
        scoreTV.setText(String.valueOf(item.getScore()));

        return view;
    }
}
