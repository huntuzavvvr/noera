package com.noera.noera;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.noera.noera.model.Product;
import com.noera.noera.repository.ShopRepository;

@Component
public class DataInitializer implements CommandLineRunner{

    private ShopRepository repository;

    public DataInitializer(ShopRepository repository){
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        if (repository.count() == 0){
            Product product1 = new Product();
            product1.setName("noera_base");
            product1.setPrice(999);
            product1.setImageUrl("/images/1_1.png");
            product1.setHoverImageUrl("/images/1_2.png");
            Product product2 = new Product();
            product2.setName("noera_base");
            product2.setPrice(1199);
            product2.setImageUrl("/images/2_1.png");
            product2.setHoverImageUrl("/images/2_2.png");
            repository.save(product1);
            repository.save(product2);
        }
    }
    
}
