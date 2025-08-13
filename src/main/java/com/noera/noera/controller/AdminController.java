package com.noera.noera.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.DeleteExchange;

import com.noera.noera.model.Product;
import com.noera.noera.service.ShopService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequestMapping("/admin")
// @RequiredArgsConstructor
public class AdminController {
    
    @Autowired
    private ShopService service;

    @GetMapping("/products")
    public String getMethodName(Model model) {
        List<Product> products = service.findAll();
        model.addAttribute("products", products);
        model.addAttribute("product", new Product());
        return "ad";
    }

    // @PostMapping("/products")
    // public String addProduct(@ModelAttribute Product product) {

    //     service.save(product);
    //     return "redirect:/admin/products";
    // }
    
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id){
        service.delete(id);
        return "redirect:/admin/products";
    }

    @PostMapping("/products")
    public String postMethodName(@ModelAttribute Product product, @RequestParam("image") MultipartFile file) {
        System.out.println("FADLF");
        if (!file.isEmpty()){
            try{
                System.out.println("FADLF");
                Path path = Paths.get("src/main/resources/static/images/" + file.getOriginalFilename());
                // Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        product.setImageUrl("/images/" + file.getOriginalFilename());
        service.save(product);
        return "redirect:/admin/products";
    }
    
}
