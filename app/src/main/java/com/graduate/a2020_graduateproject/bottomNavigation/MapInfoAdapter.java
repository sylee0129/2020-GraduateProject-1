package com.graduate.a2020_graduateproject.bottomNavigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.graduate.a2020_graduateproject.PlanItem;
import com.graduate.a2020_graduateproject.PlanViewHolder;
import com.graduate.a2020_graduateproject.R;

import java.util.ArrayList;
import java.util.Collections;

public class MapInfoAdapter extends RecyclerView.Adapter<MapInfoViewHolder>
        implements MapInfoItemTouchHelperCallback.onItemMoveListener {

    private String selected_room_id;
    private ArrayList<MapInfoItem> mapInfoItems = new ArrayList<>();

    public MapInfoAdapter(String selected_room_id){
        this.selected_room_id = selected_room_id;
    }

    @NonNull
    @Override
    public MapInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(
                parent.getContext()
        ).inflate(R.layout.map_info_list, parent, false);
        return new MapInfoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MapInfoViewHolder holder, int position) {

        MapInfoItem item = mapInfoItems.get(position);

        // holder.setPlace_text(item.getName()); // 나중에 name 집어넣기
        holder.setPlace_text(item.getKey());

    }

    @Override
    public int getItemCount() {
        return mapInfoItems.size();
    }

    public void add(MapInfoItem newItem){
        mapInfoItems.add(newItem);
    }

    public void clear(){
        mapInfoItems.clear();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mapInfoItems, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mapInfoItems, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);


        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        return;
    }
}