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
    }
    public void save() {
        if(!context.getSharedPreferences("field_v2",0).edit().putString("data",data.toString()).commit())
            throw new IllegalStateException("Storage is full. Changes could not be saved.");
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
