package com.technicallskillz.tracktogether.Utills;

public class HotZone {
    private double Lat;
    private double Long;

    public HotZone(double lat, double aLong) {
        Lat = lat;
        Long = aLong;
    }

    public HotZone() {
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLong() {
        return Long;
    }

    public void setLong(double aLong) {
        Long = aLong;
    }
}
