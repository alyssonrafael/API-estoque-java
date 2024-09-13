package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.sales.Sale;
import com.example.login_auth_api.dto.SaleDTO;
import com.example.login_auth_api.services.SaleService;
import com.example.login_auth_api.exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    //Endpoint para criar uma venda
    @PostMapping
    public ResponseEntity<Object> createSale(@RequestBody SaleDTO saleDTO) {
        try {
            Sale sale = saleService.createSale(saleDTO);
            SaleDTO resultDTO = saleService.convertToDTO(sale);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultDTO);
        } catch (CustomException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Método de pagamento inválido. Métodos aceitos: PIX, DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO.");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Endpoint para listar todas as vendas
    @GetMapping
    public ResponseEntity<?> listAllSales() {
        try {
            List<SaleDTO> sales = saleService.listAllSales();
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            // Retorna uma mensagem de erro no corpo da resposta
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    @GetMapping("/last-five-sales")
    public ResponseEntity<?> listLastFiveSales() {
        try {
            List<SaleDTO> sales = saleService.listLastFiveSales();
            return ResponseEntity.ok(sales);
        } catch (Exception e) {
            // Retorna uma mensagem de erro no corpo da resposta
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Endpoint para buscar uma venda pelo ID (ajustado para String)
    @GetMapping("/{id}")
    public ResponseEntity<?> getSaleById(@PathVariable String id) {
        try {
            SaleDTO saleDTO = saleService.getSaleById(id);
            return ResponseEntity.ok(saleDTO);
        } catch (CustomException e) {
            // Retorna a mensagem de erro como uma String simples
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Retorna uma mensagem de erro genérica para outros erros
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Endpoint para deletar uma venda pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSaleById(@PathVariable String id) {
        try {
            saleService.deleteSaleById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Venda excluída com sucesso.");
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Método para listar vendas dentro de um intervalo de datas
    @GetMapping("/salesByDateRange")
    public ResponseEntity<List<SaleDTO>> getSalesByDateRange(@RequestParam String start, @RequestParam String end) {

        // Define o formato esperado das datas
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // Converte as strings para LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);

        // Obtém a lista de vendas no intervalo especificado
        List<SaleDTO> sales = saleService.listSalesByDateRange(startDateTime, endDateTime);

        // Retorna a lista de vendas em formato JSON
        return ResponseEntity.ok(sales);
    }
}
