package com.example.sata.myapplication2;

/**
 * Created by ravi on 26/09/17.
 */

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.MyViewHolder> {

    private Context context;
    private List<Uri> uriList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, description, price;
        public ImageView thumbnail;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            thumbnail = view.findViewById(R.id.thumbnail);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }


    public CartListAdapter(Context context, List<Uri> uriList) {
        this.context = context;
        this.uriList = uriList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Uri uri = uriList.get(position);
        holder.name.setText(uri.getLastPathSegment());

        Glide.with(context)
                .load(uri)
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }

    public void removeItem(int position) {
        uriList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Uri uri, int position) {
        uriList.add(position, uri);
        notifyItemInserted(position);
    }
}
