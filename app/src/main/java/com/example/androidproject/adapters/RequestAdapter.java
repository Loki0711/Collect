package com.example.androidproject.adapters;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidproject.DetailActivity;
import com.example.androidproject.databinding.ItemRequestHistoryBinding;
import com.example.androidproject.models.Garbage;
import com.example.androidproject.utils.AppConstant;
import com.google.android.gms.common.internal.ServiceSpecificExtraArgs;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RequestAdapter extends ListAdapter<Garbage, RequestViewHolder> {

    private Geocoder geocoder;
    private String userType = "";

    public RequestAdapter(Geocoder geocoder, String userType) {
        super(new GarbageDiffUtil());
        this.geocoder = geocoder;
        this.userType = userType;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder(ItemRequestHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Garbage garbageRequest = getItem(position);

        holder.binding.tvName.setText(garbageRequest.getGarbageType());
        holder.binding.tvAddedBy.setText("Added By: " + garbageRequest.getAddedBy());

        holder.binding.tvDate.setText(getFormattedDate(garbageRequest.getAddedOn()));
        holder.binding.tvTime.setText(getTime(garbageRequest.getAddedOn()));

        holder.binding.tvStatus.setText("Status: " + garbageRequest.getStatus());

        if (garbageRequest.getPickedOn() != null) {
            holder.binding.tvPickedOn.setText("Picked on: " + getTime(garbageRequest.getPickedOn()) + " , " + getFormattedDate(garbageRequest.getPickedOn()));
        }


        if (garbageRequest.getStatus().equals(AppConstant.REQUEST_STATUS_ACCEPTED))
            Log.e("CheckPoint", "Accepted Status By: " + garbageRequest.getAcceptedBy());

        try {
            List<Address> address = geocoder.getFromLocation(garbageRequest.getLat(), garbageRequest.getLng(), 4);
            if (!address.isEmpty()) {
                holder.binding.tvAddress.setText(address.get(0).getAddressLine(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userType.equals(AppConstant.USERTYPE_COLLECTOR)) {
            holder.binding.getRoot().setOnClickListener(v -> {
                DetailActivity.startActivity(holder.binding.getRoot().getContext(), garbageRequest);
            });
        }
    }

    private String getFormattedDate(Date date) {
        return new SimpleDateFormat("dd/MMM/yyyy").format(date);
    }

    private String getTime(Date date) {
        return new SimpleDateFormat("H:mm").format(date);
    }


}

class GarbageDiffUtil extends ItemCallback<Garbage> {

    @Override
    public boolean areItemsTheSame(@NonNull Garbage oldItem, @NonNull Garbage newItem) {
        return oldItem.getId().equals(newItem.getId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull Garbage oldItem, @NonNull Garbage newItem) {
        return oldItem.equals(newItem);
    }
}


class RequestViewHolder extends RecyclerView.ViewHolder {

    public ItemRequestHistoryBinding binding;

    public RequestViewHolder(ItemRequestHistoryBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

}
