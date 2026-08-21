package com.styro.medigo.model;

import java.util.List;

public class Order {

    private String orderId;
    private String userId;
    private String outletId;
    private List<CartItem> items;
    private double subtotal;
    private double deliveryFee;
    private double totalAmount;
    private String deliveryAddress;
    private String paymentMethod;
    private String transactionId;   // PayHere transaction ID (empty string for COD)
    private String status;
    private String prescriptionUrl;
    private double deliveryLatitude;
    private double deliveryLongitude;
    private String representativeId;
    private long createdAt;
    private long updatedAt;



    // Order status constants
    public static final String STATUS_PENDING          = "pending";
    public static final String STATUS_CONFIRMED        = "confirmed";
    public static final String STATUS_PROCESSING       = "processing";
    public static final String STATUS_OUT_FOR_DELIVERY = "out_for_delivery";
    public static final String STATUS_DELIVERED        = "delivered";
    public static final String STATUS_CANCELLED        = "cancelled";

    public Order() {
        // Required empty constructor for Firebase
    }

    public Order(String orderId, String userId, String outletId, List<CartItem> items,
                 double totalAmount, String deliveryAddress, String paymentMethod) {
        this.orderId         = orderId;
        this.userId          = userId;
        this.outletId        = outletId;
        this.items           = items;
        this.totalAmount     = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod   = paymentMethod;
        this.transactionId   = "";
        this.status          = STATUS_PENDING;
        this.createdAt       = System.currentTimeMillis();
        this.updatedAt       = System.currentTimeMillis();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOutletId() { return outletId; }
    public void setOutletId(String outletId) { this.outletId = outletId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status    = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPrescriptionUrl() { return prescriptionUrl; }
    public void setPrescriptionUrl(String prescriptionUrl) { this.prescriptionUrl = prescriptionUrl; }

    public double getDeliveryLatitude() { return deliveryLatitude; }
    public void setDeliveryLatitude(double deliveryLatitude) { this.deliveryLatitude = deliveryLatitude; }

    public double getDeliveryLongitude() { return deliveryLongitude; }
    public void setDeliveryLongitude(double deliveryLongitude) { this.deliveryLongitude = deliveryLongitude; }

    public String getRepresentativeId() { return representativeId; }
    public void setRepresentativeId(String representativeId) { this.representativeId = representativeId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}