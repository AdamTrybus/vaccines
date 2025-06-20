package org.example;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByRegionIgnoreCase(String region);
    List<Order> findByStatusInOrderByExpectedDeliveryTimeAsc(List<String> statuses);
    List<Order> findByStatusIn(List<String> statuses);
}