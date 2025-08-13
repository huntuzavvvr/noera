package com.noera.noera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noera.noera.model.Product;

@Repository
public interface AdminRepository extends JpaRepository<Product, Integer>{
    
}
