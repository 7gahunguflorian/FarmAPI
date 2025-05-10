package com.farm.delivery.farmapi.controller;

import com.farm.delivery.farmapi.dto.OrderDTOs.*;
import com.farm.delivery.farmapi.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FARMER', 'CLIENT')")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody OrderRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.createOrder(requestDto, userDetails.getUsername()));
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<OrderResponseDto>> getClientOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getClientOrders(userDetails.getUsername(), pageable));
    }

    @GetMapping("/farmer")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<Page<OrderResponseDto>> getFarmerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getFarmerOrders(userDetails.getUsername(), pageable));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateDto statusDto) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, statusDto));
    }

    @PutMapping("/{orderId}/delivery-status")
    @PreAuthorize("hasRole('FARMER')")
    public ResponseEntity<OrderResponseDto> updateDeliveryStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateDeliveryStatusDto updateDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.updateDeliveryStatus(orderId, updateDto, userDetails.getUsername()));
    }
}
