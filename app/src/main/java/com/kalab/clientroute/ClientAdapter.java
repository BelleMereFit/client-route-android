package com.kalab.clientroute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.Holder> {

    public interface ActionListener {
        void onDirections(Client client);
        void onCall(Client client);
        void onEdit(Client client);
        void onDelete(Client client);
    }

    private final List<Client> items;
    private final ActionListener listener;

    public ClientAdapter(List<Client> items, ActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Client c = items.get(position);
        h.name.setText(c.getFullName());
        h.phone.setText(c.phone);
        h.address.setText(c.getFullAddress());
        h.time.setText(c.getTimeRangeLabel());
        h.directionsButton.setOnClickListener(v -> listener.onDirections(c));
        h.callButton.setOnClickListener(v -> listener.onCall(c));
        h.editButton.setOnClickListener(v -> listener.onEdit(c));
        h.deleteButton.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, phone, address, time;
        Button directionsButton, callButton, editButton, deleteButton;

        Holder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.nameText);
            phone = v.findViewById(R.id.phoneText);
            address = v.findViewById(R.id.addressText);
            time = v.findViewById(R.id.timeText);
            directionsButton = v.findViewById(R.id.directionsButton);
            callButton = v.findViewById(R.id.callButton);
            editButton = v.findViewById(R.id.editButton);
            deleteButton = v.findViewById(R.id.deleteButton);
        }
    }
}
