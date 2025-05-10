package com.farm.delivery.farmapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToMany
    @JoinTable(
        name = "order_products",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private DeliveryInfo deliveryInfo;

    @ElementCollection
    @CollectionTable(name = "order_product_quantities", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyJoinColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Product, Integer> productQuantities = new HashMap<>();

    @Column
    private String statusNotes;

    @Column
    private LocalDateTime statusUpdateTime;

    public enum OrderStatus {
        PENDING("Order received, waiting for confirmation"),
        CONFIRMED("Order confirmed by farmer"),
        PREPARING("Order is being prepared"),
        READY_FOR_DELIVERY("Order is ready for delivery"),
        IN_DELIVERY("Order is out for delivery"),
        DELIVERED("Order has been delivered"),
        CANCELLED("Order has been cancelled");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void updateStatus(OrderStatus newStatus, String notes) {
        this.status = newStatus;
        this.statusNotes = notes;
        this.statusUpdateTime = LocalDateTime.now();
    }
}