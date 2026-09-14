package com.kalab.clientroute;

public class Client {
    public String id;
    public String firstName;
    public String lastName;
    public String phone;   // formatted as (xxx) xxx-xxxx
    public String street;  // e.g. 100 Pennsylvania Ave
    public String apt;     // optional, may be empty
    public String city;
    public String state;   // two-letter abbreviation
    public String zip;
    public int startHour24; // 0-23, -1 means not set
    public int startMinute; // 0-59
    public int endHour24;   // 0-23, -1 means not set
    public int endMinute;   // 0-59

    public Client(String id, String firstName, String lastName, String phone,
                  String street, String apt, String city, String state, String zip,
                  int startHour24, int startMinute, int endHour24, int endMinute) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.street = street;
        this.apt = apt;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.startHour24 = startHour24;
        this.startMinute = startMinute;
        this.endHour24 = endHour24;
        this.endMinute = endMinute;
    }

    public String getFullName() {
        return (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.trim().isEmpty()) sb.append(street.trim());
        if (apt != null && !apt.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(apt.trim());
        }
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city.trim());
        }
        if (state != null && !state.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state.trim());
        }
        if (zip != null && !zip.trim().isEmpty()) {
            sb.append(" ").append(zip.trim());
        }
        return sb.toString().trim();
    }

    public String getTimeRangeLabel() {
        return formatTime(startHour24, startMinute) + " - " + formatTime(endHour24, endMinute);
    }

    public static String formatTime(int hour24, int minute) {
        if (hour24 < 0) return "Not set";
        int h = hour24 % 12;
        if (h == 0) h = 12;
        String ampm = hour24 < 12 ? "AM" : "PM";
        return String.format("%d:%02d %s", h, minute, ampm);
    }
}
