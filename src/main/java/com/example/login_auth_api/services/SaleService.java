package com.example.login_auth_api.services;

import com.example.login_auth_api.domain.sales.PaymentMethod;
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

    //Método para criar uma venda
    @Transactional
    public Sale createSale(SaleDTO saleDTO) {
        // Criação da venda com campos básicos
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setObservation(saleDTO.getObservation());
        sale.setPaymentMethod(saleDTO.getPaymentMethod());
        sale.setIsGift(saleDTO.isIsGift());

        try {
            PaymentMethod paymentMethod = PaymentMethod.valueOf(String.valueOf(saleDTO.getPaymentMethod()));
            sale.setPaymentMethod(paymentMethod);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Método de pagamento inválido. Métodos aceitos: PIX, DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO.");
        }

        // Verifica e define o desconto, se não for fornecido, define como zero
        BigDecimal discount = saleDTO.getDiscount() != null ? saleDTO.getDiscount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("O desconto não pode ser negativo.");
        }
        sale.setDiscount(discount);

        // Associa o usuário usando o UUID como String
        User user = userRepository.findById(saleDTO.getUserId())
                .orElseThrow(() -> new CustomException("Usuário não encontrado."));
        sale.setUser(user);

        // Verifica se há pelo menos um produto na venda
        if (saleDTO.getItems() == null || saleDTO.getItems().isEmpty()) {
            throw new CustomException("A venda deve conter pelo menos um produto.");
        }

        // Salva a Sale primeiro para gerar um ID para a venda
        sale = saleRepository.save(sale);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal subtotal = BigDecimal.ZERO;

        // Itera sobre os itens da venda para adicionar ao banco de dados
        for (SaleDTO.SaleItemDTO itemDTO : saleDTO.getItems()) {
            // Verifica se a quantidade do produto é maior que zero
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new CustomException("A quantidade do produto deve ser maior que zero.");
            }

            // Busca o produto pelo ID
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new CustomException("Produto não encontrado."));

            // Busca o tamanho do produto pelo ID
            ProductSize size = productSizeRepository.findById(itemDTO.getSizeId())
                    .orElseThrow(() -> new CustomException("Tamanho do produto não encontrado."));

            // Verifica se a quantidade disponível é suficiente
            if (size.getQuantity() < itemDTO.getQuantity()) {
                // Inclui o nome do produto e o tamanho na mensagem de erro
                String errorMessage = String.format(
                        "Quantidade insuficiente para o produto: Nome = %s,\n Tamanho = %s,\n Quantidade disponível = %d,\n Quantidade solicitada = %d.",
                        product.getName(),
                        size.getSize(),
                        size.getQuantity(),
                        itemDTO.getQuantity()
                );
                throw new CustomException(errorMessage);
            }

            // Calcula o total do item (preço * quantidade)
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));

            // Cria o item da venda e ajusta as quantidades do produto e do tamanho
            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setSize(size);
            saleItem.setQuantity(itemDTO.getQuantity());

            // Atualiza a quantidade do tamanho e do produto
            size.setQuantity(size.getQuantity() - itemDTO.getQuantity());
            productSizeRepository.save(size);

            product.setQuantity(product.getQuantity() - itemDTO.getQuantity());
            product.setQuantitySold(product.getQuantitySold() + itemDTO.getQuantity());
            productRepository.save(product);

            // Adiciona o item à venda e salva no banco
            saleItem.setSale(sale);
            saleItemRepository.save(saleItem);
            sale.addSaleItem(saleItem);

            // Atualiza os totais da venda
            totalAmount = totalAmount.add(itemTotal);
            subtotal = subtotal.add(itemTotal);
        }

        // Verifica se o desconto não é maior que o total da venda
        if (discount.compareTo(subtotal) > 0) {
            throw new CustomException("O desconto não pode ser maior que o valor total da venda.");
        }

        // Calcula o subtotal final após o desconto
        BigDecimal finalSubtotal = subtotal.subtract(discount).max(BigDecimal.ZERO);
        sale.setSubtotal(finalSubtotal);
        sale.setTotalAmount(totalAmount);

        // Salva a venda atualizada
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
                    itemDTO.setPrice(item.getProduct().getPrice()); //pega o preço do produto
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
            throw new CustomException("Venda nao encontrada!");
        }
        Sale sale = optionalSale.get();
        return convertToDTO(sale);
    }

    // Método para listar todas as vendas em ordem cronológica (do mais recente para o mais velho)
    public List<SaleDTO> listAllSales() {
        List<Sale> sales = saleRepository.findAllByOrderBySaleDateDesc(); // Retorna em ordem decrescente
        return sales.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Método para listar as últimas 5 vendas
    public List<SaleDTO> listLastFiveSales() {
        List<Sale> sales = saleRepository.findTop5ByOrderBySaleDateDesc();
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

            // Atualizar a quantidade vendida, garantindo que não fique negativa
            int updatedQuantitySold = product.getQuantitySold() - saleItem.getQuantity();
            if (updatedQuantitySold < 0) {
                updatedQuantitySold = 0; // Se o valor calculado for negativo, definir como zero
            }
            product.setQuantitySold(updatedQuantitySold);
            productRepository.save(product);
        }

        // Remover os itens da venda
        saleItemRepository.deleteAll(sale.getItems());

        // Remover a venda
        saleRepository.delete(sale);
    }

    // Método para listar um range de vendas
    public List<SaleDTO> listSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);
        return sales.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Lista as vendas por intervalo de datas e status isGift.
    public List<SaleDTO> listSalesByDateRangeAndGiftStatus(LocalDateTime start, LocalDateTime end, Boolean isGift) {
        List<Sale> sales;
        if (isGift == null) {
            // Se isGift é null, busca todas as vendas no intervalo sem filtrar por isGift.
            sales = saleRepository.findBySaleDateBetween(start, end);
        } else {
            // Busca as vendas filtradas pelo status isGift.
            sales = saleRepository.findBySaleDateBetweenAndIsGift(start, end, isGift);
        }
        return sales.stream().map(this::convertToDTO).toList();
    }

    // Lista todas as vendas por status isGift.
    public List<SaleDTO> listAllSalesByGiftStatus(Boolean isGift) {
        List<Sale> sales;
        if (isGift == null) {
            // Se isGift é null, busca todas as vendas sem filtrar por isGift.
            sales = saleRepository.findAll();
        } else {
            // Busca as vendas filtradas pelo status isGift.
            sales = saleRepository.findByIsGift(isGift);
        }
        return sales.stream().map(this::convertToDTO).toList();
    }


}
