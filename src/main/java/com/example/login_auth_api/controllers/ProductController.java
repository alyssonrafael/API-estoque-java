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
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> findAllProducts() {
        try {
            List<Product> products = productService.findAllProducts();
            return ResponseEntity.ok(products);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<Product>> getAllDeletedProducts() {
        try {
            List<Product> products = productService.getAllProductsDeleted();
            return ResponseEntity.ok(products);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Rota para pesquisar um produto pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para criar um novo produto
    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        try {
            productService.createProduct(productDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Produto criado com sucesso!");
        } catch (IllegalArgumentException e) {
            // Verifica a mensagem da exceção e define o status HTTP 409
            if (e.getMessage().contains("Já existe um produto com esse nome")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            if (e.getMessage().contains("Categoria não encontrada")){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            // Caso genérico, retorna um erro com status 400
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para atualizar um produto completamente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody ProductDTO productDTO) {
        try {
            Product updatedProduct = productService.updateProduct(id, productDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().contains("Produto não encontrado")){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
            }
            if (ex.getMessage().contains("Categoria não encontrada")){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
            }
            if (ex.getMessage().contains("Já existe um produto com este nome.")){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para marcar um produto como deletado
    @PutMapping("/delete/{id}")
    public ResponseEntity<String> markAsDeleted(@PathVariable String id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Produto marcado como deletado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para desmarcar um produto como deletado (setar deleted para false)
    @PutMapping("/restore/{id}")
    public ResponseEntity<String> unmarkAsDeleted(@PathVariable String id) {
        try {
            productService.restoreProduct(id);
            return ResponseEntity.ok("Produto restaurado com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para atualizar o nome de um produto
    @PutMapping("/update-name/{id}")
    public ResponseEntity<String> updateProductName(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        String newName = requestBody.get("newName");
        try {
            productService.updateProductName(id, newName);
            return ResponseEntity.ok("Nome do produto atualizado com sucesso");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("O nome do produto não pode ser nulo ou vazio.")){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // Captura qualquer outra exceção e retorna erro interno do servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    //rota pra contar a quantidade de produtos
    @GetMapping("/count")
    public ResponseEntity<String> getProductCount() {
        try {
            // Obtém a contagem de produtos não deletados usando o serviço de produtos
            long productCount = productService.countProducts();
            String message = "Quantidade de produtos não deletados: " + productCount;
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            // Retorna uma resposta de erro interno do servidor com a mensagem da exceção
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }
}
