package com.farm.delivery.farmapi.repository;

import com.farm.delivery.farmapi.model.Product;
import com.farm.delivery.farmapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByOwner(User owner);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByOwnerId(Long ownerId);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
