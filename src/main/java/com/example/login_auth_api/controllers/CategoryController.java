package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.categories.Category;
import com.example.login_auth_api.dto.CategoryDTO;
import com.example.login_auth_api.repositories.CategoryRepository;
import com.example.login_auth_api.repositories.ProductRepository;
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

    @Autowired
    private final ProductRepository productRepository;

    public CategoryController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Rota para listar todas as categorias (não deletadas)
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findByDeletedFalse();
            return ResponseEntity.status(HttpStatus.OK).body(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Rota para pesquisar uma categoria pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria não encontrada");
            }
            return ResponseEntity.status(HttpStatus.OK).body(categoryOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para marcar uma categoria como deletada
    @PutMapping("/delete/{id}")
    public ResponseEntity<String> markAsDeleted(@PathVariable String id) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);

            if (categoryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria não encontrada");
            }

            Category category = categoryOpt.get();

            // Verifica se a categoria possui produtos associados
            boolean hasAssociatedProducts = productRepository.existsByCategoryId(category.getId());
            if (hasAssociatedProducts) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não é possível deletar a categoria. Existem produtos associados.");
            }

            category.setDeleted(true);
            categoryRepository.save(category);

            return ResponseEntity.status(HttpStatus.CREATED).body("Categoria marcada como deletada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para desmarcar uma categoria como deletada (setar deleted para false)
    @PutMapping("/restore/{id}")
    public ResponseEntity<String> unmarkAsDeleted(@PathVariable String id) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria não encontrada");
            }

            Category category = categoryOpt.get();
            category.setDeleted(false);
            categoryRepository.save(category);
            return ResponseEntity.status(HttpStatus.CREATED).body("Categoria restaurada");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para criar uma nova categoria com validação de nome duplicado
    @PostMapping
    public ResponseEntity<String> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            // Verificação de duplicidade pelo nome da categoria
            Optional<Category> existingCategory = categoryRepository.findByNome(categoryDTO.getNome());
            if (existingCategory.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Já existe uma categoria com esse nome.");
            }

            // Mapeamento do DTO para a entidade Category
            Category newCategory = new Category();
            newCategory.setNome(categoryDTO.getNome());
            newCategory.setDeleted(categoryDTO.getDeleted() != null ? categoryDTO.getDeleted() : false);

            categoryRepository.save(newCategory);
            return ResponseEntity.status(HttpStatus.OK).body("Categoria cadastrada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para atualizar o nome de uma categoria
    @PutMapping("/update-name/{id}")
    public ResponseEntity<String> updateCategoryName(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        try {
            // Obtém o novo nome do corpo da solicitação
            String newName = requestBody.get("newName");

            // Obtém a categoria pelo ID e verifica se ela existe
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria não encontrada");
            }

            // Valida se o novo nome é nulo ou vazio
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O novo nome da categoria não pode ser vazio");
            }

            // Verifica se o novo nome já está em uso
            Optional<Category> existingCategoryWithName = categoryRepository.findByNome(newName);
            if (existingCategoryWithName.isPresent() && !existingCategoryWithName.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito de nomes: essa categoria já existe");
            }

            // Atualiza o nome da categoria
            Category category = categoryOpt.get();
            category.setNome(newName);
            categoryRepository.save(category);

            return ResponseEntity.status(HttpStatus.OK).body("Nome da categoria atualizado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }
}
