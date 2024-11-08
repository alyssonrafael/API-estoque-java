package com.example.login_auth_api.services;

import com.example.login_auth_api.domain.products.Product;
import com.example.login_auth_api.domain.products.ProductSize;
import com.example.login_auth_api.domain.categories.Category;
import com.example.login_auth_api.dto.ProductDTO;
import com.example.login_auth_api.dto.ProductSizeDTO;
import com.example.login_auth_api.repositories.ProductRepository;
import com.example.login_auth_api.repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

        // Verifica se o número total de produtos já atingiu o limite de 300
        long totalProducts = productRepository.count();
        if (totalProducts >= 300) {
            throw new IllegalArgumentException("Limite máximo de 300 produtos atingido. Não é possível criar mais produtos. Verifique a possibilidae de expancão com o suporte");
        }

        // Lançamento de erro se a categoria não for encontrada
        Category category = categoryRepository.findById(String.valueOf(productDTO.getCategoryId()))
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        // Lançamento de erro se o nome der conflito, ou seja, já existir
        Optional<Product> existingProduct = productRepository.findByName(productDTO.getName());
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Já existe um produto com esse nome");
        }

        // Agrupa os tamanhos com o mesmo valor e soma suas quantidades
        Map<String, Integer> combinedSizes = productDTO.getSizes().stream()
                .collect(Collectors.toMap(
                        ProductSizeDTO::getSize,
                        ProductSizeDTO::getQuantity,
                        Integer::sum
                ));

        // Converte o Map de tamanhos combinados de volta para uma lista de ProductSizeDTO
        List<ProductSizeDTO> combinedSizeList = combinedSizes.entrySet().stream()
                .map(entry -> new ProductSizeDTO(null, entry.getKey(), entry.getValue()))  // Define o id como null ou algum valor padrão
                .collect(Collectors.toList());

        // Verifica se o produto tem mais de 7 tamanhos após o agrupamento
        if (combinedSizeList.size() > 7) {
            throw new IllegalArgumentException("O produto não pode ter mais de 7 tamanhos.");
        }

        // Validação dos tamanhos agrupados e cálculo da quantidade total
        int totalSizeQuantity = combinedSizeList.stream()
                .mapToInt(sizeDTO -> {
                    if (sizeDTO.getSize() == null || sizeDTO.getSize().trim().isEmpty()) {
                        throw new IllegalArgumentException("O tamanho não pode ser nulo ou vazio.");
                    }
                    if (sizeDTO.getQuantity() == null || sizeDTO.getQuantity() < 0) {
                        throw new IllegalArgumentException("A quantidade dos tamanhos não pode ser negativa ou nula.");
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

        // Criação dos tamanhos associados ao produto, se existirem
        if (productDTO.getSizes() != null) {
            List<ProductSize> sizes = combinedSizeList.stream()
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

        // Salva e retorna o produto criado
        return productRepository.save(product);
    }

    // Método para buscar um produto pelo ID
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
    }

    //Método para listar todos independente se ta deletado ou nao
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    // Método para buscar todos os produtos nao deletados
    public List<Product> getAllProducts() {
        return productRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    // Método para buscar todos os produtos deletados
    public List<Product> getAllProductsDeleted() {
        return productRepository.findByDeletedTrueOrderByCreatedAtDesc();
    }

    // Método para atualizar um produto completamente
    @Transactional
    public Product updateProduct(String id, ProductDTO productDTO) {
        // Busca o produto existente pelo ID
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        // Busca a categoria pelo ID
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

        // Verifica se o nome do produto já existe
        Optional<Product> existingProductWithName = productRepository.findByName(productDTO.getName());
        if (existingProductWithName.isPresent() && !existingProductWithName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Já existe um produto com este nome.");
        }

        // Atualiza as propriedades do produto
        existingProduct.setName(productDTO.getName());
        existingProduct.setCategory(category);
        existingProduct.setCost(productDTO.getCost());
        existingProduct.setPrice(productDTO.getPrice());

        // Cria um mapa de tamanhos existentes para fácil acesso
        Map<String, ProductSize> existingSizesMap = existingProduct.getSizes().stream()
                .collect(Collectors.toMap(ProductSize::getSize, size -> size));

        // Conta o número de tamanhos existentes e os que estão sendo passados no DTO
        Set<String> combinedSizes = new HashSet<>(existingSizesMap.keySet());
        combinedSizes.addAll(productDTO.getSizes().stream()
                .map(ProductSizeDTO::getSize)
                .collect(Collectors.toSet()));

        // Verifica se o total de tamanhos é maior que 7
        if (combinedSizes.size() > 7) {
            throw new IllegalArgumentException("O produto não pode ter mais de 7 tamanhos.");
        }

        BigDecimal totalQuantity = BigDecimal.ZERO;

        // Atualiza ou adiciona tamanhos do DTO
        for (ProductSizeDTO sizeDTO : productDTO.getSizes()) {
            // Valida se o nome do tamanho está vazio
            if (sizeDTO.getSize() == null || sizeDTO.getSize().trim().isEmpty()) {
                throw new IllegalArgumentException("O nome do tamanho não pode estar vazio.");
            }

            // Valida se a quantidade é maior ou igual a 0
            if (sizeDTO.getQuantity() == null || sizeDTO.getQuantity() < 0) {
                throw new IllegalArgumentException("A quantidade do tamanho deve ser maior ou igual a 0.");
            }

            // Recupera o tamanho existente ou cria um novo se necessário
            ProductSize size = existingSizesMap.get(sizeDTO.getSize());
            if (size != null) {
                // Se o tamanho já existir, atualiza a quantidade
                size.setQuantity(sizeDTO.getQuantity());
            } else {
                // Se o tamanho não existir, cria um novo
                size = new ProductSize();
                size.setSize(sizeDTO.getSize());
                size.setQuantity(sizeDTO.getQuantity());
                size.setProduct(existingProduct);
                existingProduct.getSizes().add(size);
            }
            totalQuantity = totalQuantity.add(BigDecimal.valueOf(size.getQuantity()));
        }

        // Adiciona a quantidade dos tamanhos que já estavam cadastrados e que não foram incluídos no DTO
        for (ProductSize existingSize : existingProduct.getSizes()) {
            if (productDTO.getSizes().stream().noneMatch(sizeDTO -> sizeDTO.getSize().equals(existingSize.getSize()))) {
                totalQuantity = totalQuantity.add(BigDecimal.valueOf(existingSize.getQuantity()));
            }
        }

        // Atualiza a quantidade total do produto
        existingProduct.setQuantity(totalQuantity.intValue());

        // Salva e retorna o produto atualizado
        return productRepository.save(existingProduct);
    }

    // Método para marcar um produto como deletado
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        product.setDeleted(true);
        productRepository.save(product);
    }

    // Método para restaurar um produto
    public void restoreProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        product.setDeleted(false);
        productRepository.save(product);
    }

    // Método para atualizar o nome do produto
    public void updateProductName(String id, String newName) {

        // Verifica se o novo nome é nulo ou vazio
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto não pode ser nulo ou vazio.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

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
