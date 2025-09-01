package com.noera.noera.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.noera.noera.model.Order;
import com.noera.noera.model.Order.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByOrderDateDesc();
    List<Order> findByStatus(OrderStatus status);
    Long countByStatus(OrderStatus status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    Double findTotalRevenue();
}