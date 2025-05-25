package com.farm.delivery.farmapi.service;

import com.farm.delivery.farmapi.dto.OrderDTOs.*;
import com.farm.delivery.farmapi.dto.ProductDTOs.*;
import com.farm.delivery.farmapi.dto.DashboardStatsDto;
import com.farm.delivery.farmapi.dto.DeliveryStatsDto;
import com.farm.delivery.farmapi.dto.PaymentStatsDTO;
import com.farm.delivery.farmapi.model.DeliveryInfo;
import com.farm.delivery.farmapi.model.Order;
import com.farm.delivery.farmapi.model.Product;
import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.repository.OrderRepository;
import com.farm.delivery.farmapi.repository.ProductRepository;
import com.farm.delivery.farmapi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!currentUser.getRole().equals(User.Role.CLIENT)) {
            throw new AccessDeniedException("Only clients can create orders");
        }

        Order order = new Order();
        order.setClient(currentUser);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        Map<Product, Integer> productQuantities = new HashMap<>();
        List<Product> products = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemDto item : requestDto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + item.getProductId()));

            if (product.getAvailableQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Not enough quantity available for product: " + product.getName());
            }

            productQuantities.put(product, item.getQuantity());
            products.add(product);
            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            product.setAvailableQuantity(product.getAvailableQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        order.setProducts(products);
        order.setProductQuantities(productQuantities);
        order.setTotalPrice(totalPrice);

        DeliveryInfo deliveryInfo = new DeliveryInfo();
        deliveryInfo.setOrder(order);
        deliveryInfo.setDeliveryAddress(requestDto.getDeliveryAddress());
        deliveryInfo.setEstimatedDeliveryTime(requestDto.getEstimatedDeliveryTime());
        deliveryInfo.setDeliveryNotes(requestDto.getDeliveryNotes());
        deliveryInfo.setStatus(DeliveryInfo.DeliveryStatus.PENDING);
        order.setDeliveryInfo(deliveryInfo);

        Order savedOrder = orderRepository.save(order);
        return convertToOrderResponseDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getClientOrders(String username, Pageable pageable) {
        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!client.getRole().equals(User.Role.CLIENT)) {
            throw new AccessDeniedException("Only clients can view their orders");
        }

        return orderRepository.findByClient(client, pageable)
                .map(this::convertToOrderResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getFarmerOrders(String username, Pageable pageable) {
        User farmer = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!farmer.getRole().equals(User.Role.FARMER)) {
            throw new AccessDeniedException("Only farmers can view their orders");
        }

        return orderRepository.findByProductsFarmer(farmer, pageable)
                .map(this::convertToOrderResponseDto);
    }

    @Transactional
    public OrderResponseDto updateDeliveryStatus(Long orderId, UpdateDeliveryStatusDto updateDto, String username) {
        User farmer = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!farmer.getRole().equals(User.Role.FARMER)) {
            throw new AccessDeniedException("Only farmers can update delivery status");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getProducts().stream().anyMatch(p -> p.getOwner().equals(farmer))) {
            throw new AccessDeniedException("You can only update status for orders containing your products");
        }

        DeliveryInfo deliveryInfo = order.getDeliveryInfo();
        deliveryInfo.setStatus(updateDto.getStatus());
        if (updateDto.getDeliveryNotes() != null) {
            deliveryInfo.setDeliveryNotes(updateDto.getDeliveryNotes());
        }

        if (updateDto.getStatus() == DeliveryInfo.DeliveryStatus.DELIVERED) {
            deliveryInfo.setActualDeliveryTime(LocalDateTime.now());
            order.setStatus(Order.OrderStatus.DELIVERED);
        }

        Order savedOrder = orderRepository.save(order);
        return convertToOrderResponseDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToOrderResponseDto);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        return convertToOrderResponseDto(order);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long id, OrderStatusUpdateDto statusDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        order.setStatus(statusDto.getStatus());
        Order savedOrder = orderRepository.save(order);
        return convertToOrderResponseDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        long totalOrders = orderRepository.count();
        long totalSuccessfulDeliveries = orderRepository.countByStatus(Order.OrderStatus.DELIVERED);
        long totalFarmers = userRepository.countByRole(User.Role.FARMER);
        long totalClients = userRepository.countByRole(User.Role.CLIENT);

        return new DashboardStatsDto(
            totalOrders,
            totalSuccessfulDeliveries,
            totalFarmers,
            totalClients
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getRecentOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderDateDesc(pageable)
            .map(this::convertToOrderResponseDto);
    }

    @Transactional(readOnly = true)
    public List<DeliveryStatsDto> getDeliveryStats(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        List<Order> orders = orderRepository.findByOrderDateBetween(
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        );
        
        // Group orders by date and count deliveries
        Map<LocalDate, Integer> deliveriesMap = new HashMap<>();
        
        // Initialize all dates in the range to ensure continuous data
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            deliveriesMap.put(currentDate, 0);
            currentDate = currentDate.plusDays(1);
        }
        
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                LocalDate deliveryDate = order.getOrderDate().toLocalDate();
                deliveriesMap.merge(deliveryDate, 1, Integer::sum);
            }
        }
        
        return deliveriesMap.entrySet().stream()
            .map(entry -> new DeliveryStatsDto(entry.getKey().toString(), entry.getValue()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentStatsDTO> getPaymentStats(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        List<Order> orders = orderRepository.findByOrderDateBetween(
            startDate.atStartOfDay(),
            endDate.atTime(23, 59, 59)
        );
        
        // Group orders by date and calculate stats
        Map<LocalDate, PaymentStatsDTO> statsMap = new HashMap<>();
        
        // Initialize all dates in the range to ensure continuous data
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            statsMap.put(currentDate, new PaymentStatsDTO(currentDate.toString(), 0.0, 0.0));
            currentDate = currentDate.plusDays(1);
        }
        
        for (Order order : orders) {
            LocalDate orderDate = order.getOrderDate().toLocalDate();
            PaymentStatsDTO stats = statsMap.get(orderDate);
            
            if (stats != null) {
                if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                    stats.setIncome(stats.getIncome() + order.getTotalPrice().doubleValue());
                } else if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                    stats.setOutgoing(stats.getOutgoing() + order.getTotalPrice().doubleValue());
                }
            }
        }
        
        return new ArrayList<>(statsMap.values());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
    }

    private LocalDate[] getDateRangeFromTimeRange(String timeRange) {
        String[] parts = timeRange.split(":");
        String period = parts[0];
        int value = Integer.parseInt(parts[1]);
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;
        
        switch (period.toLowerCase()) {
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
                throw new IllegalArgumentException("Invalid time range period. Must be one of: week, month, year");
        }
        
        return new LocalDate[]{startDate, endDate};
    }

    private OrderResponseDto convertToOrderResponseDto(Order order) {
        OrderResponseDto responseDto = new OrderResponseDto();
        setBasicOrderInfo(responseDto, order);
        setProductInfo(responseDto, order);
        setDeliveryInfo(responseDto, order);
        return responseDto;
    }

    private void setBasicOrderInfo(OrderResponseDto responseDto, Order order) {
        responseDto.setId(order.getId());
        responseDto.setClientName(order.getClient().getUsername());
        responseDto.setStatus(order.getStatus());
        responseDto.setStatusDescription(order.getStatus().getDescription());
        responseDto.setStatusNotes(order.getStatusNotes());
        responseDto.setStatusUpdateTime(order.getStatusUpdateTime());
        responseDto.setOrderDate(order.getOrderDate());
        responseDto.setTotalPrice(order.getTotalPrice());
    }

    private void setProductInfo(OrderResponseDto responseDto, Order order) {
        List<OrderProductDto> productDtos = order.getProducts().stream()
                .map(product -> {
                    OrderProductDto productDto = new OrderProductDto();
                    productDto.setId(product.getId());
                    productDto.setName(product.getName());
                    productDto.setPrice(product.getPrice());
                    
                    Integer quantity = order.getProductQuantities().get(product);
                    if (quantity == null) {
                        quantity = order.getProductQuantities().entrySet().stream()
                                .filter(entry -> entry.getKey().getId().equals(product.getId()))
                                .map(Map.Entry::getValue)
                                .findFirst()
                                .orElse(0);
                    }
                    
                    productDto.setQuantity(quantity);
                    productDto.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
                    return productDto;
                })
                .collect(Collectors.toList());
        responseDto.setProducts(productDtos);
    }

    private void setDeliveryInfo(OrderResponseDto responseDto, Order order) {
        if (order.getDeliveryInfo() != null) {
            DeliveryInfoDto deliveryInfoDto = new DeliveryInfoDto();
            deliveryInfoDto.setDeliveryAddress(order.getDeliveryInfo().getDeliveryAddress());
            deliveryInfoDto.setEstimatedDeliveryTime(order.getDeliveryInfo().getEstimatedDeliveryTime());
            deliveryInfoDto.setActualDeliveryTime(order.getDeliveryInfo().getActualDeliveryTime());
            deliveryInfoDto.setDeliveryNotes(order.getDeliveryInfo().getDeliveryNotes());
            deliveryInfoDto.setStatus(order.getDeliveryInfo().getStatus());
            responseDto.setDeliveryInfo(deliveryInfoDto);
        }
    }
}
