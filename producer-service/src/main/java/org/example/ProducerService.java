package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProducerService {
    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);
    @Autowired
    private ProducerResponseRepository responseRepository;

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

        ProducerCapacity producerCapacity = new ProducerCapacity();
        producerCapacity.setProducerName(request.getProducerName());
        producerCapacity.setVaccinesQuantity(request.getVaccinesQuantity());
        producerCapacity.setProductionDeadline(request.getProductionDeadline());

        return responseRepository.save(producerCapacity);
    }

    public List<ProducerCapacity> getAllProducerCapacities() {
        return responseRepository.findAll();
    }
}