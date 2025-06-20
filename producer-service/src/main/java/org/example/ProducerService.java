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

        // Save the new capacity first
        ProducerCapacity producerCapacity = new ProducerCapacity();
        producerCapacity.setProducerName(request.getProducerName());
        producerCapacity.setVaccinesQuantity(request.getVaccinesQuantity());
        producerCapacity.setProductionDeadline(request.getProductionDeadline());
        producerCapacity.setExcessVaccines(0);
        responseRepository.save(producerCapacity);

        int totalExcessVaccines = responseRepository.findAll().stream()
            .mapToInt(ProducerCapacity::getExcessVaccines)
            .sum();

        int totalAvailableVaccines = totalExcessVaccines + request.getVaccinesQuantity();


        String url = ORDERING_BASE_URL + "/fulfill?availableVaccines=" + totalAvailableVaccines;
        int leftoverVaccines = restTemplate.postForObject(url, null, Integer.class);

        responseRepository.findAll().forEach(pc -> {
            pc.setExcessVaccines(0);
            responseRepository.save(pc);
        });

        producerCapacity.setExcessVaccines(leftoverVaccines);
        responseRepository.save(producerCapacity);

        return producerCapacity;
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