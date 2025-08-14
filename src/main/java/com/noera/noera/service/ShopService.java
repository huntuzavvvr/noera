package com.noera.noera.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public Product findById(Integer id){
        return repository.findById(id).orElse(null);
    }
    public Product findByNameAndColor(String name, String color) {
    return repository.findByNameAndColor(name, color)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
}

}
