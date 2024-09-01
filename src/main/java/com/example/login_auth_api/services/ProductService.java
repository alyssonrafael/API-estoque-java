package com.example.login_auth_api.services;

import com.example.login_auth_api.domain.products.Product;
import com.example.login_auth_api.domain.products.ProductSize;
import com.example.login_auth_api.domain.categories.Category;
import com.example.login_auth_api.dto.ProductDTO;
import com.example.login_auth_api.repositories.ProductRepository;
import com.example.login_auth_api.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Método para criar um produto
    public Product createProduct(ProductDTO productDTO) {
        // Lançamento de erro se a categoria não for encontrada
        Category category = categoryRepository.findById(String.valueOf(productDTO.getCategoryId()))
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com o ID: " + productDTO.getCategoryId()));

        // Lançamento de erro se o nome der conflito, ou seja, já existir
        Optional<Product> existingProduct = productRepository.findByName(productDTO.getName());
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with this name already exists.");
        }

        // Calcula a soma das quantities dos tamanhos associados ao produto e valida se há quantidades negativas
        int totalSizeQuantity = productDTO.getSizes().stream()
                .mapToInt(sizeDTO -> {
                    if (sizeDTO.getQuantity() < 0) {
                        throw new IllegalArgumentException("A quantidade dos tamanhos não pode ser negativa.");
                    }
                    return sizeDTO.getQuantity();
                })
                .sum();

        // Cria um novo produto
        Product product = new Product();
        product.setId(generateUniqueId());  // Gera um ID único
        product.setName(productDTO.getName());
        product.setCategory(category);
        product.setDeleted(false);
        product.setQuantity(totalSizeQuantity);  // Define a quantidade como a soma das quantities dos tamanhos
        product.setCost(productDTO.getCost());
        product.setPrice(productDTO.getPrice());

        if (productDTO.getSizes() != null) {
            List<ProductSize> sizes = productDTO.getSizes().stream()
                    .map(sizeDTO -> {
                        ProductSize size = new ProductSize();
                        size.setSize(sizeDTO.getSize());
                        size.setQuantity(sizeDTO.getQuantity());
                        size.setProduct(product);
                        return size;
                    })
                    .collect(Collectors.toList());
            product.setSizes(sizes);
        }

        return productRepository.save(product);
    }


    // Método para buscar um produto pelo ID
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com o ID: " + id));
    }

    // Método para buscar todos os produtos nao deletados
    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    // Método para atualizar um produto completamente
    public Product updateProduct(String id, ProductDTO productDTO) {
        // Busca o produto existente pelo ID
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com o ID: " + id));

        // Busca a categoria pelo ID
        Category category = categoryRepository.findById(String.valueOf(productDTO.getCategoryId()))
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com o ID: " + productDTO.getCategoryId()));

        // Verifica se o nome do produto já existe
        Optional<Product> existingProductWithName = productRepository.findByName(productDTO.getName());
        if (existingProductWithName.isPresent() && !existingProductWithName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Já existe um produto com este nome.");
        }

        // Atualiza as propriedades do produto
        existingProduct.setName(productDTO.getName());
        existingProduct.setCategory(category);

        // Define o custo e o preço do produto
        existingProduct.setCost(productDTO.getCost());
        existingProduct.setPrice(productDTO.getPrice());

        // Valida e atualiza os tamanhos
        if (productDTO.getSizes() != null) {
            // Calcula a soma das quantidades dos tamanhos
            int totalSizeQuantity = productDTO.getSizes().stream()
                    .mapToInt(sizeDTO -> {
                        if (sizeDTO.getQuantity() < 0) {
                            throw new IllegalArgumentException("A quantidade dos tamanhos não pode ser negativa.");
                        }
                        return sizeDTO.getQuantity();
                    })
                    .sum();

            // Verifica se a soma das quantidades dos tamanhos é negativa
            if (totalSizeQuantity < 0) {
                throw new IllegalArgumentException("A quantidade total não pode ser negativa.");
            }

            // Atualiza a quantidade do produto com base na soma das quantidades dos tamanhos
            existingProduct.setQuantity(totalSizeQuantity);

            // Remove todos os tamanhos existentes que não estão na nova lista
            existingProduct.getSizes().clear();

            // Adiciona novos tamanhos
            List<ProductSize> sizes = productDTO.getSizes().stream()
                    .map(sizeDTO -> {
                        ProductSize size = new ProductSize();
                        size.setSize(sizeDTO.getSize());
                        size.setQuantity(sizeDTO.getQuantity());
                        size.setProduct(existingProduct);
                        return size;
                    })
                    .collect(Collectors.toList());

            existingProduct.getSizes().addAll(sizes);
        } else {
            // Se não houver tamanhos fornecidos, zera a quantidade do produto
            existingProduct.setQuantity(0);
            existingProduct.getSizes().clear();
        }

        // Salva e retorna o produto atualizado
        return productRepository.save(existingProduct);
    }


    // Método para marcar um produto como deletado
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com o ID: " + id));

        product.setDeleted(true);
        productRepository.save(product);
    }

    // Método para restaurar um produto
    public void restoreProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com o ID: " + id));

        product.setDeleted(false);
        productRepository.save(product);
    }

    // Método para atualizar o nome do produto
    public void updateProductName(String id, String newName) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com o ID: " + id));

        // Verifica se o novo nome já está em uso
        if (productRepository.findByName(newName).isPresent() && !productRepository.findByName(newName).get().getId().equals(id)) {
            throw new IllegalStateException("Produto com este nome já existe.");
        }

        product.setName(newName);
        productRepository.save(product);
    }

    // Método para contar a quantidade total de produtos não deletados
    public long countProducts() {
        return productRepository.countByDeletedFalse();
    }

    // Método para gerar um ID único
    private String generateUniqueId() {
        return java.util.UUID.randomUUID().toString();
    }
}
