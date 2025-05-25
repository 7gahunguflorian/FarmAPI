package com.farm.delivery.farmapi.repository;

import com.farm.delivery.farmapi.model.Order;
import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.dto.DeliveryStatsDto;
import com.farm.delivery.farmapi.dto.PaymentStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    long countByStatus(Order.OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    @Query("SELECT new com.farm.delivery.farmapi.dto.DeliveryStatsDto(CAST(o.orderDate AS string), COUNT(o)) " +
           "FROM Order o " +
           "WHERE CAST(o.orderDate AS date) BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(o.orderDate AS date) " +
           "ORDER BY CAST(o.orderDate AS date)")
    List<DeliveryStatsDto> getDeliveryStatsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT new com.farm.delivery.farmapi.dto.PaymentStatsDTO(" +
           "CAST(o.orderDate AS string), " +
           "SUM(CASE WHEN o.status = 'DELIVERED' THEN o.totalPrice ELSE 0 END), " +
           "SUM(CASE WHEN o.status = 'CANCELLED' THEN o.totalPrice ELSE 0 END)) " +
           "FROM Order o " +
           "WHERE CAST(o.orderDate AS date) BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(o.orderDate AS date) " +
           "ORDER BY CAST(o.orderDate AS date)")
    List<PaymentStatsDTO> getPaymentStatsByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
