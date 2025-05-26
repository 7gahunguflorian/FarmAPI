package com.farm.delivery.farmapi.controller;

import com.farm.delivery.farmapi.dto.DashboardStatsDto;
import com.farm.delivery.farmapi.dto.DeliveryStatsDto;
import com.farm.delivery.farmapi.dto.PaymentStatsDTO;
import com.farm.delivery.farmapi.dto.OrderDTOs.*;
import com.farm.delivery.farmapi.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody OrderRequestDto orderRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest, userDetails.getUsername()));
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<OrderResponseDto>> getClientOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return ResponseEntity.ok(orderService.getClientOrders(userDetails.getUsername(), pageable));
    }

    @GetMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Page<OrderResponseDto>> getFarmerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return ResponseEntity.ok(orderService.getFarmerOrders(userDetails.getUsername(), pageable));
    }

    @PutMapping("/{id}/delivery-status")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<OrderResponseDto> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestBody UpdateDeliveryStatusDto updateDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.updateDeliveryStatus(id, updateDto, userDetails.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FARMER', 'CLIENT')")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateDto statusDto) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, statusDto));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(orderService.getDashboardStats());
    }

    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> getRecentOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return ResponseEntity.ok(orderService.getRecentOrders(pageable));
    }

    @GetMapping("/payment-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentStatsDTO>> getPaymentStats(@RequestParam String timeRange) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;
            
            // Handle both formats: "day:1" and "day"
            if (timeRange.contains(":")) {
                String[] parts = timeRange.split(":");
                String period = parts[0];
                int value = Integer.parseInt(parts[1]);
                
                switch (period) {
                    case "day":
                        startDate = endDate.minusDays(value);
                        break;
                    case "week":
                        startDate = endDate.minusWeeks(value);
                        break;
                    case "month":
                        startDate = endDate.minusMonths(value);
                        break;
                    case "year":
                        startDate = endDate.minusYears(value);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid time range period: " + period);
                }
            } else {
                // Default to last period if only period is provided
                switch (timeRange) {
                    case "day":
                        startDate = endDate.minusDays(1);
                        break;
                    case "week":
                        startDate = endDate.minusWeeks(1);
                        break;
                    case "month":
                        startDate = endDate.minusMonths(1);
                        break;
                    case "year":
                        startDate = endDate.minusYears(1);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid time range period: " + timeRange);
                }
            }
            
            List<PaymentStatsDTO> stats = orderService.getPaymentStats(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delivery-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeliveryStatsDto>> getDeliveryStats(@RequestParam String timeRange) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;
            
            // Handle both formats: "day:1" and "day"
            if (timeRange.contains(":")) {
                String[] parts = timeRange.split(":");
                String period = parts[0];
                int value = Integer.parseInt(parts[1]);
                
                switch (period) {
                    case "day":
                        startDate = endDate.minusDays(value);
                        break;
                    case "week":
                        startDate = endDate.minusWeeks(value);
                        break;
                    case "month":
                        startDate = endDate.minusMonths(value);
                        break;
                    case "year":
                        startDate = endDate.minusYears(value);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid time range period: " + period);
                }
            } else {
                // Default to last period if only period is provided
                switch (timeRange) {
                    case "day":
                        startDate = endDate.minusDays(1);
                        break;
                    case "week":
                        startDate = endDate.minusWeeks(1);
                        break;
                    case "month":
                        startDate = endDate.minusMonths(1);
                        break;
                    case "year":
                        startDate = endDate.minusYears(1);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid time range period: " + timeRange);
                }
            }
            
            List<DeliveryStatsDto> stats = orderService.getDeliveryStats(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
