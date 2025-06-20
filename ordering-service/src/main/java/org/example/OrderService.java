package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    public Order createOrder(Order order) {
        if (order.getRegion() == null || order.getRegion().trim().isEmpty()) {
            throw new IllegalArgumentException("Region cannot be empty");
        }
        if (order.getVaccineQuantity() <= 0) {
            throw new IllegalArgumentException("Vaccine Quantity must be greater than 0");
        }
        if (order.getExpectedDeliveryTime() == null) {
            throw new IllegalArgumentException("Expected Delivery Time cannot be null");
        }

        order.setStatus("PENDING");
        order.setFulfilledQuantity(0);

        Order savedOrder = orderRepository.save(order);

        return savedOrder;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order with ID " + id + " not found"));
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByRegion(String region) {
        return orderRepository.findByRegionIgnoreCase(region);
    }

    public int fulfillOrders(int vaccineCount) {
        int remainingVaccines = vaccineCount;

        List<Order> priorityOrders = orderRepository.findByStatusInOrderByExpectedDeliveryTimeAsc(
            List.of("PRIORITY")
        );

        remainingVaccines = fulfillOrderList(priorityOrders, remainingVaccines);

        if (remainingVaccines > 0) {
            List<Order> pendingOrders = orderRepository.findByStatusInOrderByExpectedDeliveryTimeAsc(
                List.of("PENDING")
            );
            remainingVaccines = fulfillOrderList(pendingOrders, remainingVaccines);
        }

        return remainingVaccines;
    }

    private int fulfillOrderList(List<Order> orders, int availableVaccines) {
        int remaining = availableVaccines;

        for (Order order : orders) {
            int needed = order.getVaccineQuantity() - order.getFulfilledQuantity();

            if (needed <= 0) continue;

            if (remaining >= needed) {
                order.setFulfilledQuantity(order.getFulfilledQuantity() + needed);
                order.setStatus("FULFILLED");
                orderRepository.save(order);
                remaining -= needed;
            } else {
                break;
            }
        }

        return remaining;
    }

    public Order updateOrderStatus(Long id, String newStatus) {
        Order order = getOrder(id);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatusIn(List.of("PENDING"));
    }
}