package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import java.util.stream.Stream;

@EnableScheduling
@Service
public class ProducerService {
    @Autowired
    private ProducerResponseRepository responseRepository;

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    private final String ORDERING_BASE_URL = "http://ordering:8081/api/orders";

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

        LocalDate productionDeadlineDate;
        try {
            productionDeadlineDate = LocalDate.parse(request.getProductionDeadline());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid production deadline format. Use YYYY-MM-DD.");
        }

        // Save new capacity
        ProducerCapacity newCapacity = new ProducerCapacity();
        newCapacity.setProducerName(request.getProducerName());
        newCapacity.setVaccinesQuantity(request.getVaccinesQuantity());
        newCapacity.setProductionDeadline(request.getProductionDeadline());
        newCapacity.setExcessVaccines(request.getVaccinesQuantity());
        responseRepository.save(newCapacity);

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

        for (Order order : pendingOrders) {
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

        return newCapacity;
    }

    public List<ProducerCapacity> getAllProducerCapacities() {
        return responseRepository.findAll();
    }

    @Scheduled(fixedRate = 600000)
    public void expireOldOrders() {
        String url = ORDERING_BASE_URL + "/pending";
        List<Order> pendingOrders = null;
        try {
            pendingOrders = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Order>>() {}
            ).getBody();
        } catch (Exception e) {
            log.error("Error fetching pending orders", e);
            return;
        }

        LocalDate today = LocalDate.now();
        if (pendingOrders != null) {
            for (Order order : pendingOrders) {
                try {
                    LocalDate expectedDate = LocalDate.parse(order.getExpectedDeliveryTime());
                    if (expectedDate.isBefore(today)) {
                        // Call ordering-service to update order status to EXPIRED
                        String updateUrl = ORDERING_BASE_URL + "/" + order.getId() + "/status?newStatus=EXPIRED";
                        restTemplate.postForObject(updateUrl, null, Void.class);
                    }
                } catch (DateTimeParseException e) {
                    log.error("Invalid date format for order id " + order.getId(), e);
                } catch (Exception e) {
                    log.error("Error updating order status for order id " + order.getId(), e);
                }
            }
        }
    }
}