package com.farm.delivery.farmapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class ProductDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRequestDto {
        private String name;
        private BigDecimal price;
        private String description;
        private Integer availableQuantity;
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductResponseDto {
        private Long id;
        private String name;
        private BigDecimal price;
        private String description;
        private Long ownerId;
        private String ownerName;
        private Integer availableQuantity;
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProductImageDto {
        private String imageUrl;
    }
}
