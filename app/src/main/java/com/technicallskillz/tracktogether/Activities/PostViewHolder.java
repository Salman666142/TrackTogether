package com.technicallskillz.tracktogether.Activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technicallskillz.tracktogether.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder {
    CircleImageView profileImageView;
    TextView username;
    ImageView effected;
    TextView email;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);

        profileImageView=itemView.findViewById(R.id.profileImageView);
        username=itemView.findViewById(R.id.username);
        effected=itemView.findViewById(R.id.effected);
        email=itemView.findViewById(R.id.email);

    }
}
