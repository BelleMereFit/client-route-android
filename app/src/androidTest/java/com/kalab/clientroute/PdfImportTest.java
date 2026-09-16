package com.kalab.clientroute;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import java.io.*;

@RunWith(AndroidJUnit4.class)
public class PdfImportTest {
    @Test public void testProvidedPdfAndDuplicateSafeImport() throws Exception {
        Context target=InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context test=InstrumentationRegistry.getInstrumentation().getContext();
        InputStream fixture;
        try{fixture=test.getAssets().open("schedule-fixture.pdf");}
        catch(IOException e){Assume.assumeNoException("Supply the customer's PDF locally; it is not committed to the repository.",e);return;}
        SchedulePdf.Schedule schedule;
        try(InputStream in=fixture){schedule=SchedulePdf.read(target,in);}
        assertEquals(2,schedule.crews.size());
        SchedulePdf.Crew selected=null;
        for(SchedulePdf.Crew c:schedule.crews)if(c.workers.contains("REBECCA"))selected=c;
        assertNotNull(selected);assertEquals(3,selected.stops.size());
        for(SchedulePdf.Stop stop:selected.stops){
            assertTrue(stop.include);
            assertFalse("Job notes must be joined from page two",stop.notes.isEmpty());
            assertFalse(stop.first.isEmpty());assertFalse(stop.address.contains("#N/A"));
        }
        File previewFile=new File(target.getCacheDir(),"schedule-preview.pdf");
        try(InputStream in=test.getAssets().open("schedule-fixture.pdf");OutputStream out=new FileOutputStream(previewFile)){
            byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);
        }
        android.app.Instrumentation instrument=InstrumentationRegistry.getInstrumentation();
        ScheduleImportActivity preview=(ScheduleImportActivity)instrument.startActivitySync(
            new android.content.Intent(target,ScheduleImportActivity.class).setData(android.net.Uri.fromFile(previewFile))
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
        for(int i=0;i<100&&preview.schedule==null;i++){android.os.SystemClock.sleep(100);instrument.waitForIdleSync();}
        assertNotNull("Preview must finish loading",preview.schedule);
        instrument.runOnMainSync(()->assertTrue(((SchedulePdf.Crew)preview.crews.getSelectedItem()).workers.contains("REBECCA")));
        android.graphics.Bitmap screenshot=instrument.getUiAutomation().takeScreenshot();
        if(screenshot!=null)try(OutputStream out=new FileOutputStream(new File(target.getCacheDir(),"import-preview.png"))){
            screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
        }
        instrument.runOnMainSync(()->preview.finish());
        String oldClients=target.getSharedPreferences("client_route_native",0).getString("clients_json","[]");
        String oldJobs=target.getSharedPreferences("field_v2",0).getString("data","{\"jobs\":[],\"profiles\":{}}");
        try{
            target.getSharedPreferences("client_route_native",0).edit().putString("clients_json","[]").commit();
            target.getSharedPreferences("field_v2",0).edit().putString("data","{\"jobs\":[],\"profiles\":{}}").commit();
            assertArrayEquals(new int[]{3,0},ScheduleImportActivity.importStops(target,selected,schedule.date));
            assertArrayEquals(new int[]{0,3},ScheduleImportActivity.importStops(target,selected,schedule.date));
            assertEquals(3,ClientStorage.load(target).size());
            FieldStore reloaded=new FieldStore(target);
            assertEquals(3,reloaded.jobs().length());
            assertFalse(reloaded.data.has("pendingClientImport"));
            assertEquals(selected.stops.get(0).address,reloaded.jobs().getJSONObject(0).getString("address"));
            assertEquals(selected.stops.get(0).notes,reloaded.jobs().getJSONObject(0).getString("notes"));
        }finally{
            target.getSharedPreferences("client_route_native",0).edit().putString("clients_json",oldClients).commit();
            target.getSharedPreferences("field_v2",0).edit().putString("data",oldJobs).commit();
        }
    }
}
