package com.noera.noera.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Column;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.OneToMany;

@Data
@Entity
@Table(name = "orders")
public class Order {
    public enum OrderStatus {
        PENDING("Ожидает обработки"),
        CONFIRMED("Подтвержден"),
        COMPLETED("Завершен"),
        CANCELLED("Отменен");
        
        private final String displayName;
        
        OrderStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Имя клиента обязательно")
    private String customerName;
    
    @NotBlank(message = "Телефон обязателен")
    private String phone;
    
    private String address;
    private String notes;
    private LocalDateTime orderDate;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();
    
    private Double totalAmount;
    private String managerNotes;
    
    // конструкторы, геттеры, сеттеры
    
    public void calculateTotal() {
        this.totalAmount = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
}