package com.styro.medigo.model;

public class CartItem {

    private int id;
    private String medicineId;
    private String medicineName;
    private String medicineImageUrl;
    private double price;
    private int quantity;
    private boolean requiresPrescription;
    private String prescriptionPath;

    public CartItem() {
        // Default constructor
    }

    public CartItem(String medicineId, String medicineName, String medicineImageUrl,
                    double price, int quantity, boolean requiresPrescription) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.medicineImageUrl = medicineImageUrl;
        this.price = price;
        this.quantity = quantity;
        this.requiresPrescription = requiresPrescription;
    }

    public double getTotalPrice() {
        return price * quantity;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineImageUrl() {
        return medicineImageUrl;
    }

    public void setMedicineImageUrl(String medicineImageUrl) {
        this.medicineImageUrl = medicineImageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }

    public String getPrescriptionPath() {
        return prescriptionPath;
    }

    public void setPrescriptionPath(String prescriptionPath) {
        this.prescriptionPath = prescriptionPath;
    }

}