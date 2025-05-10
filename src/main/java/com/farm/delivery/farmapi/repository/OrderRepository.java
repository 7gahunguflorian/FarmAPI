package com.farm.delivery.farmapi.repository;

import com.farm.delivery.farmapi.model.Order;
import com.farm.delivery.farmapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClient(User client);

    @Query("SELECT o FROM Order o JOIN o.products p WHERE p.owner.id = :farmerId")
    List<Order> findOrdersByFarmerId(@Param("farmerId") Long farmerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.products p WHERE p.owner = :farmer")
    List<Order> findByProductsFarmer(User farmer);

    Page<Order> findByClient(User client, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.products p WHERE p.owner = :farmer")
    Page<Order> findByProductsFarmer(@Param("farmer") User farmer, Pageable pageable);
}
