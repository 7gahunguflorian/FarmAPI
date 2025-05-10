package com.farm.delivery.farmapi.dto;

import com.farm.delivery.farmapi.model.DeliveryInfo;
import com.farm.delivery.farmapi.model.Order;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class OrderDTOs {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRequestDto {
        @NotEmpty(message = "Order items cannot be empty")
        private List<OrderItemDto> items;

        @NotBlank(message = "Delivery address is required")
        @Size(min = 10, max = 200, message = "Delivery address must be between 10 and 200 characters")
        private String deliveryAddress;

        @NotNull(message = "Estimated delivery time is required")
        @Future(message = "Estimated delivery time must be in the future")
        private LocalDateTime estimatedDeliveryTime;

        @Size(max = 1000, message = "Delivery notes cannot exceed 1000 characters")
        private String deliveryNotes;

        private Map<Long, Integer> productQuantities; // productId -> quantity
        private String deliveryInstructions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponseDto {
        private Long id;
        private Long clientId;
        private String clientName;
        private List<OrderProductDto> products;
        private Order.OrderStatus status;
        private String statusDescription;
        private String statusNotes;
        private LocalDateTime statusUpdateTime;
        private LocalDateTime orderDate;
        private BigDecimal totalPrice;
        private DeliveryInfoDto deliveryInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderProductDto {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryInfoDto {
        private String deliveryAddress;
        private LocalDateTime estimatedDeliveryTime;
        private LocalDateTime actualDeliveryTime;
        private String deliveryNotes;
        private DeliveryInfo.DeliveryStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDeliveryStatusDto {
        @NotNull(message = "Delivery status is required")
        private DeliveryInfo.DeliveryStatus status;

        @Size(max = 1000, message = "Delivery notes cannot exceed 1000 characters")
        private String deliveryNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusUpdateDto {
        @NotNull(message = "Order status is required")
        private Order.OrderStatus status;

        @Size(max = 1000, message = "Status notes cannot exceed 1000 characters")
        private String statusNotes;
    }
}
