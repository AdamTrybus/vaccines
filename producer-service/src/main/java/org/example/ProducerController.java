package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.client.RestTemplate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/producers")
public class ProducerController {
    @Autowired
    private ProducerService producerService;

    @Autowired
    private ProducerResponseRepository responseRepository;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private static final String ORDERING_BASE_URL = "http://ordering:8081/api/orders";

    @PostMapping("/fulfillment")
    public void fulfillOrders() {
        // Fetch priority orders and sort them
        ResponseEntity<Order[]> priorityResponse = restTemplate.getForEntity(
            ORDERING_BASE_URL + "/priority", Order[].class);
        List<Order> priorityOrders = Arrays.stream(Objects.requireNonNull(priorityResponse.getBody()))
            .sorted(Comparator.comparing(Order::getExpectedDeliveryTime))
            .toList();

        // Fetch regular pending orders and sort them
        ResponseEntity<Order[]> pendingResponse = restTemplate.getForEntity(
            ORDERING_BASE_URL + "/pending", Order[].class);
        List<Order> pendingOrders = Arrays.stream(Objects.requireNonNull(pendingResponse.getBody()))
            .sorted(Comparator.comparing(Order::getExpectedDeliveryTime))
            .toList();

        // Combine: priority orders first
        List<Order> allOrders = Stream.concat(priorityOrders.stream(), pendingOrders.stream())
            .toList();

        // 2. Get all capacities sorted by deadline
        List<ProducerCapacity> allCapacities = responseRepository.findAll().stream()
            .sorted(Comparator.comparing((ProducerCapacity pc) -> LocalDate.parse(pc.getProductionDeadline())).reversed())
            .collect(Collectors.toList());

        for (Order order : allOrders) {
            LocalDate orderDate = LocalDate.parse(order.getExpectedDeliveryTime());

            // 3. Filter capacities that can fulfill this order (deadline >= orderDate)
            List<ProducerCapacity> validCapacities = allCapacities.stream()
                .filter(pc -> {
                    try {
                        LocalDate capacityDeadline = LocalDate.parse(pc.getProductionDeadline());
                        return capacityDeadline.isBefore(orderDate);
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                })
                .toList();

            // Sum total available vaccines for these capacities (only excessVaccines)
            int totalAvailable = validCapacities.stream()
                .mapToInt(ProducerCapacity::getExcessVaccines)
                .sum();

            if (totalAvailable < order.getVaccineQuantity()) {
                // Not enough capacity to fulfill this order, skip to next
                continue;
            }

            int remainingToFulfill = order.getVaccineQuantity();

            for (ProducerCapacity pc : validCapacities) {
                int available = pc.getExcessVaccines();

                if (available <= 0) continue;

                int used = Math.min(available, remainingToFulfill);

                // Deduct used vaccines only from excessVaccines
                int newExcess = available - used;
                pc.setExcessVaccines(newExcess);
                responseRepository.save(pc);

                remainingToFulfill -= used;
                if (remainingToFulfill == 0) break;
            }

            String fulfillUrl = ORDERING_BASE_URL + "/" + order.getId() + "/fulfill";
            restTemplate.postForObject(fulfillUrl, null, Void.class);
        }
    }

    @PostMapping("/capacities")
    public ResponseEntity<?> registerProducerCapacity(@RequestBody ProducerCapacityRequest capacityRequest) {
        try {
            ProducerCapacity savedCapacity = producerService.registerProducerCapacity(capacityRequest);
            return new ResponseEntity<>(savedCapacity, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/capacities")
    public ResponseEntity<List<ProducerCapacity>> getAllCapacities() {
        List<ProducerCapacity> capacities = producerService.getAllProducerCapacities();
        if (capacities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(capacities, HttpStatus.OK);
    }

    @GetMapping("/capacities/{producerName}")
    public ResponseEntity<List<ProducerCapacity>> getCapacitiesByProducer(@PathVariable String producerName) {
        List<ProducerCapacity> capacities = responseRepository.findByProducerName(producerName);
        if (capacities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(capacities, HttpStatus.OK);
    }
}