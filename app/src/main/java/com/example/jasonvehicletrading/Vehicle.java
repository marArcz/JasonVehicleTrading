package com.example.jasonvehicletrading;

public class Vehicle {
    private String id, unit,chassis_number,specs,price, updated_price,photo;
    private int year_model,container_number;
    private boolean is_sold;
    private boolean deleted = false;

    public Vehicle() {
    }

    public Vehicle(String id, String unit, String chassis_number, String specs, String price, String updated_price, int year_model, int container_number, boolean is_sold,String photo) {
        this.id = id;
        this.unit = unit;
        this.chassis_number = chassis_number;
        this.specs = specs;
        this.price = price;
        this.updated_price = updated_price;
        this.year_model = year_model;
        this.container_number = container_number;
        this.is_sold = is_sold;
        this.photo = photo;
    }

    public Vehicle(String unit, String chassis_number, String specs, String price, String updated_price, int year_model, int container_number, boolean is_sold, String photo) {
        this.unit = unit;
        this.chassis_number = chassis_number;
        this.specs = specs;
        this.price = price;
        this.updated_price = updated_price;
        this.year_model = year_model;
        this.container_number = container_number;
        this.is_sold = is_sold;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getChassis_number() {
        return chassis_number;
    }

    public void setChassis_number(String chassis_number) {
        this.chassis_number = chassis_number;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUpdated_price() {
        return updated_price;
    }

    public void setUpdated_price(String updated_price) {
        this.updated_price = updated_price;
    }

    public int getYear_model() {
        return year_model;
    }

    public void setYear_model(int year_model) {
        this.year_model = year_model;
    }

    public int getContainer_number() {
        return container_number;
    }

    public void setContainer_number(int container_number) {
        this.container_number = container_number;
    }

    public boolean isIs_sold() {
        return is_sold;
    }

    public void setIs_sold(boolean is_sold) {
        this.is_sold = is_sold;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
