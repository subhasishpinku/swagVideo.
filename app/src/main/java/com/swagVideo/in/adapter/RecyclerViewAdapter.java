package com.swagVideo.in.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    LayoutInflater layoutInflater;
    List<T> dataSource;
    int resource;
    Mapper<T> mapper;

    public interface Mapper<T> {
        void map(com.swagVideo.in.adapter.RecyclerViewAdapter.ViewHolder viewHolder, T source);
    }

    public RecyclerViewAdapter(Context context, int resource, List<T> dataSource) {
        layoutInflater = LayoutInflater.from(context);
        this.dataSource = dataSource;
        this.resource = resource;
    }

    public void setMapper(Mapper<T> mapper) {
        this.mapper = mapper;
    }


    public void filterList(List<T> dataSource) {
        this.dataSource = dataSource;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(resource, null, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mapper != null) mapper.map((ViewHolder) holder, dataSource.get(position));

    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        HashMap<Integer, View> value;

        public ViewHolder(View itemView) {
            super(itemView);
            value = new HashMap<>();
            setReferences(itemView);
        }

        private void setReferences(View v) {
            if (v.getId() != View.NO_ID) value.put(v.getId(), v);

            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;

                if (vg.getChildCount() > 0) {
                    for (int i = 0; i < vg.getChildCount(); i++) setReferences(vg.getChildAt(i));
                }
            }
        }

        public View getView(int resource) {
            return value.get(resource);
        }

        public void setView(int resource, View view) {
            this.value.put(resource, view);
        }
    }

}

