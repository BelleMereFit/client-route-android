package com.kalab.clientroute;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ClientStorage {
    private static final String PREFS = "client_route_native";
    private static final String KEY = "clients_json";
    public static List<Client> load(Context context) {
        List<Client> items = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        try { JSONArray a = new JSONArray(raw); for (int i=0; i<a.length(); i++) { JSONObject o=a.getJSONObject(i); items.add(new Client(o.optString("id"),o.optString("firstName"),o.optString("lastName"),o.optString("phone"),o.optString("address"),o.optString("startTime"),o.optString("endTime"))); } } catch (Exception ignored) {}
        return items;
    }
    public static void save(Context context, List<Client> clients) {
        JSONArray a = new JSONArray();
        try { for (Client c: clients) { JSONObject o=new JSONObject(); o.put("id",c.id);o.put("firstName",c.firstName);o.put("lastName",c.lastName);o.put("phone",c.phone);o.put("address",c.address);o.put("startTime",c.startTime);o.put("endTime",c.endTime);a.put(o); } } catch(Exception ignored) {}
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY,a.toString()).apply();
    }
}
