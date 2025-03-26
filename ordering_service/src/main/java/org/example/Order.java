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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String region;
    private int cases;
    private int vaccineQuantity;
    private LocalDate expectedDeliveryTime;
    private String status;
}