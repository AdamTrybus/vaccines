package org.example;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "vaccine_orders")
@Getter
@Setter
public class Order {
    private String region;
    private int vaccineQuantity;
    private int fulfilledQuantity;
    private String status;
    private String expectedDeliveryTime;

    // Getters
    public String getRegion() { return region; }
    public int getVaccineQuantity() { return vaccineQuantity; }
    public int getFulfilledQuantity() { return fulfilledQuantity; }
    public String getStatus() { return status; }
    public String getExpectedDeliveryTime() { return expectedDeliveryTime; }

    // Setters
    public void setRegion(String region) { this.region = region; }
    public void setVaccineQuantity(int vaccineQuantity) { this.vaccineQuantity = vaccineQuantity; }
    public void setFulfilledQuantity(int fulfilledQuantity) { this.fulfilledQuantity = fulfilledQuantity; }
    public void setStatus(String status) { this.status = status; }
    public void setExpectedDeliveryTime(String expectedDeliveryTime) { this.expectedDeliveryTime = expectedDeliveryTime; }
}