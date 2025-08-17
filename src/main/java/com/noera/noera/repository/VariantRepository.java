package com.noera.noera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noera.noera.model.ProductVariant;

@Repository
public interface VariantRepository extends JpaRepository<ProductVariant, Integer> {
    void deleteByProductId(Integer productId);
}
