package org.example;

public class OrderRequest {
    private Long orderId;
    private Integer vaccineQuantity;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getVaccineQuantity() {
        return vaccineQuantity;
    }

    public void setVaccineQuantity(Integer vaccineQuantity) {
        this.vaccineQuantity = vaccineQuantity;
    }
}