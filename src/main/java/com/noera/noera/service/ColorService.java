package com.noera.noera.service;

import org.springframework.stereotype.Service;

import com.noera.noera.model.ProductColor;
import com.noera.noera.repository.ColorRepository;

@Service
public class ColorService {

    private ColorRepository repository;

    public ColorService(ColorRepository repository){
        this.repository = repository;
    }

    public ProductColor findBySizeIdAndColorName(Integer id, String color) {
        return repository.findByProductSizeIdAndColorName(id, color);
    }
    public void deleteColor(Integer colorId) {
        repository.deleteById(colorId);
    }

    public ProductColor findById(Integer colorId) {
        return repository.findById(colorId).orElse(null);
    }
}
