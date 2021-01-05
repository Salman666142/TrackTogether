package com.technicallskillz.tracktogether.Activities;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.technicallskillz.tracktogether.R;

public class LocationViewHolder extends RecyclerView.ViewHolder {
    TextView  username;
    TextView email;
    public LocationViewHolder(@NonNull View itemView) {
        super(itemView);
        username=itemView.findViewById(R.id.username);
        email=itemView.findViewById(R.id.LatLong);
    }
}
