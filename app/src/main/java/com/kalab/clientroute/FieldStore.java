package com.kalab.clientroute;

import android.content.Context;
import org.json.*;

/** Separate versioned store preserves legacy client IDs and records unchanged. */
public final class FieldStore {
    private final Context context;
    public JSONObject data;
    public FieldStore(Context c) throws Exception {
        context=c;
        data=new JSONObject(c.getSharedPreferences("field_v2",0).getString("data",
            "{\"jobs\":[],\"profiles\":{},\"crew\":\"\",\"schema\":2}"));
        finishPendingImport();
    }
    public void save() {
        if(!context.getSharedPreferences("field_v2",0).edit().putString("data",data.toString()).commit())
            throw new IllegalStateException("Storage is full. Changes could not be saved.");
    }
    /** Write-ahead record completes a client/job import after interruption. */
    public void finishPendingImport() throws Exception {
        if(!data.has("pendingClientImport"))return;
        if(!context.getSharedPreferences("client_route_native",0).edit()
            .putString("clients_json",data.getString("pendingClientImport")).commit())
            throw new IllegalStateException("Cannot finish import: storage is full.");
        data.remove("pendingClientImport");save();
    }
    public JSONArray jobs() { return data.optJSONArray("jobs"); }
    public JSONObject active() {
        for(int i=0;i<jobs().length();i++){
            JSONObject j=jobs().optJSONObject(i);
            if(j.optLong("start")>0 && j.optLong("end")==0) return j;
        }
        return null;
    }
    public JSONObject profile(String id) throws Exception {
        JSONObject p=data.getJSONObject("profiles");
        if(!p.has(id))p.put(id,new JSONObject());
        return p.getJSONObject(id);
    }
}
