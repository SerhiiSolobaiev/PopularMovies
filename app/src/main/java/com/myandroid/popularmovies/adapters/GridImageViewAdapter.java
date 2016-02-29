package com.myandroid.popularmovies.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.myandroid.popularmovies.entities.MovieItem;
import com.myandroid.popularmovies.R;

import java.util.ArrayList;

public class GridImageViewAdapter extends ArrayAdapter {

    private Context context;
    private int layoutResourceId;
    private ArrayList<MovieItem> data = new ArrayList<>();

    private static final int TYPE_ITEM_NORMAL = 0;
    private static final int TYPE_ITEM_FAVORITE = 1;


    public GridImageViewAdapter(Context context, int resource, ArrayList<MovieItem> newItems) {
        super(context, resource, newItems);
        this.layoutResourceId = resource;
        this.context = context;
        this.data = newItems;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (data.get(position).getIsFavorite() == 1) ? TYPE_ITEM_FAVORITE : TYPE_ITEM_NORMAL;
    }

    @Override
    public void clear() {
        data.clear();
        this.notifyDataSetChanged();
        super.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        int type = getItemViewType(position);
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();

            holder.image = (ImageView) row.findViewById(R.id.image);
            holder.markAsFavorite = (ImageView) row.findViewById(R.id.favorite);

            if (type == 1) {
                holder.markAsFavorite.setVisibility(View.VISIBLE);
            }

            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        MovieItem item = data.get(position);
        if (data.get(position).getImage() != null) {
            holder.image.setImageBitmap(item.getImage());
        }
        return row;
    }

    static class ViewHolder {
        ImageView image;
        ImageView markAsFavorite;
    }
}
