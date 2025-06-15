package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producers")
public class ProducerController {
    @Autowired
    private ProducerService producerService;

    @PostMapping("/register-capacity")
    public ResponseEntity<?> registerProducerCapacity(@RequestBody ProducerCapacityRequest capacityRequest) {
        try {
            ProducerCapacity savedCapacity = producerService.registerProducerCapacity(capacityRequest);
            return new ResponseEntity<>(savedCapacity, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/capacities/all")
    public ResponseEntity<List<ProducerCapacity>> getAllCapacities() {
        List<ProducerCapacity> capacities = producerService.getAllProducerCapacities();
        if (capacities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(capacities, HttpStatus.OK);
    }
}