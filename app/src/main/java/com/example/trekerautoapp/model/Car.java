package com.example.trekerautoapp.model;

public class Car {
    private String id;
    private String brand;
    private String model;
    private String year;
    private String plate;
    private long mileage;
    private long createdAt;

    public Car() {
    }

    public Car(String brand, String model, String year, String plate, long mileage, long createdAt) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.plate = plate;
        this.mileage = mileage;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public long getMileage() {
        return mileage;
    }

    public void setMileage(long mileage) {
        this.mileage = mileage;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
