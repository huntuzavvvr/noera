package com.noera.noera.service;

import org.springframework.stereotype.Service;

import com.noera.noera.model.ProductSize;
import com.noera.noera.repository.SizeRepository;

@Service
public class SizeService {
    private SizeRepository repository;

    public SizeService(SizeRepository repository){
        this.repository = repository;
    }

    public ProductSize findByProductIdAndSizeName(Integer id, String sizeName) {
        return repository.findByProductIdAndSizeName(id, sizeName);
    }

    public void deleteSize(Integer sizeId) {
        repository.deleteById(sizeId);
    }
}
