package com.noera.noera.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class ProductSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String sizeName; // "S", "M", "L" и т.д.
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @OneToMany(mappedBy = "productSize", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductColor> colors = new ArrayList<>();
}