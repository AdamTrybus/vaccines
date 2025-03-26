package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {
    @Autowired
    private ProducerResponseRepository responseRepository;

    public ProducerResponse processOrderRequest(OrderRequest request) {
        ProducerResponse response = new ProducerResponse();
        response.setOrderId(request.getOrderId());
        response.setAvailableVaccines(request.getVaccineQuantity()); // Przykład logiki
        response.setDeliveryTime("2025-04-01"); // Przykład
        return responseRepository.save(response);
    }
}
