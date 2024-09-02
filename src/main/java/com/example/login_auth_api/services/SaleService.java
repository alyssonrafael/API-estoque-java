package com.example.login_auth_api.services;

import com.example.login_auth_api.domain.sales.Sale;
import com.example.login_auth_api.domain.sales.SaleItem;
import com.example.login_auth_api.domain.products.Product;
import com.example.login_auth_api.domain.products.ProductSize;
import com.example.login_auth_api.domain.user.User;
import com.example.login_auth_api.exceptions.CustomException;
import com.example.login_auth_api.dto.SaleDTO;
import com.example.login_auth_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductSizeRepository productSizeRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Transactional
    public Sale createSale(SaleDTO saleDTO) {
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setObservation(saleDTO.getObservation());
        sale.setPaymentMethod(saleDTO.getPaymentMethod());
        sale.setIsGift(saleDTO.isIsGift());

        // Verifica e define o desconto
        BigDecimal discount = saleDTO.getDiscount() != null ? saleDTO.getDiscount() : BigDecimal.ZERO;
        sale.setDiscount(discount);

        // Associa o usuário usando o UUID como String
        User user = userRepository.findById(saleDTO.getUserId()) // Supondo que o UUID está armazenado no campo "id" da entidade User
                .orElseThrow(() -> new CustomException("Usuário não encontrado."));
        sale.setUser(user);

        // Salva a Sale primeiro
        sale = saleRepository.save(sale);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (SaleDTO.SaleItemDTO itemDTO : saleDTO.getItems()) {
            Optional<ProductSize> optionalSize = productSizeRepository.findById(itemDTO.getSizeId());
            if (optionalSize.isEmpty()) {
                throw new CustomException("Tamanho do produto não encontrado.");
            }

            ProductSize size = optionalSize.get();
            if (size.getQuantity() < itemDTO.getQuantity()) {
                throw new CustomException("Quantidade insuficiente para o tamanho do produto.");
            }

            Optional<Product> optionalProduct = productRepository.findById(itemDTO.getProductId());
            if (optionalProduct.isEmpty()) {
                throw new CustomException("Produto não encontrado.");
            }

            Product product = optionalProduct.get();
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setSize(size);
            saleItem.setQuantity(itemDTO.getQuantity());

            size.setQuantity(size.getQuantity() - itemDTO.getQuantity());
            productSizeRepository.save(size);

            product.setQuantity(product.getQuantity() - itemDTO.getQuantity());
            productRepository.save(product);

            saleItem.setSale(sale);
            saleItemRepository.save(saleItem);
            sale.addSaleItem(saleItem);

            totalAmount = totalAmount.add(itemTotal);
            subtotal = subtotal.add(itemTotal);
        }

        BigDecimal finalSubtotal = subtotal.subtract(discount).max(BigDecimal.ZERO);
        sale.setSubtotal(finalSubtotal);
        sale.setTotalAmount(totalAmount);

        sale = saleRepository.save(sale);

        return sale;
    }

    public SaleDTO convertToDTO(Sale sale) {
        SaleDTO saleDTO = new SaleDTO();
        saleDTO.setId(sale.getId());
        saleDTO.setSaleDate(sale.getSaleDate());
        saleDTO.setTotalAmount(sale.getTotalAmount());
        saleDTO.setSubtotal(sale.getSubtotal());
        saleDTO.setDiscount(sale.getDiscount());
        saleDTO.setObservation(sale.getObservation());
        saleDTO.setPaymentMethod(sale.getPaymentMethod());
        saleDTO.setUserId(sale.getUser().getId()); // Aqui, use o campo correto que representa o UUID como String na sua entidade User
        saleDTO.setUserName(sale.getUser().getName());
        saleDTO.setIsGift(sale.getIsGift());

        List<SaleDTO.SaleItemDTO> itemDTOs = sale.getItems().stream()
                .map(item -> {
                    // Cria o DTO do item com os campos adicionais de nome do produto e do tamanho
                    SaleDTO.SaleItemDTO itemDTO = new SaleDTO.SaleItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName()); // Nome do produto
                    itemDTO.setSizeId(item.getSize().getId());
                    itemDTO.setSizeName(item.getSize().getSize()); // Nome do tamanho
                    itemDTO.setQuantity(item.getQuantity());
                    return itemDTO;
                })
                .toList();

        saleDTO.setItems(itemDTOs);

        return saleDTO;
    }

    // Método para buscar uma venda por ID (ajustado para String)
    public SaleDTO getSaleById(String id) {
        Optional<Sale> optionalSale = saleRepository.findById(id);

        if (optionalSale.isEmpty()) {
            throw new CustomException("Venda não encontrada."); // Exceção personalizada para venda não encontrada
        }

        Sale sale = optionalSale.get();
        return convertToDTO(sale); // Converte a venda encontrada para SaleDTO
    }

    // Método para listar todas as vendas
    public List<SaleDTO> listAllSales() {
        List<Sale> sales = saleRepository.findAll();
        return sales.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteSaleById(String saleId) {
        // Encontrar a venda
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new CustomException("Venda não encontrada."));

        // Atualizar os itens da venda
        for (SaleItem saleItem : sale.getItems()) {
            // Atualizar a quantidade do tamanho do produto
            ProductSize size = saleItem.getSize();
            size.setQuantity(size.getQuantity() + saleItem.getQuantity());
            productSizeRepository.save(size);

            // Atualizar a quantidade total do produto
            Product product = saleItem.getProduct();
            product.setQuantity(product.getQuantity() + saleItem.getQuantity());
            productRepository.save(product);
        }

        // Remover os itens da venda
        saleItemRepository.deleteAll(sale.getItems());

        // Remover a venda
        saleRepository.delete(sale);
    }


}
