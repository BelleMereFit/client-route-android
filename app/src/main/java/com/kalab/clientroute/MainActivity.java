package com.kalab.clientroute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

public class MainActivity extends AppCompatActivity {
    private static final int REQ_LOCATION=2001;
    private LinearLayout listContainer; private ScrollView formContainer; private TextView formTitle; private EditText first,last,phone,address,start,end; private final List<Client> clients=new ArrayList<>(); private ClientAdapter adapter; private String editingId;
    @Override public void onCreate(Bundle state) { super.onCreate(state);setContentView(R.layout.activity_main);setSupportActionBar((MaterialToolbar)findViewById(R.id.toolbar));listContainer=findViewById(R.id.listContainer);formContainer=findViewById(R.id.formContainer);formTitle=findViewById(R.id.formTitle);first=findViewById(R.id.firstNameInput);last=findViewById(R.id.lastNameInput);phone=findViewById(R.id.phoneInput);address=findViewById(R.id.addressInput);start=findViewById(R.id.startInput);end=findViewById(R.id.endInput);clients.addAll(ClientStorage.load(this));adapter=new ClientAdapter(clients,this::showClientDialog);RecyclerView rv=findViewById(R.id.recyclerView);rv.setLayoutManager(new LinearLayoutManager(this));rv.setAdapter(adapter);((Button)findViewById(R.id.addButton)).setOnClickListener(v->showForm(null));((Button)findViewById(R.id.saveButton)).setOnClickListener(v->saveClient());((Button)findViewById(R.id.cancelButton)).setOnClickListener(v->showList());requestLocation();showList(); }
    @Override public boolean onCreateOptionsMenu(Menu m){m.add(0,1,0,"Add Client");return true;} @Override public boolean onOptionsItemSelected(MenuItem i){if(i.getItemId()==1){showForm(null);return true;}return super.onOptionsItemSelected(i);}
    private void requestLocation(){if(Build.VERSION.SDK_INT>=23&&ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);}
    private void showList(){setTitle("Client Route");listContainer.setVisibility(View.VISIBLE);formContainer.setVisibility(View.GONE);adapter.notifyDataSetChanged();}
    private void showForm(Client c){listContainer.setVisibility(View.GONE);formContainer.setVisibility(View.VISIBLE);if(c==null){setTitle("Add Client");formTitle.setText("Add Client");editingId=null;first.setText("");last.setText("");phone.setText("");address.setText("");start.setText("");end.setText("");}else{setTitle("Edit Client");formTitle.setText("Edit Client");editingId=c.id;first.setText(c.firstName);last.setText(c.lastName);phone.setText(c.phone);address.setText(c.address);start.setText(c.startTime);end.setText(c.endTime);}}
    private void saveClient(){String f=first.getText().toString().trim(),l=last.getText().toString().trim(),p=phone.getText().toString().trim(),a=address.getText().toString().trim(),s=start.getText().toString().trim(),e=end.getText().toString().trim();if(TextUtils.isEmpty(f)||TextUtils.isEmpty(l)||TextUtils.isEmpty(p)||TextUtils.isEmpty(a)){Toast.makeText(this,"Please enter name, phone, and address.",Toast.LENGTH_LONG).show();return;}if(editingId==null)clients.add(0,new Client(String.valueOf(System.currentTimeMillis()),f,l,p,a,s,e));else for(int i=0;i<clients.size();i++)if(clients.get(i).id.equals(editingId)){clients.set(i,new Client(editingId,f,l,p,a,s,e));break;}ClientStorage.save(this,clients);showList();}
    private void showClientDialog(Client c){new AlertDialog.Builder(this).setTitle(c.firstName+" "+c.lastName).setMessage("Phone: "+c.phone+"\n\nAddress: "+c.address+"\n\nVisit window: "+val(c.startTime)+" to "+val(c.endTime)).setItems(new String[]{"Open Directions","Edit Client","Delete Client"},(d,w)->{if(w==0)openDirections(c.address);else if(w==1)showForm(c);else deleteClient(c.id);}).setNegativeButton("Close",null).show();}
    private String val(String s){return s==null||s.trim().isEmpty()?"Not set":s;} private void deleteClient(String id){for(int i=0;i<clients.size();i++)if(clients.get(i).id.equals(id)){clients.remove(i);break;}ClientStorage.save(this,clients);showList();}
    private void openDirections(String destination){Intent map=new Intent(Intent.ACTION_VIEW,Uri.parse("google.navigation:q="+Uri.encode(destination)));map.setPackage("com.google.android.apps.maps");if(map.resolveActivity(getPackageManager())!=null)startActivity(map);else startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/?api=1&query="+Uri.encode(destination))));}
}
