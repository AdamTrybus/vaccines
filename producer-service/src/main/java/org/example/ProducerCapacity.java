package org.example;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity
@Table(name = "producer_capacity")
public class ProducerCapacity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String producerName;
    private int vaccinesQuantity;
    private String productionDeadline;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProducerName() { return producerName; }
    public void setProducerName(String producerName) { this.producerName = producerName; }
    public int getVaccinesQuantity() { return vaccinesQuantity; }
    public void setVaccinesQuantity(int vaccinesQuantity) { this.vaccinesQuantity = vaccinesQuantity; }
    public String getProductionDeadline() { return productionDeadline; }
    public void setProductionDeadline(String productionDeadline) { this.productionDeadline = productionDeadline; }
}