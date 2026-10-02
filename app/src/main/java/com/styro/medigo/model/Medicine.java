package com.styro.medigo.model;

public class Medicine {

    private String medicineId;
    private String name;
    private String category;
    private String description;
    private String usageGuidelines;
    private String brand;
    private String strength;
    private double price;
    private int stockQuantity;
    private String imageUrl;
    private String outletId;
    private boolean requiresPrescription;
    private boolean isAvailable;
    private boolean isFeatured;
    private String type;
    private long updatedAt;

    public Medicine() {
    }

    public Medicine(String medicineId, String name, String category, double price) {
        this.medicineId = medicineId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.isAvailable = true;
        this.isFeatured = false;
        this.requiresPrescription = false;
        this.type = "medicine";
        this.updatedAt = System.currentTimeMillis();
    }


    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUsageGuidelines() {
        return usageGuidelines;
    }

    public void setUsageGuidelines(String usageGuidelines) {
        this.usageGuidelines = usageGuidelines;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOutletId() {
        return outletId;
    }

    public void setOutletId(String outletId) {
        this.outletId = outletId;
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

}