package com.recognize.match.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.recognize.match.R;
import com.recognize.match.bean.ThemeRowBean;

import java.util.List;

/**
 * This Adapter is for the AlbumBean Activity`
 */
public class AlbumsAdapter extends ArrayAdapter<ThemeRowBean> {

	private Context context;
    int layoutResourceId;
    private List<ThemeRowBean> items;
    private Resources res;

    public AlbumsAdapter(Context context, int layoutResourceId, List<ThemeRowBean> list) {
        super(context, layoutResourceId, list);
        this.layoutResourceId = layoutResourceId;
		this.context = context;
        this.items = list;
        this.res = context.getResources();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if(v == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) v.findViewById(R.id.grid_image);
            holder.title = (TextView) v.findViewById(R.id.grid_text);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        ThemeRowBean item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.image.setImageBitmap(item.getImage());
        return v;
	}

    static class ViewHolder
    {
        TextView title;
        ImageView image;
    }

}
