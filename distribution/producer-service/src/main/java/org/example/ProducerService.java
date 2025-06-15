package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.example.model.Order;
import org.example.repository.OrderRepository;
import org.example.service.OrderService;
import java.time.LocalDate;
import java.util.List;

@EnableScheduling
@Service
public class ProducerService {
    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    @Autowired
    private ProducerResponseRepository responseRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    public ProducerCapacity registerProducerCapacity(ProducerCapacityRequest request) {

        if (request.getProducerName() == null || request.getProducerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Producer name cannot be null or empty.");
        }
        if (request.getVaccinesQuantity() <= 0) {
            throw new IllegalArgumentException("Vaccine quantity must be positive.");
        }
        if (request.getProductionDeadline() == null || request.getProductionDeadline().trim().isEmpty()) {
            throw new IllegalArgumentException("Production deadline cannot be null or empty.");
        }

        // Save the new capacity first
        ProducerCapacity producerCapacity = new ProducerCapacity();
        producerCapacity.setProducerName(request.getProducerName());
        producerCapacity.setVaccinesQuantity(request.getVaccinesQuantity());
        producerCapacity.setProductionDeadline(request.getProductionDeadline());
        producerCapacity.setExcessVaccines(0); // Initially no excess
        responseRepository.save(producerCapacity);

        // Calculate total available vaccines = all excess + new capacity's vaccinesQuantity
        int totalExcessVaccines = responseRepository.findAll().stream()
            .mapToInt(ProducerCapacity::getExcessVaccines)
            .sum();

        int totalAvailableVaccines = totalExcessVaccines + request.getVaccinesQuantity();

        // Fulfill orders with totalAvailableVaccines
        int leftoverVaccines = orderService.fulfillOrders(totalAvailableVaccines);

        // Reset excessVaccines for all existing capacities to 0
        responseRepository.findAll().forEach(pc -> {
            pc.setExcessVaccines(0);
            responseRepository.save(pc);
        });

        // Assign leftover vaccines only to the newly created capacity
        producerCapacity.setExcessVaccines(leftoverVaccines);
        responseRepository.save(producerCapacity);

        return producerCapacity;
    }

    public List<ProducerCapacity> getAllProducerCapacities() {
        return responseRepository.findAll();
    }

    @Scheduled(fixedRate = 600000)
    public void expireOldOrders() {
        List<Order> pendingOrders = orderRepository.findByStatusIn(List.of("PENDING", "PARTIALLY_FULFILLED"));
        LocalDate today = LocalDate.now();

        for (Order order : pendingOrders) {
            if (order.getExpectedDeliveryTime().isBefore(today)) {
                order.setStatus("EXPIRED");
                orderRepository.save(order);
            }
        }
    }
}