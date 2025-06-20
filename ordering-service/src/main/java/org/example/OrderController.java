package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.findAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<Order> prioritizeOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        if (!"EXPIRED".equals(order.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        Order updatedOrder = orderService.updateOrderStatus(id, "PRIORITY");
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order updatedOrder = orderService.updateOrderStatus(id, "CANCELLED");
        return ResponseEntity.ok(updatedOrder);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam String newStatus) {
        Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<Order>> getOrdersByRegion(@PathVariable String region) {
        List<Order> orders = orderService.getOrdersByRegion(region);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public List<Order> getPendingOrders() {
        return orderService.getPendingOrders();
    }

    @PostMapping("/fulfill")
    public ResponseEntity<Integer> fulfillOrders(@RequestParam int availableVaccines) {
        int leftover = orderService.fulfillOrders(availableVaccines);
        return ResponseEntity.ok(leftover);
    }

}