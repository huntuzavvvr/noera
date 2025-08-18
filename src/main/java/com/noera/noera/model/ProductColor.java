package com.noera.noera.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ProductColor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String colorName;
    private Integer quantity;
    private String imageUrl;
    private String hoverImageUrl;
    private String colorHex;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private ProductSize productSize;
}
