package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.products.Product;
import com.example.login_auth_api.dto.ProductDTO;
import com.example.login_auth_api.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Rota para listar todos os produtos (não deletados)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    // Rota para pesquisar um produto pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Rota para criar um novo produto com validação de nome duplicado
    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            productService.createProduct(productDTO);
            return ResponseEntity.ok("Product created successfully.");
        } catch (IllegalArgumentException e) {
            // Verifica a mensagem da exceção e define o status HTTP 409
            if (e.getMessage().contains("Product with this name already exists.")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            // Caso genérico, retorna um erro com status 400
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Rota para atualizar um produto
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable String id, @Valid @RequestBody ProductDTO productDTO) {
        try {
            productService.updateProduct(id, productDTO);
            return ResponseEntity.ok("Product updated successfully.");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Já existe um produto com este nome.")){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            if (e.getMessage().contains("A quantidade do produto não pode ser menor que a soma das quantidades dos tamanhos associados.")){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Rota para marcar um produto como deletado
    @PutMapping("/delete/{id}")
    public ResponseEntity<String> markAsDeleted(@PathVariable String id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Product marked as deleted.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Rota para desmarcar um produto como deletado (setar deleted para false)
    @PutMapping("/restore/{id}")
    public ResponseEntity<String> unmarkAsDeleted(@PathVariable String id) {
        try {
            productService.restoreProduct(id);
            return ResponseEntity.ok("Product restored successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Rota para atualizar o nome de um produto
    @PutMapping("/update-name/{id}")
    public ResponseEntity<String> updateProductName(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        String newName = requestBody.get("newName");
        try {
            productService.updateProductName(id, newName);
            return ResponseEntity.ok("Product name updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    //rota pra contar a quantidade de produtos
    @GetMapping("/count")
    public ResponseEntity<Long> getProductCount() {
        long productCount = productService.countProducts();
        return ResponseEntity.ok(productCount);
    }
}
