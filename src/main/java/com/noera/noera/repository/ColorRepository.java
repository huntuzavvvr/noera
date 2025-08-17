package com.noera.noera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noera.noera.model.ProductColor;

@Repository
public interface ColorRepository extends JpaRepository<ProductColor, Integer>{

    ProductColor findByProductSizeIdAndColorName(Integer id, String color);
    
}
