package com.tracking.bustrackingsystem.Model;

public class Model_Route {
    public String getDb_key() {
        return db_key;
    }

    public void setDb_key(String db_key) {
        this.db_key = db_key;
    }


    public String db_key;

    public String getBus_full() {
        return bus_full;
    }

    public void setBus_full(String bus_full) {
        this.bus_full = bus_full;
    }

    public String bus_full;

    public String getBus_no() {
        return bus_no;
    }

    public void setBus_no(String bus_no) {
        this.bus_no = bus_no;
    }

    String bus_no;
    String driver;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public String getDest_name() {
        return dest_name;
    }

    public void setDest_name(String dest_name) {
        this.dest_name = dest_name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    String source;
    String dest;
    String source_name;
    String dest_name;
    String distance;
    String departure;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
