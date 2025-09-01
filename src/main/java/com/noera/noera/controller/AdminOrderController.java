package com.noera.noera.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.noera.noera.model.Order;
import com.noera.noera.model.OrderItem;
import com.noera.noera.repository.OrderRepository;


@Controller
@RequestMapping()
public class AdminOrderController {

    private OrderRepository orderRepository;

    public AdminOrderController(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }

    @GetMapping("/admin/orders")
public String showOrders(@RequestParam(required = false) String status, Model model) {
    List<Order> orders;
    if (status != null && !status.isEmpty()) {
        orders = orderRepository.findByStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
    } else {
        orders = orderRepository.findAllByOrderByOrderDateDesc();
    }
    
    model.addAttribute("orders", orders);
    model.addAttribute("order", new Order()); // Для формы добавления
    model.addAttribute("orderItem", new OrderItem()); // Для формы добавления товара
    model.addAttribute("pendingCount", orderRepository.countByStatus(Order.OrderStatus.PENDING));
    model.addAttribute("confirmedCount", orderRepository.countByStatus(Order.OrderStatus.CONFIRMED));
    model.addAttribute("completedCount", orderRepository.countByStatus(Order.OrderStatus.COMPLETED));
    model.addAttribute("totalRevenue", orderRepository.findTotalRevenue());
    
    return "ad_orders";
}

@PostMapping("/admin/orders/create")
public String createOrder(@Validated @ModelAttribute Order order, 
                         BindingResult result,
                         @RequestParam("productNames") List<String> productNames,
                         @RequestParam("sizes") List<String> sizes,
                         @RequestParam("colors") List<String> colors,
                         @RequestParam("quantities") List<Integer> quantities,
                         @RequestParam("prices") List<Double> prices,
                         RedirectAttributes redirectAttributes) {
    
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "Пожалуйста, заполните все обязательные поля");
        return "redirect:/admin/orders";
    }
    
    try {
        order.setOrderDate(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        
        // Создаем items из переданных параметров
        for (int i = 0; i < productNames.size(); i++) {
            if (productNames.get(i) != null && !productNames.get(i).isEmpty()) {
                OrderItem item = new OrderItem();
                item.setProductName(productNames.get(i));
                item.setSize(sizes.get(i));
                item.setColor(colors.get(i));
                item.setQuantity(quantities.get(i));
                item.setPrice(prices.get(i));
                order.getItems().add(item);
            }
        }
        
        if (order.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Добавьте хотя бы один товар в заказ");
            return "redirect:/admin/orders";
        }
        
        order.calculateTotal();
        orderRepository.save(order);
        
        redirectAttributes.addFlashAttribute("success", "Заказ успешно создан");
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Ошибка при создании заказа: " + e.getMessage());
    }
    
    return "redirect:/admin/orders";
}

@PostMapping("/admin/orders/{id}/update-status")
public String updateOrderStatus(@PathVariable Long id, 
                               @RequestParam Order.OrderStatus status,
                               @RequestParam(required = false) String managerNotes) {
    
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Order not found"));
    
    order.setStatus(status);
    if (managerNotes != null) {
        order.setManagerNotes(managerNotes);
    }
    
    orderRepository.save(order);
    return "redirect:/admin/orders?success=Status+updated";
}

@PostMapping("/admin/orders/{id}/delete")
public String deleteOrder(@PathVariable Long id) {
    orderRepository.deleteById(id);
    return "redirect:/admin/orders?success=Order+deleted";
}
}
