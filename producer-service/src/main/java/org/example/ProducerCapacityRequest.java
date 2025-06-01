package org.example;

public class ProducerCapacityRequest {
    private String producerName;
    private int vaccinesQuantity; // Zmieniono nazwę pola
    private String productionDeadline;

    public String getProducerName() { return producerName; }
    public void setProducerName(String producerName) { this.producerName = producerName; }
    public int getVaccinesQuantity() { return vaccinesQuantity; }
    public void setVaccinesQuantity(int vaccinesQuantity) { this.vaccinesQuantity = vaccinesQuantity; }
    public String getProductionDeadline() { return productionDeadline; }
    public void setProductionDeadline(String productionDeadline) { this.productionDeadline = productionDeadline; }
}