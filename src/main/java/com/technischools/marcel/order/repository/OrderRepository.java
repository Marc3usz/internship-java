package com.technischools.marcel.order.repository;

import com.technischools.marcel.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {}
