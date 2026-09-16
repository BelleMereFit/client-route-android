package com.kalab.clientroute;

import android.content.Context;
import android.content.Intent;
import android.app.Instrumentation;
import android.widget.EditText;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.json.*;

@RunWith(AndroidJUnit4.class)
public class FieldStoreTest {
    Context getContext(){return InstrumentationRegistry.getInstrumentation().getTargetContext();}
    @Test public void testJobLifecycleAndPersistence() throws Exception {
        FieldStore s=new FieldStore(getContext());
        String original=s.data.toString();
        try {
            s.data=new JSONObject("{\"jobs\":[],\"profiles\":{},\"crew\":\"Michael - Mowing\",\"schema\":2}");
            JSONObject j=new JSONObject();
            j.put("id","test-job");j.put("client","test-client");j.put("start",1000);j.put("end",0);
            j.put("photos",new JSONArray());j.put("crew",s.data.getString("crew"));
            s.jobs().put(j);s.profile("test-client").put("flags","Test gate");s.save();
            FieldStore reopened=new FieldStore(getContext());
            assertEquals("test-job",reopened.active().getString("id"));
            assertEquals("Test gate",reopened.profile("test-client").getString("flags"));
            assertEquals("Michael - Mowing",reopened.active().getString("crew"));
            reopened.active().put("end",61000);reopened.save();
            assertNull(new FieldStore(getContext()).active());
            assertEquals("1h 01m",FieldActivity.duration(3660000));
            assertEquals("0h 00m",FieldActivity.duration(-1));
        } finally {s.data=new JSONObject(original);s.save();}
    }
    @Test public void testOptionalContactFieldsAndScreens() throws Exception {
        Context context=getContext();
        String original=context.getSharedPreferences("client_route_native",0).getString("clients_json","[]");
        Instrumentation instrument=InstrumentationRegistry.getInstrumentation();
        AddEditClientActivity form=(AddEditClientActivity)instrument.startActivitySync(
            new Intent(context,AddEditClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        try {
            instrument.runOnMainSync(()->{
                ((EditText)form.findViewById(R.id.firstNameInput)).setText("Test");
                ((EditText)form.findViewById(R.id.lastNameInput)).setText("Customer");
                ((EditText)form.findViewById(R.id.streetInput)).setText("100 Test Street");
                ((EditText)form.findViewById(R.id.cityInput)).setText("Tyler");
                form.findViewById(R.id.saveButton).performClick();
            });
            instrument.waitForIdleSync();
            boolean found=false;
            for(Client c:ClientStorage.load(context))if(c.firstName.equals("Test")&&c.lastName.equals("Customer")){
                assertEquals("",c.phone);assertEquals("",c.zip);found=true;
            }
            assertTrue("Client must save without phone, apt, ZIP or time",found);
            FieldActivity field=(FieldActivity)instrument.startActivitySync(
                new Intent(context,FieldActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            instrument.runOnMainSync(()->{
                for(String screen:new String[]{"Today","Clients","Reports","Setup"}){
                    field.screen=screen;field.render();assertNotNull(field.body);
                }
                field.finish();
            });
        } finally {
            context.getSharedPreferences("client_route_native",0).edit().putString("clients_json",original).commit();
            instrument.runOnMainSync(()->form.finish());
        }
    }
}
