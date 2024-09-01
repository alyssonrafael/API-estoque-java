package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.products.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductSizeRepository extends JpaRepository<ProductSize, String> {
}
