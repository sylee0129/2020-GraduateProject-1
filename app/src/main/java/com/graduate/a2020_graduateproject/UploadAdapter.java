package com.graduate.a2020_graduateproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.graduate.a2020_graduateproject.bottomNavigation.MapInfoViewHolder;

import java.util.ArrayList;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadViewHolder> {

    private ArrayList<Upload> uploadItems = new ArrayList<>();

    private  String selected_room_id;

    public UploadAdapter(String selected_room_id){
        this.selected_room_id = selected_room_id;
    }
    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // RecyclerView가 어댑터에 연결된 후 최초로 ViewHolder 생성
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image_item, parent, false);
        return new UploadViewHolder(v, selected_room_id);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {  // 실제로 각 아이템들에 대한 매칭을 시켜주는 역할

        Glide.with(holder.image)
                .load(uploadItems.get(position).getImageUrl())
                .error(R.drawable.kakao_default_profile_image)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return uploadItems.size();
    }

    public void clear() {
        uploadItems.clear();
    }

    public void remove(int position) {
        uploadItems.remove(uploadItems.get(position));
    }

    public void add(Upload upload) {
        uploadItems.add(upload);
    }

    public class UploadViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public String selected_room_id;

        public UploadViewHolder(@NonNull View itemView, String selected_room_id) {
            super(itemView);
            this.selected_room_id = selected_room_id;

            image = itemView.findViewById(R.id.image);

            // 아이템 클릭 이벤트 처리
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    Log.v("gallery", "context = " + v.getContext());

                    Intent intent = new Intent(v.getContext(), GalleryImageViewerActivity.class);

                    int position = getAdapterPosition();

                    if(position != RecyclerView.NO_POSITION) {
                        intent.putExtra("clicked image", uploadItems.get(position).getImageUrl());
                        Log.v("gallery", "image url = " + uploadItems.get(position).getImageUrl());

                        intent.putExtra("selected room id", selected_room_id);

                        context.startActivity(intent);
                    }

                }
            });
        }
    }
}
