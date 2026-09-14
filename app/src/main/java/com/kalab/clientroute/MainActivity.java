package com.kalab.clientroute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ClientAdapter.ActionListener {
    private static final int REQ_LOCATION = 2001;

    private final List<Client> allClients = new ArrayList<>();
    private final List<Client> visibleClients = new ArrayList<>();
    private ClientAdapter adapter;
    private EditText searchInput;
    private View emptyState;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));

        recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);
        emptyState = findViewById(R.id.emptyState);

        adapter = new ClientAdapter(visibleClients, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.addButton).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditClientActivity.class)));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { applyFilter(s.toString()); }
        });

        requestLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        allClients.clear();
        allClients.addAll(ClientStorage.load(this));
        applyFilter(searchInput.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Add Client");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, AddEditClientActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION);
        }
    }

    private void applyFilter(String query) {
        visibleClients.clear();
        String q = query == null ? "" : query.trim().toLowerCase(Locale.US);
        for (Client c : allClients) {
            if (q.isEmpty() || matches(c, q)) visibleClients.add(c);
        }
        adapter.notifyDataSetChanged();
        emptyState.setVisibility(visibleClients.isEmpty() ? View.VISIBLE : View.GONE);
        ((TextView) emptyState.findViewById(R.id.emptyStateText)).setText(
                allClients.isEmpty()
                        ? "No clients yet. Tap Add Client to create your first one."
                        : "No clients match your search.");
        recyclerView.setVisibility(visibleClients.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private boolean matches(Client c, String q) {
        return contains(c.getFullName(), q) || contains(c.phone, q) || contains(c.city, q)
                || contains(c.getFullAddress(), q) || contains(c.state, q) || contains(c.zip, q);
    }

    private boolean contains(String field, String q) {
        return field != null && field.toLowerCase(Locale.US).contains(q);
    }

    @Override
    public void onDirections(Client c) {
        String destination = c.getFullAddress();
        if (destination.trim().isEmpty()) {
            Toast.makeText(this, "This client has no address saved yet.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent map = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + Uri.encode(destination)));
        map.setPackage("com.google.android.apps.maps");
        if (map.resolveActivity(getPackageManager()) != null) {
            startActivity(map);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(destination))));
        }
    }

    @Override
    public void onCall(Client c) {
        if (c.phone == null || c.phone.trim().isEmpty()) {
            Toast.makeText(this, "This client has no phone number saved yet.", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(c.phone))));
    }

    @Override
    public void onEdit(Client c) {
        Intent intent = new Intent(this, AddEditClientActivity.class);
        intent.putExtra(AddEditClientActivity.EXTRA_CLIENT_ID, c.id);
        startActivity(intent);
    }

    @Override
    public void onDelete(Client c) {
        new AlertDialog.Builder(this)
                .setTitle("Delete client")
                .setMessage("Delete " + c.getFullName() + "? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    for (int i = 0; i < allClients.size(); i++) {
                        if (allClients.get(i).id.equals(c.id)) {
                            allClients.remove(i);
                            break;
                        }
                    }
                    ClientStorage.save(this, allClients);
                    applyFilter(searchInput.getText().toString());
                    Toast.makeText(this, "Client deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
