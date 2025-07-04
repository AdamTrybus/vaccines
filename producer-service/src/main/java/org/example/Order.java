package org.example;

public class Order {
    private Long id;
    private String status;
    private String expectedDeliveryTime;
    private int vaccineQuantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExpectedDeliveryTime() { return expectedDeliveryTime; }
    public void setExpectedDeliveryTime(String expectedDeliveryTime) { this.expectedDeliveryTime = expectedDeliveryTime; }

    public int getVaccineQuantity() { return vaccineQuantity; }
    public void setVaccineQuantity(int vaccineQuantity) { this.vaccineQuantity = vaccineQuantity; }
}