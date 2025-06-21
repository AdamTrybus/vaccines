package org.example;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;


public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByRegionIgnoreCase(String region);
    List<Order> findByStatusInAndExpectedDeliveryTimeBeforeOrderByExpectedDeliveryTimeAsc(List<String> statuses, LocalDate beforeDate);
    List<Order> findByStatusIn(List<String> statuses);

}