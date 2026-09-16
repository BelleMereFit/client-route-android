package com.kalab.clientroute;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ScheduleImportActivity extends Activity {
    SchedulePdf.Schedule schedule;
    LinearLayout root,list;
    Spinner crews;
    String selectedDate="";
    boolean applying=false;
    int dp(int n){return (int)(getResources().getDisplayMetrics().density*n);}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(16),dp(8),dp(16),dp(8));return l;}
    void text(LinearLayout l,String s,int size){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setPadding(0,dp(6),0,dp(6));l.addView(v);}
    Button button(LinearLayout l,String s,View.OnClickListener click){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setMinHeight(dp(52));l.addView(b);b.setOnClickListener(click);return b;}
    void error(String message){new AlertDialog.Builder(this).setTitle("Import not completed").setMessage(message).setPositiveButton("OK",(d,w)->finish()).show();}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);root=column();setContentView(root);text(root,"Import schedule",28);
        Uri uri=getIntent().getData();
        if(Intent.ACTION_SEND.equals(getIntent().getAction()))uri=getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if(b!=null&&b.containsKey("uri"))uri=Uri.parse(b.getString("uri"));
        if(uri!=null)load(uri);
        else if(b==null){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/pdf");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,9);}
        else button(root,"Choose schedule PDF",v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/pdf");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,9);});
    }
    Uri source;
    @Override protected void onSaveInstanceState(Bundle b){if(source!=null)b.putString("uri",source.toString());super.onSaveInstanceState(b);}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==9){if(result==RESULT_OK&&data!=null)load(data.getData());else finish();}}
    void load(Uri uri){
        source=uri;text(root,"Reading PDF on this tablet…",18);
        new Thread(()->{
            try(InputStream in=getContentResolver().openInputStream(uri)){
                // Bound untrusted attachment size before parsing.
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
                while((n=in.read(buffer))!=-1){if(bytes.size()+n>10*1024*1024)throw new IOException("Choose a PDF smaller than 10 MB.");bytes.write(buffer,0,n);}
                SchedulePdf.Schedule parsed=SchedulePdf.read(this,new ByteArrayInputStream(bytes.toByteArray()));
                runOnUiThread(()->{if(!isFinishing()){schedule=parsed;selectedDate=parsed.date;preview();}});
            }catch(Exception e){runOnUiThread(()->{if(!isFinishing())error(e.getMessage()==null?"The PDF could not be read.":e.getMessage());});}
        },"SchedulePDF").start();
    }
    void preview(){
        root.removeAllViews();text(root,"Review schedule",26);
        text(root,"Choose the crew. Uncheck any stops you do not want. The printed order is preserved.",16);
        button(root,"Job date: "+selectedDate,v->{
            String[] p=selectedDate.split("-");
            new DatePickerDialog(this,(view,y,m,d)->{selectedDate=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);((Button)v).setText("Job date: "+selectedDate);},
                Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2])).show();
        });
        String today=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
        if(!selectedDate.equals(today))text(root,"This PDF is dated "+schedule.date+", not today. Change the job date above if appropriate.",16);
        crews=new Spinner(this);ArrayAdapter<SchedulePdf.Crew> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,schedule.crews);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);crews.setAdapter(a);root.addView(crews);
        ScrollView scroll=new ScrollView(this);list=column();scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        button(root,"Import selected stops",v->confirm());
        button(root,"Cancel",v->finish());
        crews.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){showStops(schedule.crews.get(pos));}
            public void onNothingSelected(android.widget.AdapterView<?> p){}
        });
        for(int i=0;i<schedule.crews.size();i++)if(schedule.crews.get(i).workers.toUpperCase(Locale.US).contains("REBECCA"))crews.setSelection(i);
    }
    void showStops(SchedulePdf.Crew crew){
        list.removeAllViews();
        text(list,"Crew: "+crew.workers+"\nRoles not listed in the PDF remain unspecified. Update them in the job details.",15);
        for(SchedulePdf.Stop stop:crew.stops){
            CheckBox include=new CheckBox(this);include.setText(stop.label);include.setTextSize(20);include.setChecked(stop.include);list.addView(include);
            include.setOnCheckedChangeListener((b,checked)->stop.include=checked);
            text(list,stop.address,18);
            text(list,"Profile: "+stop.first+" "+stop.last,15);
            if(!stop.notes.isEmpty())text(list,"Job notes: "+stop.notes,16);
            if(!stop.flags.isEmpty())text(list,"Access: "+stop.flags,16);
            if(!stop.include)text(list,"Review needed: name or address may be incomplete. Edit before selecting.",15);
            button(list,"Edit this stop",v->edit(stop));
        }
    }
    EditText editField(LinearLayout l,String label,String value){text(l,label,14);EditText e=new EditText(this);e.setText(value);e.setMinHeight(dp(48));l.addView(e);return e;}
    void edit(SchedulePdf.Stop stop){
        LinearLayout l=column();EditText first=editField(l,"First name",stop.first),last=editField(l,"Last name",stop.last),
            address=editField(l,"Full address (verify destination)",stop.address),notes=editField(l,"Job notes",stop.notes),flags=editField(l,"Gate code / access flags",stop.flags);
        ScrollView scroll=new ScrollView(this);scroll.addView(l);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(stop.label).setView(scroll).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(first.getText().toString().trim().isEmpty()||last.getText().toString().trim().isEmpty()||address.getText().toString().trim().isEmpty()){Toast.makeText(this,"Name and address are required",Toast.LENGTH_LONG).show();return;}
            stop.first=first.getText().toString().trim();stop.last=last.getText().toString().trim();stop.address=address.getText().toString().trim();
            stop.notes=notes.getText().toString();stop.flags=flags.getText().toString();dialog.dismiss();showStops((SchedulePdf.Crew)crews.getSelectedItem());
        }));dialog.show();
    }
    void confirm(){
        if(applying)return;
        SchedulePdf.Crew crew=(SchedulePdf.Crew)crews.getSelectedItem();int count=0;for(SchedulePdf.Stop s:crew.stops)if(s.include)count++;
        if(count==0){Toast.makeText(this,"Select at least one stop",Toast.LENGTH_LONG).show();return;}
        new AlertDialog.Builder(this).setTitle("Import "+count+" stops for "+selectedDate+"?")
            .setMessage("Crew: "+crew.workers+"\n\nConfirm the addresses are correct. Existing imported jobs for the same date, crew and stop are skipped, not overwritten. No email is accessed.")
            .setNegativeButton("Review again",null).setPositiveButton("Import",(d,w)->apply(crew)).show();
    }
    void apply(SchedulePdf.Crew crew){
        applying=true;
        new Thread(()->{
            try {
                int[] result=importStops(this,crew,selectedDate);
                runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Schedule imported")
                    .setMessage(result[0]+" jobs added; "+result[1]+" duplicates skipped.\nJob date: "+selectedDate)
                    .setPositiveButton("Open route",(d,w)->{startActivity(new Intent(this,FieldActivity.class).putExtra("routeDate",selectedDate).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish();}).setCancelable(false).show());
            }catch(Exception e){runOnUiThread(()->error(e.getMessage()));}
        },"SaveSchedule").start();
    }
    public static synchronized int[] importStops(Context context,SchedulePdf.Crew crew,String date)throws Exception{
        FieldStore store=new FieldStore(context);JSONArray clients=new JSONArray(context.getSharedPreferences("client_route_native",0).getString("clients_json","[]"));
        List<Client> existing=ClientStorage.load(context);int added=0,skipped=0;
        for(SchedulePdf.Stop stop:crew.stops){
            if(!stop.include)continue;
            if(stop.first.trim().isEmpty()||stop.last.trim().isEmpty()||stop.address.trim().isEmpty())throw new IOException("Each selected stop needs a name and address.");
            String stable=SchedulePdf.key(stop.label)+"|"+SchedulePdf.key(stop.address);
            String importKey=date+"|"+SchedulePdf.key(crew.workers)+"|"+stable;
            boolean duplicate=false;for(int i=0;i<store.jobs().length();i++)if(store.jobs().getJSONObject(i).optString("importKey").equals(importKey))duplicate=true;
            if(duplicate){skipped++;continue;}
            String id="pdf-"+UUID.nameUUIDFromBytes(stable.getBytes("UTF-8")).toString();
            for(Client c:existing)if(SchedulePdf.key(c.getFullName()).equals(SchedulePdf.key(stop.first+" "+stop.last))&&SchedulePdf.key(c.getFullAddress()).equals(SchedulePdf.key(stop.address)))id=c.id;
            boolean has=false;for(int i=0;i<clients.length();i++)if(clients.getJSONObject(i).optString("id").equals(id))has=true;
            if(!has){
                JSONObject c=new JSONObject();c.put("id",id);c.put("firstName",stop.first);c.put("lastName",stop.last);c.put("phone","");
                // Preserve the full original address without guessing missing components.
                c.put("street",stop.address);c.put("apt","");c.put("city","");c.put("state","");c.put("zip","");c.put("startHour24",-1);c.put("endHour24",-1);clients.put(c);
            }
            JSONObject profile=store.profile(id);
            if(!profile.has("notes"))profile.put("notes","Imported schedule label: "+stop.label);
            if(!stop.flags.isEmpty()&&!profile.optString("flags").contains(stop.flags))profile.put("flags",(profile.optString("flags")+"\n"+stop.flags).trim());
            JSONObject job=new JSONObject();job.put("id",UUID.randomUUID().toString());job.put("client",id);job.put("name",stop.first+" "+stop.last);job.put("address",stop.address);
            job.put("scheduled",date);job.put("crew",crew.workers);job.put("notes",stop.notes);job.put("photos",new JSONArray());job.put("importKey",importKey);job.put("sourceLabel",stop.label);
            store.jobs().put(job);added++;
        }
        store.data.put("pendingClientImport",clients.toString());store.save();store.finishPendingImport();
        return new int[]{added,skipped};
    }
}
