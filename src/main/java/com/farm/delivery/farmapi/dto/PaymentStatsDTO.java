package com.farm.delivery.farmapi.dto;

public class PaymentStatsDTO {
    private String date;
    private double income;
    private double outgoing;
    
    // Constructor
    public PaymentStatsDTO(String date, double income, double outgoing) {
        this.date = date;
        this.income = income;
        this.outgoing = outgoing;
    }
    
    // Default constructor
    public PaymentStatsDTO() {
    }
    
    // Getters and setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getIncome() { return income; }
    public void setIncome(double income) { this.income = income; }
    public double getOutgoing() { return outgoing; }
    public void setOutgoing(double outgoing) { this.outgoing = outgoing; }
} 