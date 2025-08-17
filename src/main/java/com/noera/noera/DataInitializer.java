package com.noera.noera;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductSize;
import com.noera.noera.model.ProductColor;
// import com.noera.noera.repository.ProductRepository;
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
            Product hoodie = new Product();
            hoodie.setName("Noera Base Hoodie");
            hoodie.setPrice(BigDecimal.valueOf(999)); // Фиксированная цена
            
            // Размер S
            ProductSize sizeS = new ProductSize();
            sizeS.setSizeName("S");
            
            // Цвета для размера S
            ProductColor sWhite = new ProductColor();
            sWhite.setColorName("white");
            sWhite.setQuantity(5);
            sWhite.setImageUrl("/images/white_1.png");
            sWhite.setHoverImageUrl("/images/white_2.png");
            
            ProductColor sBlack = new ProductColor();
            sBlack.setColorName("black");
            sBlack.setQuantity(3);
            sBlack.setImageUrl("/images/black_1.png");
            sBlack.setHoverImageUrl("/images/black_2.png");
            
            sizeS.setColors(List.of(sWhite, sBlack));
            sWhite.setProductSize(sizeS);
            sBlack.setProductSize(sizeS);
            
            // Размер M
            ProductSize sizeM = new ProductSize();
            sizeM.setSizeName("M");
            
            // Цвета для размера M
            ProductColor mWhite = new ProductColor();
            mWhite.setColorName("white");
            mWhite.setQuantity(7);
            mWhite.setImageUrl("/images/white_1.png");
            mWhite.setHoverImageUrl("/images/white_2.png");
            
            ProductColor mBlack = new ProductColor();
            mBlack.setColorName("black");
            mBlack.setQuantity(4);
            mBlack.setImageUrl("/images/black_1.png");
            mBlack.setHoverImageUrl("/images/black_2.png");
            
            sizeM.setColors(List.of(mWhite, mBlack));
            mWhite.setProductSize(sizeM);
            mBlack.setProductSize(sizeM);
            
            // Связываем товар с размерами
            hoodie.setSizes(List.of(sizeS, sizeM));
            sizeS.setProduct(hoodie);
            sizeM.setProduct(hoodie);
            
            // Сохраняем товар (размеры и цвета сохранятся каскадно)
            productRepository.save(hoodie);

            // Второй товар для примера
            Product tshirt = new Product();
            tshirt.setName("Noera Basic T-Shirt");
            tshirt.setPrice(BigDecimal.valueOf(599));
            
            ProductSize tshirtSize = new ProductSize();
            tshirtSize.setSizeName("Universal");
            
            ProductColor tshirtRed = new ProductColor();
            tshirtRed.setColorName("red");
            tshirtRed.setQuantity(10);
            tshirtRed.setImageUrl("/images/white_1.png");
            
            tshirtSize.setColors(List.of(tshirtRed));
            tshirtRed.setProductSize(tshirtSize);
            
            tshirt.setSizes(List.of(tshirtSize));
            tshirtSize.setProduct(tshirt);
            
            productRepository.save(tshirt);
        }
    }
}