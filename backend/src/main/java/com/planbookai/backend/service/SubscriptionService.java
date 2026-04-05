package com.planbookai.backend.service;

import com.planbookai.backend.dto.OrderDTO;
import com.planbookai.backend.dto.OrderRequest;
import com.planbookai.backend.dto.PackageDTO;
import com.planbookai.backend.dto.PackageRequest;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.Order;
import com.planbookai.backend.model.entity.SubscriptionPackage;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.OrderRepository;
import com.planbookai.backend.repository.SubscriptionPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionPackageRepository packageRepository;
    private final OrderRepository orderRepository;

    public SubscriptionService(SubscriptionPackageRepository packageRepository, OrderRepository orderRepository) {
        this.packageRepository = packageRepository;
        this.orderRepository = orderRepository;
    }

    // ===================================
    // PACKAGES
    // ===================================

    public List<PackageDTO> getAllPackages() {
        return packageRepository.findAll().stream().map(this::mapToPackageDTO).collect(Collectors.toList());
    }

    public PackageDTO createPackage(PackageRequest request, User user) {
        validatePackageRequest(request);

        SubscriptionPackage pkg = new SubscriptionPackage();
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setPrice(request.getPrice());
        pkg.setDurationDays(request.getDurationDays());
        pkg.setFeatures(request.getFeatures());
        pkg.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        pkg.setCreatedBy(user);
        return mapToPackageDTO(packageRepository.save(pkg));
    }

    public PackageDTO updatePackage(Integer id, PackageRequest request) {
        validatePackageRequest(request);
        SubscriptionPackage pkg = findPackageOrThrow(id);
        
        pkg.setName(request.getName());
        pkg.setDescription(request.getDescription());
        pkg.setPrice(request.getPrice());
        pkg.setDurationDays(request.getDurationDays());
        pkg.setFeatures(request.getFeatures());
        if(request.getIsActive() != null) {
            pkg.setIsActive(request.getIsActive());
        }
        
        return mapToPackageDTO(packageRepository.save(pkg));
    }

    public void deletePackage(Integer id) {
        packageRepository.delete(findPackageOrThrow(id));
    }

    public PackageDTO deactivatePackage(Integer id) {
        SubscriptionPackage pkg = findPackageOrThrow(id);
        pkg.setIsActive(false);
        return mapToPackageDTO(packageRepository.save(pkg));
    }

    private PackageDTO mapToPackageDTO(SubscriptionPackage pkg) {
        PackageDTO dto = new PackageDTO();
        dto.setId(pkg.getId());
        dto.setName(pkg.getName());
        dto.setDescription(pkg.getDescription());
        dto.setPrice(pkg.getPrice());
        dto.setDurationDays(pkg.getDurationDays());
        dto.setFeatures(pkg.getFeatures());
        dto.setIsActive(pkg.getIsActive());
        dto.setCreatedAt(pkg.getCreatedAt());
        return dto;
    }

    // ===================================
    // ORDERS
    // ===================================

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToOrderDTO).collect(Collectors.toList());
    }

    public List<OrderDTO> getMyOrders(User user) {
        return orderRepository.findByUserId(user.getId()).stream().map(this::mapToOrderDTO).collect(Collectors.toList());
    }

    public OrderDTO createOrder(OrderRequest request, User user) {
        validateOrderRequest(request);
        SubscriptionPackage pkg = findPackageOrThrow(request.getPackageId());

        if (!pkg.getIsActive()) {
            throw new IllegalArgumentException("This package is no longer active");
        }

        Order order = new Order();
        order.setUser(user);
        order.setSubscriptionPackage(pkg);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setAmountPaid(pkg.getPrice());
        order.setPaymentMethod(request.getPaymentMethod());
        
        return mapToOrderDTO(orderRepository.save(order));
    }

    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = findOrderOrThrow(orderId);
        Order.OrderStatus newStatus = parseOrderStatus(status);
        order.setStatus(newStatus);

        // If active, set started and expired dates
        if (newStatus == Order.OrderStatus.ACTIVE && order.getStartedAt() == null) {
            order.setStartedAt(LocalDateTime.now());
            order.setExpiresAt(LocalDateTime.now().plusDays(order.getSubscriptionPackage().getDurationDays()));
        }

        return mapToOrderDTO(orderRepository.save(order));
    }

    private SubscriptionPackage findPackageOrThrow(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("packageId is required");
        }
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));
    }

    private Order findOrderOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("orderId is required");
        }
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private Order.OrderStatus parseOrderStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("status is required");
        }
        try {
            return Order.OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private void validateOrderRequest(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
        if (request.getPackageId() == null) {
            throw new IllegalArgumentException("packageId is required");
        }
        if (!StringUtils.hasText(request.getPaymentMethod())) {
            throw new IllegalArgumentException("paymentMethod is required");
        }
    }

    private void validatePackageRequest(PackageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Package request is required");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("name is required");
        }
        if (request.getPrice() == null) {
            throw new IllegalArgumentException("price is required");
        }
        if (request.getPrice().signum() < 0) {
            throw new IllegalArgumentException("price must be greater than or equal to 0");
        }
        if (request.getDurationDays() == null || request.getDurationDays() < 1) {
            throw new IllegalArgumentException("durationDays must be greater than or equal to 1");
        }
    }

    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserEmail(order.getUser().getEmail());
        dto.setUserFullName(order.getUser().getFullName());
        dto.setPackageId(order.getSubscriptionPackage().getId());
        dto.setPackageName(order.getSubscriptionPackage().getName());
        dto.setStatus(order.getStatus().name());
        dto.setAmountPaid(order.getAmountPaid());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setStartedAt(order.getStartedAt());
        dto.setExpiresAt(order.getExpiresAt());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }
}
