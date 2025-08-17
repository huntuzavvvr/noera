package com.noera.noera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noera.noera.model.ProductSize;

@Repository
public interface SizeRepository extends JpaRepository<ProductSize, Integer>{
    ProductSize findByProductIdAndSizeName(Integer id, String sizeName);
}
