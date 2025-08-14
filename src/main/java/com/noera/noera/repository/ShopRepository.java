package com.noera.noera.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.noera.noera.model.Product;

@Repository
public interface ShopRepository extends JpaRepository<Product, Integer>{
    Optional<Product> findByNameAndColor(String name, String color);

}
