package com.noera.noera.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.noera.noera.model.Product;
import com.noera.noera.repository.ShopRepository;

@Service
public class ShopService {
    private ShopRepository repository;

    public ShopService(ShopRepository repository){
        this.repository = repository;
    }

    public List<Product> findAll(){
        return repository.findAll();
    }

    public Product save(Product product){
        return repository.save(product);
    }

    public void delete(Integer id){
        repository.deleteById(id);
    }
}
