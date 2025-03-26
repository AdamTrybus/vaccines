package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class); // Use SLF4J for logging

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Order createOrder(Order order) {
        // Validate order fields
        if (order.getRegion() == null || order.getRegion().trim().isEmpty()) {
            throw new IllegalArgumentException("Region cannot be empty");
        }
        if (order.getCases() <= 0 || order.getVaccineQuantity() <= 0) {
            throw new IllegalArgumentException("Cases and Vaccine Quantity must be greater than 0");
        }
        if (order.getExpectedDeliveryTime() == null) {
            throw new IllegalArgumentException("Expected Delivery Time cannot be null");
        }

        // Set initial status
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // Notify producer-service
        try {
            restTemplate.postForObject("http://localhost:8080/api/producers/request", savedOrder, Void.class);
            logger.info("Successfully notified producer-service for order ID: {}", savedOrder.getId());
        } catch (ResourceAccessException ex) {
            logger.error("Failed to notify producer-service for order ID: {}. Error: {}", savedOrder.getId(), ex.getMessage(), ex);
            savedOrder.setStatus("PENDING_NOTIFICATION_FAILED");
            orderRepository.save(savedOrder);
        }

        return savedOrder;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order with ID " + id + " not found"));
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }
}