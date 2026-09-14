package com.kalab.clientroute;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientStorage {
    private static final String PREFS = "client_route_native";
    private static final String KEY = "clients_json";

    // Matches legacy free-text times like "8:00 AM", "08:15 pm", "8:00AM"
    private static final Pattern LEGACY_TIME = Pattern.compile(
            "(\\d{1,2}):(\\d{2})\\s*(AM|PM|am|pm)?");

    public static List<Client> load(Context context) {
        List<Client> items = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        try {
            JSONArray a = new JSONArray(raw);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                items.add(fromJson(o));
            }
        } catch (Exception ignored) {}
        return items;
    }

    public static void save(Context context, List<Client> clients) {
        JSONArray a = new JSONArray();
        try {
            for (Client c : clients) {
                JSONObject o = new JSONObject();
                o.put("id", c.id);
                o.put("firstName", c.firstName);
                o.put("lastName", c.lastName);
                o.put("phone", c.phone);
                o.put("street", c.street);
                o.put("apt", c.apt);
                o.put("city", c.city);
                o.put("state", c.state);
                o.put("zip", c.zip);
                o.put("startHour24", c.startHour24);
                o.put("startMinute", c.startMinute);
                o.put("endHour24", c.endHour24);
                o.put("endMinute", c.endMinute);
                a.put(o);
            }
        } catch (Exception ignored) {}
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, a.toString()).apply();
    }

    // Reads either the current schema, or migrates an older single-field
    // address/startTime/endTime record so existing local data is not lost.
    private static Client fromJson(JSONObject o) throws Exception {
        if (o.has("street")) {
            return new Client(
                    o.optString("id"),
                    o.optString("firstName"),
                    o.optString("lastName"),
                    o.optString("phone"),
                    o.optString("street"),
                    o.optString("apt"),
                    o.optString("city"),
                    o.optString("state", "TX"),
                    o.optString("zip"),
                    o.optInt("startHour24", -1),
                    o.optInt("startMinute", 0),
                    o.optInt("endHour24", -1),
                    o.optInt("endMinute", 0));
        }

        // Legacy record from the earlier build: single "address" field and
        // free-text "startTime"/"endTime" strings.
        String legacyAddress = o.optString("address", "");
        int[] start = parseLegacyTime(o.optString("startTime", ""));
        int[] end = parseLegacyTime(o.optString("endTime", ""));
        return new Client(
                o.optString("id"),
                o.optString("firstName"),
                o.optString("lastName"),
                o.optString("phone"),
                legacyAddress, // put the old free-text address into Street Address for review
                "",
                "",
                "TX",
                "",
                start == null ? -1 : start[0],
                start == null ? 0 : start[1],
                end == null ? -1 : end[0],
                end == null ? 0 : end[1]);
    }

    private static int[] parseLegacyTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        Matcher m = LEGACY_TIME.matcher(text.trim());
        if (!m.find()) return null;
        try {
            int hour = Integer.parseInt(m.group(1));
            int minute = Integer.parseInt(m.group(2));
            String ampm = m.group(3);
            if (ampm != null) {
                ampm = ampm.toUpperCase();
                if (ampm.equals("PM") && hour < 12) hour += 12;
                if (ampm.equals("AM") && hour == 12) hour = 0;
            }
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
            return new int[]{hour, minute};
        } catch (Exception e) {
            return null;
        }
    }
}
