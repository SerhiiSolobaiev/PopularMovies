package com.myandroid.popularmovies.adapters;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myandroid.popularmovies.R;
import com.myandroid.popularmovies.entities.TrailerItem;

import java.util.ArrayList;

public class ListTrailerAdapter extends ArrayAdapter {

    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();

    public ListTrailerAdapter(Context context, int resource, ArrayList objects) {
        super(context, resource, objects);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.trailerName= (TextView) row.findViewById(R.id.textView_trailerName);
            holder.imageYoutube = (ImageView) row.findViewById(R.id.imageView_Youtube);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        TrailerItem item = (TrailerItem) data.get(position);
        holder.trailerName.setText(item.getNameTrailer());
        holder.imageYoutube.setImageResource(R.drawable.youtube_play);
        return row;
    }
    static class ViewHolder {
        TextView trailerName;
        ImageView imageYoutube;
    }
}
