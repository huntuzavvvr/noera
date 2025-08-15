package com.noera.noera.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ProductVariant {
    @Id
    @GeneratedValue
    private Integer id;
    private BigDecimal price;
    private String color;
    private Integer quantity;
    private String imageUrl;
    private String hoverImageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="product_id")
    private Product product;
}
