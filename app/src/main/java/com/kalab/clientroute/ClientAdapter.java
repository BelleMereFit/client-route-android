package com.kalab.clientroute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.Holder> {
    public interface Listener { void onClick(Client client); }
    private final List<Client> items; private final Listener listener;
    public ClientAdapter(List<Client> items, Listener listener) { this.items=items; this.listener=listener; }
    @NonNull public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) { return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client,parent,false)); }
    public void onBindViewHolder(@NonNull Holder h, int p) { Client c=items.get(p); h.name.setText(c.firstName+" "+c.lastName); h.phone.setText(c.phone); h.address.setText(c.address); h.time.setText(label(c.startTime)+" - "+label(c.endTime)); h.itemView.setOnClickListener(v->listener.onClick(c)); }
    private String label(String text) { return text==null || text.trim().isEmpty() ? "Not set" : text; }
    public int getItemCount() { return items.size(); }
    static class Holder extends RecyclerView.ViewHolder { TextView name,phone,address,time; Holder(@NonNull View v) { super(v);name=v.findViewById(R.id.nameText);phone=v.findViewById(R.id.phoneText);address=v.findViewById(R.id.addressText);time=v.findViewById(R.id.timeText); } }
}
