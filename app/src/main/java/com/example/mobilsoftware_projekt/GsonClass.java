package com.example.mobilsoftware_projekt;

public class GsonClass {
    private String id, verkehrsmittel, duration, length, date, track, start, ende;

    public GsonClass(String id, String verkehrsmittel, String duration, String length,
                     String date, String track, String start, String ende){
        this.id = id;
        this.verkehrsmittel = verkehrsmittel;
        this.duration = duration;
        this.length = length;
        this.date = date;
        this.track = track;
        this.start = start;
        this.ende = ende;
    }
}
