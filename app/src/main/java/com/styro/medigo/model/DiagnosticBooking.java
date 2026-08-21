package com.styro.medigo.model;

public class DiagnosticBooking {

    private String bookingId;
    private String userId;
    private String outletId;
    private String testName;
    private String testDescription;
    private double price;
    private String mode;
    private String address;
    private double latitude;
    private double longitude;
    private String scheduledDate;
    private String scheduledTime;
    private String status;
    private String representativeId;
    private String specialInstructions;
    private long createdAt;
    private long updatedAt;

    public static final String MODE_HOME_VISIT    = "home_visit";
    public static final String MODE_SELF_TEST_KIT = "self_test_kit";

    public static final String STATUS_PENDING   = "pending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_ASSIGNED  = "assigned";
    public static final String STATUS_EN_ROUTE  = "en_route";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    public DiagnosticBooking() {}

    public DiagnosticBooking(String bookingId, String userId, String outletId,
                             String testName, double price, String mode,
                             String address, String scheduledDate, String scheduledTime) {
        this.bookingId     = bookingId;
        this.userId        = userId;
        this.outletId      = outletId;
        this.testName      = testName;
        this.price         = price;
        this.mode          = mode;
        this.address       = address;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.status        = STATUS_PENDING;
        this.createdAt     = System.currentTimeMillis();
        this.updatedAt     = System.currentTimeMillis();
    }

    public String getBookingId(){
        return bookingId; }
    public void setBookingId(String bookingId){
        this.bookingId = bookingId;
    }

    public String getUserId(){
        return userId;
    }
    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getOutletId(){
        return outletId;
    }
    public void setOutletId(String outletId){
        this.outletId = outletId;
    }

    public String getTestName(){
        return testName;
    }
    public void setTestName(String testName){
        this.testName = testName;
    }

    public String getTestDescription(){
        return testDescription;
    }
    public void setTestDescription(String testDescription){
        this.testDescription = testDescription;
    }

    public double getPrice(){
        return price;
    }
    public void setPrice(double price){
        this.price = price;
    }

    public String getMode(){
        return mode;
    }
    public void setMode(String mode){
        this.mode = mode;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude(){
        return latitude;
    }
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLongitude(){
        return longitude;
    }
    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public String getScheduledDate(){
        return scheduledDate;
    }
    public void setScheduledDate(String scheduledDate){
        this.scheduledDate = scheduledDate;
    }

    public String getScheduledTime(){
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status; this.updatedAt = System.currentTimeMillis();
    }

    public String getRepresentativeId(){
        return representativeId;
    }
    public void setRepresentativeId(String id){
        this.representativeId = id;
    }

    public String getSpecialInstructions(){
        return specialInstructions;
    }
    public void setSpecialInstructions(String si){
        this.specialInstructions = si;
    }

    public long getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(long createdAt){
        this.createdAt = createdAt;
    }

    public long getUpdatedAt(){
        return updatedAt;
    }
    public void setUpdatedAt(long updatedAt){
        this.updatedAt = updatedAt;
    }
}