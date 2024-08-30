package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.categories.Category;
import com.example.login_auth_api.dto.CategoryDTO;
import com.example.login_auth_api.repositories.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    // Rota para listar todas as categorias (não deletadas)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findByDeletedFalse();
        return ResponseEntity.ok(categories);
    }

    // Rota para pesquisar uma categoria pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
        }
        return ResponseEntity.ok(categoryOpt.get());
    }

    // Rota para marcar uma categoria como deletada
    @PutMapping("/delete/{id}")
    public ResponseEntity<String> markAsDeleted(@PathVariable String id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
        }
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found."));
        category.setDeleted(true);
        categoryRepository.save(category);
        return ResponseEntity.ok("Category marked as deleted.");
    }

    // Rota para desmarcar uma categoria como deletada (setar deleted para false)
    @PutMapping("/restore/{id}")
    public ResponseEntity<String> unmarkAsDeleted(@PathVariable String id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
        }
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found."));
        category.setDeleted(false);
        categoryRepository.save(category);
        return ResponseEntity.ok("Category restored successfully.");
    }

    // Rota para criar uma nova categoria com validação de nome duplicado
    @PostMapping
    public ResponseEntity<String> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        // Verificação de duplicidade pelo nome da categoria
        Optional<Category> existingCategory = categoryRepository.findByNome(categoryDTO.getNome());
        if (existingCategory.isPresent()) {
            return ResponseEntity.badRequest().body("Category with this name already exists.");
        }

        // Mapeamento do DTO para a entidade Category
        Category newCategory = new Category();
        newCategory.setNome(categoryDTO.getNome());
        newCategory.setDeleted(categoryDTO.getDeleted() != null ? categoryDTO.getDeleted() : false);

        categoryRepository.save(newCategory);
        return ResponseEntity.ok("Category created successfully.");
    }


    // Rota para atualizar o nome de uma categoria
    @PutMapping("/update-name/{id}")
    public ResponseEntity<String> updateCategoryName(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        // Obtém o novo nome do corpo da solicitação
        String newName = requestBody.get("newName");
        // Obtém a categoria pelo ID e verifica se ela existe
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category not found.");
        }

        // Valida se o novo nome é nulo ou vazio
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("New name cannot be null or empty.");
        }

        // Verifica se o novo nome já está em uso
        Optional<Category> existingCategoryWithName = categoryRepository.findByNome(newName);
        if (existingCategoryWithName.isPresent() && !existingCategoryWithName.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict of names");
        }


        // Atualiza o nome da categoria
        Category category = categoryOpt.get();
        category.setNome(newName);
        categoryRepository.save(category);

        return ResponseEntity.ok("Category name updated successfully.");
    }
}
