package com.planbookai.backend.service;

import com.planbookai.backend.dto.OrderDTO;
import com.planbookai.backend.dto.OrderRequest;
import com.planbookai.backend.dto.PackageDTO;
import com.planbookai.backend.dto.PackageRequest;
import com.planbookai.backend.model.entity.Order;
import com.planbookai.backend.model.entity.SubscriptionPackage;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.OrderRepository;
import com.planbookai.backend.repository.SubscriptionPackageRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionPackageRepository packageRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionPackageRepository packageRepository, OrderRepository orderRepository, UserRepository userRepository) {
        this.packageRepository = packageRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    // ===================================
    // PACKAGES
    // ===================================

    public List<PackageDTO> getAllPackages() {
        return packageRepository.findAll().stream().map(this::mapToPackageDTO).collect(Collectors.toList());
    }

    public PackageDTO createPackage(PackageRequest request, User user) {
        SubscriptionPackage pkg = SubscriptionPackage.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .features(request.getFeatures())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(user)
                .build();
        return mapToPackageDTO(packageRepository.save(pkg));
    }

    public PackageDTO updatePackage(Integer id, PackageRequest request) {
        SubscriptionPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        
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
        packageRepository.deleteById(id);
    }

    public PackageDTO deactivatePackage(Integer id) {
        SubscriptionPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
        pkg.setIsActive(false);
        return mapToPackageDTO(packageRepository.save(pkg));
    }

    private PackageDTO mapToPackageDTO(SubscriptionPackage pkg) {
        return PackageDTO.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .price(pkg.getPrice())
                .durationDays(pkg.getDurationDays())
                .features(pkg.getFeatures())
                .isActive(pkg.getIsActive())
                .createdAt(pkg.getCreatedAt())
                .build();
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
        SubscriptionPackage pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (!pkg.getIsActive()) {
            throw new RuntimeException("This package is no longer active");
        }

        Order order = Order.builder()
                .user(user)
                .subscriptionPackage(pkg)
                .status(Order.OrderStatus.PENDING)
                .amountPaid(pkg.getPrice())
                .paymentMethod(request.getPaymentMethod())
                .build();
        
        return mapToOrderDTO(orderRepository.save(order));
    }

    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        order.setStatus(newStatus);

        // If active, set started and expired dates
        if (newStatus == Order.OrderStatus.ACTIVE && order.getStartedAt() == null) {
            order.setStartedAt(LocalDateTime.now());
            order.setExpiresAt(LocalDateTime.now().plusDays(order.getSubscriptionPackage().getDurationDays()));
        }

        return mapToOrderDTO(orderRepository.save(order));
    }

    private OrderDTO mapToOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .userFullName(order.getUser().getFullName())
                .packageId(order.getSubscriptionPackage().getId())
                .packageName(order.getSubscriptionPackage().getName())
                .status(order.getStatus().name())
                .amountPaid(order.getAmountPaid())
                .paymentMethod(order.getPaymentMethod())
                .startedAt(order.getStartedAt())
                .expiresAt(order.getExpiresAt())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
