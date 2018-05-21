package com.example.danlaw.demo.model;

public class DataLogger {
    private String name;
    private String address;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public DataLogger(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
