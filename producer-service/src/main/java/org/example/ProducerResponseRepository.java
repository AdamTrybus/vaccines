package org.example;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProducerResponseRepository extends JpaRepository<ProducerCapacity, Long> {
     List<ProducerCapacity> findByProducerName(String producerName);
     List<ProducerCapacity> findByProductionDeadlineBefore(String date);
}