package com.kalab.clientroute;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import org.json.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

/** Parked-use field workflow. No location tracking or background timer service needed. */
public class FieldActivity extends Activity {
    FieldStore store;
    LinearLayout page,body;
    String screen="Today", selectedClient="", pendingJob="", pendingPhoto="";
    String routeDate="";
    int reportDays=1;
    Handler handler=new Handler();
    TextView clock;
    final Runnable tick=new Runnable(){ public void run(){
        if(clock!=null && store!=null && store.active()!=null)
            clock.setText("Elapsed • "+duration(System.currentTimeMillis()-store.active().optLong("start")));
        handler.postDelayed(this,1000);
    }};
    interface Work {void run() throws Exception;}
    void safe(Work w){try{w.run();}catch(Exception e){new AlertDialog.Builder(this).setTitle("Action not completed").setMessage(e.getMessage()).setPositiveButton("OK",null).show();}}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        routeDate=getIntent().getStringExtra("routeDate");
        if(routeDate==null)routeDate=day(System.currentTimeMillis());
        if(b!=null)routeDate=b.getString("routeDate",routeDate);
        if(b!=null){pendingJob=b.getString("pendingJob","");pendingPhoto=b.getString("pendingPhoto","");screen=b.getString("screen","Today");selectedClient=b.getString("client","");}
        try{store=new FieldStore(this);}catch(Exception e){
            new AlertDialog.Builder(this).setMessage("Saved job data could not be read. Do not uninstall or clear app data.").setPositiveButton("Close",(d,w)->finish()).show();return;
        }
        render();handler.post(tick);
    }
    @Override protected void onResume(){super.onResume();if(store!=null)safe(()->{store=new FieldStore(this);render();});}
    @Override protected void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
    @Override protected void onSaveInstanceState(Bundle b){
        b.putString("routeDate",routeDate);
        b.putString("pendingJob",pendingJob);b.putString("pendingPhoto",pendingPhoto);b.putString("screen",screen);b.putString("client",selectedClient);super.onSaveInstanceState(b);
    }
    int dp(int n){return (int)(getResources().getDisplayMetrics().density*n);}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(16),dp(10),dp(16),dp(10));return l;}
    TextView text(LinearLayout parent,String s,int size){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(32,45,40));t.setPadding(0,dp(6),0,dp(8));parent.addView(t);return t;
    }
    void button(LinearLayout p,String s,Work w){
        Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setMinHeight(dp(56));p.addView(b,new LinearLayout.LayoutParams(-1,-2));b.setOnClickListener(v->safe(w));
    }
    EditText input(LinearLayout p,String label,String value){
        text(p,label,14);EditText e=new EditText(this);e.setText(value);e.setTextSize(18);e.setMinHeight(dp(52));e.setInputType( android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);p.addView(e);return e;
    }
    LinearLayout card(){
        LinearLayout c=column();c.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(8),0,dp(8));body.addView(c,lp);return c;
    }
    void render(){
        clock=null;page=column();page.setBackgroundColor(Color.rgb(242,246,243));setContentView(page);
        text(page,"CLIENT ROUTE",14);text(page,screen,30);
        LinearLayout nav=new LinearLayout(this);page.addView(nav);
        for(String tab:new String[]{"Today","Clients","Reports","Setup"}){
            Button b=new Button(this);b.setText(tab);b.setAllCaps(false);b.setTextSize(13);nav.addView(b,new LinearLayout.LayoutParams(0,dp(52),1));
            b.setOnClickListener(v->{screen=tab;selectedClient="";if(tab.equals("Today"))routeDate=day(System.currentTimeMillis());render();});
        }
        ScrollView scroll=new ScrollView(this);page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));body=column();scroll.addView(body);
        safe(()->{if(screen.equals("Today"))today();else if(screen.equals("Clients"))clients("");else if(screen.equals("Profile"))profile();else if(screen.equals("Reports"))reports();else setup();});
    }
    String date(long ms){return new SimpleDateFormat("EEE, MMM d • h:mm a",Locale.US).format(new Date(ms));}
    String day(long ms){return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date(ms));}
    static String duration(long ms){long min=Math.max(0,ms)/60000;return String.format(Locale.US,"%dh %02dm",min/60,min%60);}
    Client client(String id){for(Client c:ClientStorage.load(this))if(c.id.equals(id))return c;return null;}
    String name(JSONObject j){return j.optString("name","Client");}
    void today() throws Exception{
        text(body,new SimpleDateFormat("EEEE, MMMM d",Locale.US).format(new Date()),18);
        text(body,"Set up while parked. Start navigation before driving.",14);
        button(body,"Import schedule PDF",()->startActivity(new Intent(this,ScheduleImportActivity.class)));
        button(body,"Route date: "+routeDate+" (change)",()->{
            String[] p=routeDate.split("-");
            new DatePickerDialog(this,(view,y,m,d)->{routeDate=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);render();},
                Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2])).show();
        });
        JSONObject active=store.active();
        if(active!=null){
            LinearLayout c=card();text(c,"ACTIVE JOB",14);text(c,name(active),26);
            clock=text(c,"Elapsed • "+duration(System.currentTimeMillis()-active.optLong("start")),24);
            text(c,"Total includes travel, work and loading.",14);
            String flags=store.profile(active.optString("client")).optString("flags");
            if(!flags.isEmpty())text(c,"ACCESS / FLAGS\n"+flags,20);
            button(c,"Resume directions",()->navigate(active.optString("address")));
            if(active.optLong("arrived")==0)button(c,"Mark arrived (optional)",()->{active.put("arrived",System.currentTimeMillis());store.save();render();});
            button(c,"Job notes & workers",()->editJob(active));
            button(c,"Add issue photo",()->photo(active));
            button(c,"Finish job",()->new AlertDialog.Builder(this).setTitle("Equipment loaded and job finished?")
                .setMessage("This stops the timer and saves this job to the client's history.")
                .setNegativeButton("Keep running",null).setPositiveButton("Finish job",(d,w)->safe(()->{active.put("end",System.currentTimeMillis());store.save();render();})).show());
        }
        int count=0;
        for(int i=0;i<store.jobs().length();i++){
            JSONObject j=store.jobs().getJSONObject(i);
            if(!j.optString("scheduled").equals(routeDate) || j.optLong("start")>0)continue;
            count++;LinearLayout c=card();text(c,name(j),24);text(c,j.optString("address"),18);
            String f=store.profile(j.optString("client")).optString("flags");if(!f.isEmpty())text(c,"Access / flags: "+f,18);
            if(!j.optString("notes").isEmpty())text(c,j.optString("notes"),16);
            button(c,"Start & Navigate",()->start(j));
            button(c,"Job details",()->editJob(j));
            button(c,"Remove from today",()->new AlertDialog.Builder(this).setMessage("Remove this unstarted job? Client history is kept.").setNegativeButton("Cancel",null)
                .setPositiveButton("Remove",(d,w)->safe(()->{removeJob(j.optString("id"));store.save();render();})).show());
        }
        if(count==0 && active==null)text(body,"No jobs waiting for "+routeDate+". Import a PDF or add a client to the route.",20);
        button(body,"Add client to route",()->{screen="Clients";render();});
    }
    void removeJob(String id)throws Exception{
        JSONArray a=new JSONArray();for(int i=0;i<store.jobs().length();i++)if(!store.jobs().getJSONObject(i).optString("id").equals(id))a.put(store.jobs().get(i));store.data.put("jobs",a);
    }
    void start(JSONObject j)throws Exception{
        if(store.active()!=null){throw new Exception("Finish the active job before starting another.");}
        j.put("start",System.currentTimeMillis());store.save();
        try{navigate(j.optString("address"));}catch(Exception e){j.put("start",0);store.save();throw e;}
        render();
    }
    void navigate(String address)throws Exception{
        if(address.trim().isEmpty())throw new Exception("Add an address before navigating.");
        Intent map=new Intent(Intent.ACTION_VIEW,Uri.parse("google.navigation:q="+Uri.encode(address)));
        try{startActivity(map);}catch(ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+Uri.encode(address))));
        }
    }
    void clients(String unused){
        button(body,"Add new client",()->startActivity(new Intent(this,AddEditClientActivity.class)));
        EditText search=input(body,"Find by name, phone or address","");
        LinearLayout list=column();body.addView(list);
        fillClients(list,"");
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){fillClients(list,s.toString());}public void afterTextChanged(Editable e){}});
    }
    void fillClients(LinearLayout list,String query){
        list.removeAllViews();
        for(Client c:ClientStorage.load(this)){
            if(!(c.getFullName()+" "+c.phone+" "+c.getFullAddress()).toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US)))continue;
            button(list,c.getFullName()+"\n"+c.getFullAddress(),()->{selectedClient=c.id;screen="Profile";render();});
        }
    }
    void profile()throws Exception{
        Client c=client(selectedClient);if(c==null){text(body,"Client not found.",20);return;}
        text(body,c.getFullName(),26);text(body,c.getFullAddress(),18);
        JSONObject p=store.profile(c.id);text(body,"Access / flags: "+p.optString("flags","None"),18);
        text(body,"Client notes: "+p.optString("notes","None"),16);
        button(body,"Add to route: "+routeDate,()->{
            for(int i=0;i<store.jobs().length();i++){
                JSONObject existing=store.jobs().getJSONObject(i);
                if(existing.optString("client").equals(c.id)&&existing.optString("scheduled").equals(routeDate)&&existing.optLong("end")==0)
                    throw new Exception("This client already has an unfinished job on this date.");
            }
            JSONObject j=new JSONObject();j.put("id",UUID.randomUUID().toString());j.put("client",c.id);j.put("name",c.getFullName());j.put("address",c.getFullAddress());j.put("scheduled",routeDate);
            j.put("crew",store.data.optString("crew"));j.put("photos",new JSONArray());store.jobs().put(j);store.save();screen="Today";render();
        });
        button(body,"Edit contact information",()->startActivity(new Intent(this,AddEditClientActivity.class).putExtra(AddEditClientActivity.EXTRA_CLIENT_ID,c.id)));
        button(body,"Edit access flags & client notes",()->{
            LinearLayout l=column();EditText flags=input(l,"Gate code / pets / hazards / important flags",p.optString("flags"));
            EditText notes=input(l,"Persistent client notes",p.optString("notes"));
            form("Client notes",l,()->{p.put("flags",flags.getText().toString());p.put("notes",notes.getText().toString());store.save();render();});
        });
        if(c.phone!=null&&!c.phone.isEmpty())button(body,"Call client",()->startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+c.phone.replaceAll("[^0-9+]","")))));
        int n=0;long total=0;
        for(int i=0;i<store.jobs().length();i++){JSONObject j=store.jobs().getJSONObject(i);if(j.optString("client").equals(c.id)&&j.optLong("end")>0){n++;total+=j.optLong("end")-j.optLong("start");}}
        text(body,"History • "+n+" completed • "+duration(total),22);
        for(int i=store.jobs().length()-1;i>=0;i--){JSONObject j=store.jobs().getJSONObject(i);if(j.optString("client").equals(c.id)&&j.optLong("end")>0)jobSummary(j);}
        button(body,"Delete client",()->{
            for(int i=0;i<store.jobs().length();i++)if(store.jobs().getJSONObject(i).optString("client").equals(c.id))throw new Exception("This client has job records. Deletion is blocked to protect history.");
            new AlertDialog.Builder(this).setMessage("Delete this client permanently?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->safe(()->{
                List<Client> all=ClientStorage.load(this);for(int i=all.size()-1;i>=0;i--)if(all.get(i).id.equals(c.id))all.remove(i);
                ClientStorage.save(this,all);screen="Clients";render();
            })).show();
        });
    }
    void form(String title,LinearLayout l,Work save){
        ScrollView s=new ScrollView(this);s.addView(l);
        AlertDialog d=new AlertDialog.Builder(this).setTitle(title).setView(s).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(v->d.getButton(-1).setOnClickListener(b->safe(()->{save.run();d.dismiss();})));d.show();
    }
    void editJob(JSONObject j){
        LinearLayout l=column();EditText notes=input(l,"Notes for this job",j.optString("notes"));
        EditText crew=input(l,"Workers and roles (one per line: Name - Role)",j.optString("crew"));
        text(l,"Example: Michael - Driver / Mowing\nKevin - Weedeater",14);
        form("Job details",l,()->{j.put("notes",notes.getText().toString());j.put("crew",crew.getText().toString());store.save();render();});
    }
    void photo(JSONObject j)throws Exception{
        File folder=new File(getFilesDir(),"issues");if(!folder.exists()&&!folder.mkdirs())throw new IOException("Cannot create photo folder.");
        File f=File.createTempFile("issue_",".jpg",folder);pendingPhoto=f.getName();pendingJob=j.optString("id");
        store.data.put("pendingPhoto",pendingPhoto);store.data.put("pendingJob",pendingJob);store.save();
        Uri uri=FileProvider.getUriForFile(this,getPackageName()+".photos",f);
        Intent capture=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);capture.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        capture.setClipData(ClipData.newRawUri("Issue photo",uri));capture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try{startActivityForResult(capture,41);}catch(ActivityNotFoundException e){f.delete();throw new Exception("No camera app is installed on this device.");}
    }
    @Override protected void onActivityResult(int req,int result,Intent data){
        super.onActivityResult(req,result,data);
        if(req==42&&result==RESULT_OK&&data!=null){safe(()->export(data.getData()));return;}
        if(req!=41)return;
        safe(()->{
            pendingPhoto=store.data.optString("pendingPhoto",pendingPhoto);pendingJob=store.data.optString("pendingJob",pendingJob);
            File f=new File(new File(getFilesDir(),"issues"),pendingPhoto);
            if(result!=RESULT_OK||f.length()==0){f.delete();return;}
            JSONObject job=null;for(int i=0;i<store.jobs().length();i++)if(store.jobs().getJSONObject(i).optString("id").equals(pendingJob))job=store.jobs().getJSONObject(i);
            if(job==null)throw new Exception("Job not found. Photo retained for recovery.");
            JSONObject pic=new JSONObject();pic.put("file",pendingPhoto);pic.put("taken",System.currentTimeMillis());pic.put("caption","");
            job.getJSONArray("photos").put(pic);store.data.remove("pendingPhoto");store.data.remove("pendingJob");store.save();
            LinearLayout l=column();EditText caption=input(l,"Describe the issue (optional)","");
            form("Photo saved",l,()->{pic.put("caption",caption.getText().toString());store.save();render();});
        });
    }
    void jobSummary(JSONObject j)throws Exception{
        LinearLayout c=card();text(c,name(j)+" • "+date(j.optLong("start")),19);
        text(c,"Total: "+duration(j.optLong("end")-j.optLong("start")),22);
        if(j.optLong("arrived")>0)text(c,"Travel: "+duration(j.optLong("arrived")-j.optLong("start"))+" • On-site: "+duration(j.optLong("end")-j.optLong("arrived")),16);
        text(c,"Workers / roles:\n"+j.optString("crew","Not recorded"),16);text(c,"Notes: "+j.optString("notes",""),16);
        button(c,"Edit job notes / workers",()->editJob(j));
        button(c,"Add issue photo",()->photo(j));
        JSONArray pics=j.optJSONArray("photos");if(pics==null)return;
        for(int i=0;i<pics.length();i++){
            JSONObject pic=pics.getJSONObject(i);button(c,"View photo • "+pic.optString("caption","Issue"),()->{
                File f=new File(new File(getFilesDir(),"issues"),pic.getString("file"));
                Intent view=new Intent(Intent.ACTION_VIEW);view.setDataAndType(FileProvider.getUriForFile(this,getPackageName()+".photos",f),"image/jpeg");view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(view);
            });
        }
    }
    void reports()throws Exception{
        for(int days:new int[]{1,7,30})button(body,days==1?"Today":"Last "+days+" days",()->{reportDays=days;render();});
        Calendar cal=Calendar.getInstance();cal.set(Calendar.HOUR_OF_DAY,0);cal.set(Calendar.MINUTE,0);cal.set(Calendar.SECOND,0);cal.set(Calendar.MILLISECOND,0);cal.add(Calendar.DATE,1-reportDays);
        long since=cal.getTimeInMillis(),total=0;int n=0;
        for(int i=0;i<store.jobs().length();i++){JSONObject j=store.jobs().getJSONObject(i);if(j.optLong("start")>=since&&j.optLong("end")>0){n++;total+=j.optLong("end")-j.optLong("start");}}
        text(body,(reportDays==1?"Today":"Last "+reportDays+" days")+" • "+n+" jobs",24);text(body,duration(total)+" total elapsed",26);
        text(body,"Includes travel and loading. Jobs are grouped by start date. In-progress jobs are excluded. This is not individual payroll time.",14);
        for(int i=store.jobs().length()-1;i>=0;i--){JSONObject j=store.jobs().getJSONObject(i);if(j.optLong("start")>=since&&j.optLong("end")>0)jobSummary(j);}
    }
    void setup(){
        text(body,"Default crew",24);text(body,"Set once, then copied into each new job. Change individual jobs when workers or roles differ.",16);
        EditText crew=input(body,"One worker and role per line",store.data.optString("crew"));
        button(body,"Save default crew",()->{store.data.put("crew",crew.getText().toString());store.save();Toast.makeText(this,"Crew saved",Toast.LENGTH_SHORT).show();});
        text(body,"Schedule import",24);
        text(body,"Intended inbox: rebeccafay1992@gmail.com\nOpen or share the schedule PDF from Gmail or Files into Client Route Field. Review the crew, date and stops, then import. PDF processing stays on this tablet.",16);
        button(body,"Import schedule PDF",()->startActivity(new Intent(this,ScheduleImportActivity.class)));
        text(body,"Automatic email retrieval is not connected. Rebecca must authorize access before unattended inbox import can be added. Excel files and scanned/image-only PDFs are not supported by this importer.",16);
        text(body,"Local storage & backup",24);text(body,"Clients, gate codes, job records and photos stay on this tablet. Uninstalling or clearing storage deletes them. Export regularly and keep the backup private.",16);
        button(body,"Export backup ZIP (includes photos)",()->{
            Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/zip");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"Client-Route-"+day(System.currentTimeMillis())+".zip");startActivityForResult(i,42);
        });
        text(body,"Version 2.1 • Android 6.0.1+\nThis side-by-side build has separate storage from the original Client Route app. Existing records are not automatically copied.\nBackup contains JSON records and original photos. In-app restore is not included.",14);
    }
    void export(Uri uri)throws Exception{
        try(OutputStream out=getContentResolver().openOutputStream(uri);ZipOutputStream zip=new ZipOutputStream(out)){
            zip.putNextEntry(new ZipEntry("jobs-and-profiles.json"));zip.write(store.data.toString(2).getBytes("UTF-8"));zip.closeEntry();
            zip.putNextEntry(new ZipEntry("clients.json"));zip.write(getSharedPreferences("client_route_native",0).getString("clients_json","[]").getBytes("UTF-8"));zip.closeEntry();
            File[] files=new File(getFilesDir(),"issues").listFiles();if(files!=null)for(File f:files){
                zip.putNextEntry(new ZipEntry("issues/"+f.getName()));try(FileInputStream in=new FileInputStream(f)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)zip.write(b,0,n);}zip.closeEntry();
            }
        }
        Toast.makeText(this,"Backup exported",Toast.LENGTH_LONG).show();
    }
    @Override public void onBackPressed(){if(!screen.equals("Today")){screen="Today";render();}else super.onBackPressed();}
}
