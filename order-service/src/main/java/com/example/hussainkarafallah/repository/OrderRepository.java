package com.example.hussainkarafallah.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.core.annotation.Order;
import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(domainClass = Order.class ,idClass = UUID.class)
public interface OrderRepository {
    Optional<Order> findById(UUID id);

    void save(Order order);
}
