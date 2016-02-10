package com.myandroid.popularmovies.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.myandroid.popularmovies.entities.ImageItem;
import com.myandroid.popularmovies.R;

import java.util.ArrayList;

public class GridImageViewAdapter extends ArrayAdapter {

    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();

    public GridImageViewAdapter(Context context, int resource, ArrayList data) {
        super(context, resource, data);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ImageView image;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            image = (ImageView) row.findViewById(R.id.image);
            row.setTag(image);
        } else {
            image = (ImageView) row.getTag();
        }

        ImageItem item = (ImageItem) data.get(position);
        image.setImageBitmap(item.getImage());
        return row;
    }
}
