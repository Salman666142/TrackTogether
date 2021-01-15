package com.technicallskillz.tracktogether.Utills;

public class HotZonePeople {

    private double Lat;
    private double Long;
    private String type;
    private String status;

    public HotZonePeople(double lat, double aLong, String type, String status) {
        Lat = lat;
        Long = aLong;
        this.type = type;
        this.status = status;
    }

    public HotZonePeople() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
