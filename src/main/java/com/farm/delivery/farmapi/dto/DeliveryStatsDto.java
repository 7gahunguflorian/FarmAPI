package com.farm.delivery.farmapi.dto;

public class DeliveryStatsDto {
    private String date;
    private int deliveries;
    
    // Constructor
    public DeliveryStatsDto(String date, int deliveries) {
        this.date = date;
        this.deliveries = deliveries;
    }
    
    // Default constructor
    public DeliveryStatsDto() {
    }
    
    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getDeliveries() { return deliveries; }
    public void setDeliveries(int deliveries) { this.deliveries = deliveries; }
} 