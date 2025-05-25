package com.farm.delivery.farmapi.dto;

public class DashboardStatsDto {
    private long totalOrders;
    private long totalSuccessfulDeliveries;
    private long totalFarmers;
    private long totalClients;

    public DashboardStatsDto() {
    }

    public DashboardStatsDto(long totalOrders, long totalSuccessfulDeliveries, long totalFarmers, long totalClients) {
        this.totalOrders = totalOrders;
        this.totalSuccessfulDeliveries = totalSuccessfulDeliveries;
        this.totalFarmers = totalFarmers;
        this.totalClients = totalClients;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalSuccessfulDeliveries() {
        return totalSuccessfulDeliveries;
    }

    public void setTotalSuccessfulDeliveries(long totalSuccessfulDeliveries) {
        this.totalSuccessfulDeliveries = totalSuccessfulDeliveries;
    }

    public long getTotalFarmers() {
        return totalFarmers;
    }

    public void setTotalFarmers(long totalFarmers) {
        this.totalFarmers = totalFarmers;
    }

    public long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(long totalClients) {
        this.totalClients = totalClients;
    }
} 