package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.categories.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    // Busca todas as categorias que não estão deletadas
    List<Category> findByDeletedFalse();

    // Busca uma categoria pelo nome
    Optional<Category> findByNome(String nome);
}
