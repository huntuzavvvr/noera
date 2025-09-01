package com.noera.noera;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductSize;
import com.noera.noera.model.ProductColor;
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
            // Создаём основной товар - худи
            Product hoodie = createHoodie();
            productRepository.save(hoodie);

            // Создаём второй товар - футболку
            Product tshirt = createTshirt();
            productRepository.save(tshirt);
        }
    }

    private Product createHoodie() {
        Product hoodie = new Product();
        hoodie.setName("Noera Base Hoodie");
        hoodie.setPrice(BigDecimal.valueOf(299000)); // Цена в UZS
        hoodie.setDescription("Cool hoodie");

        // Размер S
        ProductSize sizeS = new ProductSize();
        sizeS.setSizeName("S");
        
        // Цвета для размера S
        ProductColor sWhite = createColor("Белый", "#FFFFFF", 5, 
                "/images/white_1.png", "/images/white_2.png");
        
        ProductColor sBlack = createColor("Чёрный", "#000000", 3, 
                "/images/black_1.png", "/images/black_2.png");
        
        sizeS.setColors(List.of(sWhite, sBlack));
        sWhite.setProductSize(sizeS);
        sBlack.setProductSize(sizeS);
        
        // Размер M
        ProductSize sizeM = new ProductSize();
        sizeM.setSizeName("M");
        
        // Цвета для размера M
        ProductColor mWhite = createColor("Белый", "#FFFFFF", 7, 
                "/images/white_1.png", "/images/white_2.png");
        
        ProductColor mBlack = createColor("Чёрный", "#000000", 4, 
                "/images/black_1.png", "/images/black_2.png");
        
        sizeM.setColors(List.of(mWhite, mBlack));
        mWhite.setProductSize(sizeM);
        mBlack.setProductSize(sizeM);
        
        // Связываем товар с размерами
        hoodie.setSizes(List.of(sizeS, sizeM));
        sizeS.setProduct(hoodie);
        sizeM.setProduct(hoodie);
        
        return hoodie;
    }

    private Product createTshirt() {
        Product tshirt = new Product();
        tshirt.setName("Noera Basic T-Shirt");
        tshirt.setPrice(BigDecimal.valueOf(199000)); // Цена в UZS
        tshirt.setDescription("Cool tshirt");

        ProductSize tshirtSize = new ProductSize();
        tshirtSize.setSizeName("Universal");
        
        // Цвета для футболки
        ProductColor tshirtRed = createColor("Красный", "#FF0000", 10, 
                "/images/white_1.png", null);
        
        ProductColor tshirtBlue = createColor("Синий", "#0000FF", 8, 
                "/images/black_1.png", "/images/black_2.png");
        
        tshirtSize.setColors(List.of(tshirtRed, tshirtBlue));
        tshirtRed.setProductSize(tshirtSize);
        tshirtBlue.setProductSize(tshirtSize);
        
        tshirt.setSizes(List.of(tshirtSize));
        tshirtSize.setProduct(tshirt);
        
        return tshirt;
    }

    private ProductColor createColor(String name, String hex, int quantity, 
                                  String imageUrl, String hoverImageUrl) {
        ProductColor color = new ProductColor();
        color.setColorName(name);
        color.setColorHex(hex); // Устанавливаем HEX-код цвета
        color.setQuantity(quantity);
        color.setImageUrl(imageUrl);
        color.setHoverImageUrl(hoverImageUrl);
        return color;
    }
}