package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/producers")
public class ProducerController {
    @Autowired
    private ProducerService producerService;

    @PostMapping("/request")
    public ResponseEntity<ProducerResponse> handleOrderRequest(@RequestBody OrderRequest orderRequest) {
        ProducerResponse response = producerService.processOrderRequest(orderRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

// Klasa pomocnicza do受け取ania żądania od Ordering Service
class OrderRequest {
    private Long orderId;
    private int vaccineQuantity;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public int getVaccineQuantity() { return vaccineQuantity; }
    public void setVaccineQuantity(int vaccineQuantity) { this.vaccineQuantity = vaccineQuantity; }
}
