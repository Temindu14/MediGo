package com.styro.medigo.model;

public class Outlet {

    private String outletId;
    private String name;
    private String address;
    private String phone;
    private double latitude;
    private double longitude;
    private boolean isOpen;
    private String openingTime;
    private String closingTime;
    private String managerId;

    public Outlet() {
    }

    public Outlet(String outletId, String name, String address, String phone,
                  double latitude, double longitude) {
        this.outletId = outletId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOpen = true;
    }


    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

}