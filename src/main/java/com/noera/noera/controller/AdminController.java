package com.noera.noera.controller;

import com.noera.noera.dto.ProductColorDto;
import com.noera.noera.dto.ProductDto;
import com.noera.noera.dto.ProductSizeDto;
import com.noera.noera.model.Product;
import com.noera.noera.model.ProductColor;
import com.noera.noera.model.ProductSize;
import com.noera.noera.repository.ShopRepository;
import com.noera.noera.service.ColorService;
import com.noera.noera.service.ShopService;
import com.noera.noera.service.SizeService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";
    
    private final ShopService service;
    private final SizeService sizeService;
    private final ColorService colorService;
    private final ShopRepository repository;

    public AdminController(ShopService service, SizeService sizeService, ColorService colorService, ShopRepository repository) {
        this.service = service;
        this.sizeService = sizeService;
        this.colorService = colorService;
        this.repository = repository;
    }

    @GetMapping("/products")
    public String getProducts(Model model) {
        List<Product> products = service.findAll();
        model.addAttribute("products", products);
        model.addAttribute("product", new Product());
        return "ad";
    }

    @PostMapping("/products")
    public String addProduct(
        @ModelAttribute Product product,
        MultipartHttpServletRequest request, RedirectAttributes redirectAttributes) {
        
        request.getParameterMap().forEach((key, value) -> {
            System.out.println("Param: " + key + " = " + Arrays.toString(value));
        });
        
        // Debug logging to see all files
        request.getFileMap().forEach((key, value) -> {
            System.out.println("File: " + key + " = " + (value != null ? value.getOriginalFilename() : "null"));
        });
        
        try{
            if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название товара обязательно");
        }
        
        if (product.getPrice() == null) {
            throw new IllegalArgumentException("Цена обязательна");
        }
        
        List<ProductSize> sizes = new ArrayList<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        
        // Создаем директорию для загрузки, если её нет
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin/products?error=upload_dir";
        }

        // Обработка размеров и цветов
        Set<Integer> sizeIndices = new TreeSet<>();
        for (String paramName : parameterMap.keySet()) {
            if (paramName.matches("sizes\\[\\d+\\]\\.sizeName")) {
                int index = Integer.parseInt(paramName.replaceAll(".*\\[(\\d+)\\].*", "$1"));
                sizeIndices.add(index);
            }
        }

        for (int sizeIndex : sizeIndices) {
            ProductSize size = new ProductSize();
            
            String sizeName = getLastNonEmptyValue(request.getParameterValues("sizes[" + sizeIndex + "].sizeName"));
            size.setSizeName(sizeName);
            // if (sizeName == null){
            //     continue;
            // }
            // Обработка цветов
            Set<Integer> colorIndices = new TreeSet<>();
            for (String paramName : parameterMap.keySet()) {
                if (paramName.matches("sizes\\[" + sizeIndex + "\\]\\.colors\\[\\d+\\]\\.colorName")) {
                    int colorIndex = Integer.parseInt(paramName.replaceAll(".*\\[(\\d+)\\].*", "$1"));
                    colorIndices.add(colorIndex);
                }
            }

            List<ProductColor> colors = new ArrayList<>();
            for (int colorIndex : colorIndices) {
                ProductColor color = new ProductColor();
                color.setColorName(getLastNonEmptyValue(
                    request.getParameterValues("sizes[" + sizeIndex + "].colors[" + colorIndex + "].colorName")));

                String colorHex = getLastNonEmptyValue(
                    request.getParameterValues("sizes[" + sizeIndex + "].colors[" + colorIndex + "].colorHex"));
                color.setColorHex(colorHex);
                String quantityStr = getLastNonEmptyValue(
                    request.getParameterValues("sizes[" + sizeIndex + "].colors[" + colorIndex + "].quantity"));
                color.setQuantity(quantityStr != null ? Integer.parseInt(quantityStr) : 0);

                // Обработка изображений
                MultipartFile imageFile = request.getFile("sizeColorImages[" + sizeIndex + "]" + "[" + colorIndex + "]");
                if (imageFile != null && !imageFile.isEmpty()) {
                    System.out.println("Found image file: " + imageFile.getOriginalFilename() + ", size: " + imageFile.getSize());
                    String imageUrl = saveImage(imageFile);
                    color.setImageUrl(imageUrl);
                } else {
                    System.out.println("No image file found for size " + sizeIndex + " color " + colorIndex);
                }

                MultipartFile hoverImageFile = request.getFile("sizeColorHoverImages[" + sizeIndex + "]" + "[" + colorIndex + "]");
                if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
                    String hoverImageUrl = saveImage(hoverImageFile);
                    color.setHoverImageUrl(hoverImageUrl);
                }

                color.setProductSize(size);
                colors.add(color);
            }

            size.setColors(colors);
            size.setProduct(product);
            sizes.add(size);
        }

        product.setSizes(sizes);
        service.save(product);
        } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    private String getLastNonEmptyValue(String[] values) {
        if (values == null || values.length == 0) return null;
        for (int i = values.length - 1; i >= 0; i--) {
            if (values[i] != null && !values[i].trim().isEmpty()) {
                return values[i].trim();
            }
        }
        return null;
    }


    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/image")
    public String handleProductUpload(
        @RequestParam String name,
        @RequestParam String size,
        @RequestParam String color,
        @RequestParam("image") MultipartFile imageFile,
        @RequestParam(value = "hoverImage", required = false) MultipartFile hoverImageFile,
        RedirectAttributes redirectAttributes) {

    try {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название товара обязательно");
        }
        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException("Размер обязателен");
        }
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Цвет обязателен");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Основное изображение обязательно");
        }

        Product product = service.findOneByName(name);
        if (product == null) {
            throw new IllegalArgumentException("Товар не найден");
        }

        ProductSize productSize = sizeService.findByProductIdAndSizeName(product.getId(), size);
        if (productSize == null) {
            throw new IllegalArgumentException("Размер не найден");
        }

        ProductColor productColor = colorService.findBySizeIdAndColorName(productSize.getId(), color);
        if (productColor == null) {
            throw new IllegalArgumentException("Цвет не найден");
        }

        String imageUrl = saveImage(imageFile);
        if (imageUrl == null) {
            throw new RuntimeException("Не удалось сохранить изображение");
        }
        productColor.setImageUrl(imageUrl);

        if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
            String hoverImageUrl = saveImage(hoverImageFile);
            productColor.setHoverImageUrl(hoverImageUrl);
        }

        service.save(product);
        redirectAttributes.addFlashAttribute("success", "Изображения успешно обновлены");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
    }

    return "redirect:/admin/products";
}
    
   @PostMapping("/products/{productId}/sizes/{sizeId}/delete")
    public String deleteSize(@PathVariable Integer productId, @PathVariable Integer sizeId) {
        sizeService.deleteSize(sizeId);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{productId}/colors/{colorId}/delete")
    public String deleteColor(@PathVariable Integer productId, @PathVariable Integer colorId) {
        colorService.deleteColor(colorId);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}")
    public String getProductDescription(@PathVariable Integer id, 
                                      @RequestParam(required = false) String sizeName,
                                      @RequestParam(required = false) Integer colorId,
                                      Model model) {
        System.out.println(sizeName);
        Product product = service.findById(id);
        if (product == null) {
            return "redirect:/";
        }

        // Получаем все размеры товара
        List<ProductSize> sizes = product.getSizes();
        
        // Находим выбранный размер (или первый доступный)
        ProductSize selectedSize = sizes.stream()
                .filter(s -> sizeName == null || sizeName.equalsIgnoreCase(s.getSizeName()))
                .findFirst()
                .orElse(sizes.isEmpty() ? null : sizes.get(0));
        
        // Получаем все цвета для выбранного размера
        List<ProductColor> colors = selectedSize != null ? selectedSize.getColors() : Collections.emptyList();
        
        // Находим выбранный цвет (или первый доступный)
        ProductColor selectedColor = colors.stream()
                .filter(c -> colorId == null || colorId.equals(c.getId()))
                .findFirst()
                .orElse(colors.isEmpty() ? null : colors.get(0));

        // Получаем все уникальные цвета для всех размеров (для галереи)
        List<String> allProductImages = product.getSizes().stream()
                .flatMap(size -> size.getColors().stream())
                .map(ProductColor::getImageUrl)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Добавляем hover-изображения если они есть
        product.getSizes().stream()
                .flatMap(size -> size.getColors().stream())
                .map(ProductColor::getHoverImageUrl)
                .filter(Objects::nonNull)
                .forEach(allProductImages::add);
        System.out.println(selectedSize.getSizeName());
        System.out.println(selectedColor.getColorName());
        // System.out.println(selectedColor.getColorName());
        model.addAttribute("product", product);
        model.addAttribute("selectedSize", selectedSize);
        model.addAttribute("selectedColor", selectedColor);
        model.addAttribute("availableColors", colors);
        model.addAttribute("allProductImages", allProductImages);

        return "ad_edit";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product) {

    for (ProductSize size : product.getSizes()) {
        for (ProductColor color : size.getColors()) {
            // сохраняем обычное фото
            if (color.getImageFile() != null && !color.getImageFile().isEmpty()) {
                String imageUrl = saveImage(color.getImageFile());
                color.setImageUrl(imageUrl);
            }

            // сохраняем hover-фото (если оно у тебя есть)
            if (color.getHoverImageFile() != null && !color.getHoverImageFile().isEmpty()) {
                String hoverImageUrl = saveImage(color.getHoverImageFile());
                color.setHoverImageUrl(hoverImageUrl);
            }

            // привязываем цвет обратно к размеру (если нужно, чтобы JPA сохранил)
            color.setProductSize(size);
        }
    }

    // Привязываем размеры обратно к продукту (чтобы каскадное сохранение сработало)
    for (ProductSize size : product.getSizes()) {
        size.setProduct(product);
    }

    // теперь сохраняем сам продукт (JPA сохранит всё каскадом)
    service.save(product);

    return "redirect:/admin/products/" + product.getId();
}


    @GetMapping("/shop")
    public String getShop(Model model) {
        List<Product> products = service.findAll();
        model.addAttribute("products", products);
        return "ad_shop";
    }

    @GetMapping("/shop/products/{id}")
    public String getShopProductDescription(@PathVariable Integer id, 
                                      @RequestParam(required = false) String sizeName,
                                      @RequestParam(required = false) Integer colorId,
                                      Model model) {
        System.out.println(sizeName);
        Product product = service.findById(id);
        if (product == null) {
            return "redirect:/";
        }

        // Получаем все размеры товара
        List<ProductSize> sizes = product.getSizes();
        
        // Находим выбранный размер (или первый доступный)
        ProductSize selectedSize = sizes.stream()
                .filter(s -> sizeName == null || sizeName.equalsIgnoreCase(s.getSizeName()))
                .findFirst()
                .orElse(sizes.isEmpty() ? null : sizes.get(0));
        
        // Получаем все цвета для выбранного размера
        List<ProductColor> colors = selectedSize != null ? selectedSize.getColors() : Collections.emptyList();
        
        // Находим выбранный цвет (или первый доступный)
        ProductColor selectedColor = colors.stream()
                .filter(c -> colorId == null || colorId.equals(c.getId()))
                .findFirst()
                .orElse(colors.isEmpty() ? null : colors.get(0));

        // Получаем все уникальные цвета для всех размеров (для галереи)
        List<String> allProductImages = product.getSizes().stream()
                .flatMap(size -> size.getColors().stream())
                .map(ProductColor::getImageUrl)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Добавляем hover-изображения если они есть
        product.getSizes().stream()
                .flatMap(size -> size.getColors().stream())
                .map(ProductColor::getHoverImageUrl)
                .filter(Objects::nonNull)
                .forEach(allProductImages::add);
        System.out.println(selectedSize.getSizeName());
        System.out.println(selectedColor.getColorName());
        // System.out.println(selectedColor.getColorName());
        model.addAttribute("product", product);
        model.addAttribute("selectedSize", selectedSize);
        model.addAttribute("selectedColor", selectedColor);
        model.addAttribute("availableColors", colors);
        model.addAttribute("allProductImages", allProductImages);

        return "ad_description";
    }

    @GetMapping("/shop/products/{id}/variant")
@ResponseBody
public Map<String, Object> getProductVariant(
        @PathVariable Integer id,
        @RequestParam(required = false) String sizeName,
        @RequestParam(required = false) Integer colorId) {

    Product product = service.findById(id);
    if (product == null) {
        return Map.of("error", "Product not found");
    }

    // Логика та же как в getProductDescription
    List<ProductSize> sizes = product.getSizes();

    ProductSize selectedSize = sizes.stream()
            .filter(s -> sizeName == null || sizeName.equalsIgnoreCase(s.getSizeName()))
            .findFirst()
            .orElse(sizes.isEmpty() ? null : sizes.get(0));

    List<ProductColor> colors = selectedSize != null ? selectedSize.getColors() : Collections.emptyList();

    // Если цвет не выбран, но есть доступные цвета, выбираем первый
    ProductColor selectedColor = null;
    if (colorId != null) {
        selectedColor = colors.stream()
                .filter(c -> colorId.equals(c.getId()))
                .findFirst()
                .orElse(null);
    }
    
    // Если цвет не выбран явно, но есть доступные цвета, берем первый
    if (selectedColor == null && !colors.isEmpty()) {
        selectedColor = colors.get(0);
    }

    return Map.of(
            "price", product.getPrice(),
            "sizeName", selectedSize != null ? selectedSize.getSizeName() : null,
            "colorId", selectedColor != null ? selectedColor.getId() : null,
            "colorName", selectedColor != null ? selectedColor.getColorName() : null,
            "quantity", selectedColor != null ? selectedColor.getQuantity() : 0,
            "imageUrl", selectedColor != null ? selectedColor.getImageUrl() : null,
            "hoverImageUrl", selectedColor != null ? selectedColor.getHoverImageUrl() : null
    );
}

    // Вспомогательный метод для проверки доступности размера
    private boolean isSizeAvailable(ProductSize size) {
        return size != null && size.getColors().stream()
                .anyMatch(color -> color.getQuantity() > 0);
    }

    @PostMapping("/shop/products/{id}/description")
    @ResponseBody
    public Map<String, Object> updateDescription(@PathVariable Integer id,
                                                @RequestBody Map<String, String> payload) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Product product = service.findById(id);
        if (product == null) return Map.of("success", false);

        String newDescription = payload.get("description");
        System.out.println(newDescription);
        product.setDescription(newDescription);
        service.save(product);

        return Map.of("success", true);
    }

    private String saveImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                System.out.println("File is null or empty");
                return null;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                System.out.println("Original filename is empty");
                return null;
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path path = Paths.get(UPLOAD_DIR + uniqueFileName);

            System.out.println("Saving file to: " + path.toAbsolutePath());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return "/images/" + uniqueFileName;
        } catch (IOException e) {
            System.err.println("Failed to save image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/shop/products/{productId}/colors/{colorId}/images")
    @ResponseBody
    public ResponseEntity<String> handleProductUpload(
        @PathVariable Integer productId,
        @PathVariable Integer colorId,

        @RequestParam(value = "mainImage", required = false) MultipartFile imageFile,
        @RequestParam(value = "hoverImage", required = false) MultipartFile hoverImageFile, RedirectAttributes redirectAttributes) {
            System.out.println("ФОТООООООООООООООООООООООО");
    try {

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Основное изображение обязательно");
        }

        Product product = service.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Товар не найден");
        }


        ProductColor productColor = colorService.findById(colorId);
        if (productColor == null) {
            throw new IllegalArgumentException("Цвет не найден");
        }

        String imageUrl = saveImage(imageFile);
        if (imageUrl == null) {
            throw new RuntimeException("Не удалось сохранить изображение");
        }
        productColor.setImageUrl(imageUrl);

        if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
            String hoverImageUrl = saveImage(hoverImageFile);
            productColor.setHoverImageUrl(hoverImageUrl);
        }

        service.save(product);
        redirectAttributes.addFlashAttribute("success", "Изображения успешно обновлены");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
    }

    return ResponseEntity.ok("GOOD");
}



@GetMapping("/shop/products/{id}/sizes/{sizeName}")
    public ResponseEntity<?> getBySize(@PathVariable Integer id, @PathVariable String sizeName) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Находим размер
        var size = product.getSizes().stream()
                .filter(s -> s.getSizeName().equalsIgnoreCase(sizeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Size not found"));

        // Формируем ответ
        Map<String, Object> response = new HashMap<>();
        response.put("price", product.getPrice());
        response.put("quantity", size.getColors().stream().mapToInt(ProductColor::getQuantity).sum());

        List<Map<String, Object>> colors = size.getColors().stream()
        .map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("colorHex", c.getColorHex());
            map.put("colorName", c.getColorName());
            return map;
        })
        .toList();

        response.put("colors", colors);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/shop/products/{id}/colors/{colorId}")
    public ResponseEntity<?> getByColor(@PathVariable Integer id, @PathVariable Integer colorId) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Находим цвет
        var color = product.getSizes().stream()
                .flatMap(s -> s.getColors().stream())
                .filter(c -> c.getId().equals(colorId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Color not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("price", product.getPrice());
        response.put("quantity", color.getQuantity());
        response.put("imageUrl", color.getImageUrl());
        response.put("hoverImageUrl", color.getHoverImageUrl());

        return ResponseEntity.ok(response);
    }
}