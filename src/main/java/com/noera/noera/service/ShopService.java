package com.noera.noera.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.noera.noera.model.Product;
import com.noera.noera.repository.ShopRepository;
import com.noera.noera.repository.VariantRepository;

@Service
public class ShopService {
    private ShopRepository repository;
    private VariantRepository variantRepository;

    public ShopService(ShopRepository repository, VariantRepository variantRepository){
        this.repository = repository;
        this.variantRepository = variantRepository;
    }

    public List<Product> findAll(){
        for (var elem : repository.findAll()){
            System.out.println(elem.getName());
        }
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
//     public Product findByNameAndColor(String name, String color) {
//     return repository.findByNameAndColor(name, color)
//             .orElseThrow(() -> new RuntimeException("Товар не найден"));
// }
    public List<Product> findByName(String name) {
        return repository.findAllByName(name);
    }

    public void deleteVariant(Integer productId, Integer variantId) {
        System.out.println("SErvice");
        System.out.println(productId + " " +  variantId);
        variantRepository.deleteById(variantId);
    }

    public Product findOneByName(String name) {
        return repository.findByName(name);
    }
}
