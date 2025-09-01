package com.noera.noera.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Название товара обязательно")
    private String productName;
    
    @NotBlank(message = "Размер обязателен")
    private String size;
    
    @NotBlank(message = "Цвет обязателен")
    private String color;
    
    private Integer quantity;
    private Double price;
    
    // конструкторы, геттеры, сеттеры
}