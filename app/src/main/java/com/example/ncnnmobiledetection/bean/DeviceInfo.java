package com.example.ncnnmobiledetection.bean;


public class DeviceInfo {
    public int VID;
    public int PID;
    public String DeviceName;
    public String ProductName;

    public DeviceInfo() {
    }

    public int getVID() {
        return this.VID;
    }

    public void setVID(int VID) {
        this.VID = VID;
    }

    public int getPID() {
        return this.PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public void setProductName(String ProductName) {
        this.ProductName = ProductName;
    }

    public String getProductName() {
        return this.ProductName;
    }

    public void setDeviceName(String DeviceName) {
        this.DeviceName = DeviceName;
    }

    public String getDeviceName() {
        return this.DeviceName;
    }
}