package com.davidbadell.orderservice.repository;

import com.davidbadell.orderservice.entity.Order;
import com.davidbadell.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserId(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
