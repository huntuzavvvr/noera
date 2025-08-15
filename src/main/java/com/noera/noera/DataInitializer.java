package com.noera.noera;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductVariant;
import com.noera.noera.repository.ShopRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ShopRepository productRepository;

    public DataInitializer(ShopRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {

            // Создаём основной товар
            Product product = new Product();
            product.setName("Noera Base Hoodie");

            // Создаём варианты
            ProductVariant whiteVariant = new ProductVariant();
            whiteVariant.setColor("white");
            whiteVariant.setPrice(BigDecimal.valueOf(999));
            whiteVariant.setQuantity(10);
            whiteVariant.setImageUrl("/images/white_1.png");
            whiteVariant.setHoverImageUrl("/images/white_2.png");
            whiteVariant.setProduct(product); // связь

            ProductVariant blackVariant = new ProductVariant();
            blackVariant.setColor("black");
            blackVariant.setPrice(BigDecimal.valueOf(1199));
            blackVariant.setQuantity(5);
            blackVariant.setImageUrl("/images/black_1.png");
            blackVariant.setHoverImageUrl("/images/black_2.png");
            blackVariant.setProduct(product);

            // ProductVariant redVariant = new ProductVariant();
            // redVariant.setColor("red");
            // redVariant.setPrice(BigDecimal.valueOf(1099));
            // redVariant.setQuantity(3);
            // redVariant.setImageUrl("/images/red_1.png");
            // redVariant.setHoverImageUrl("/images/red_2.png");
            // redVariant.setProduct(product);

            // Добавляем варианты в товар
            product.setVariants(List.of(whiteVariant, blackVariant));

            // Сохраняем товар (варианты сохранятся каскадно)
            productRepository.save(product);
        }
    }
}
