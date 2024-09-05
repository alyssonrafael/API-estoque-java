package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.products.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByName(String name);

    List<Product> findByDeletedFalseOrderByCreatedAtDesc();

    List<Product> findByDeletedTrueOrderByCreatedAtDesc();

    boolean existsByCategoryId(String category);

    long countByDeletedFalse();
}
