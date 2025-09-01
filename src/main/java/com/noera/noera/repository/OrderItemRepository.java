package com.noera.noera.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.noera.noera.model.Order;

public interface OrderItemRepository extends JpaRepository<Order, Long>{
    
}
