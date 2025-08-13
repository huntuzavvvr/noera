package com.noera.noera.controller;

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

    @PostMapping("/products")
    public String addProduct(@ModelAttribute Product product) {
        //TODO: process POST request
        service.save(product);
        return "redirect:/admin/products";
    }
    
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id){
        service.delete(id);
        return "redirect:/admin/products";
    }
}
