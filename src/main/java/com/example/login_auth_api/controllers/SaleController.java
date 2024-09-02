package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.sales.Sale;
import com.example.login_auth_api.dto.SaleDTO;
import com.example.login_auth_api.services.SaleService;
import com.example.login_auth_api.exceptions.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleDTO> createSale(@RequestBody SaleDTO saleDTO) {
        try {
            Sale sale = saleService.createSale(saleDTO);
            SaleDTO resultDTO = saleService.convertToDTO(sale);
            return ResponseEntity.ok(resultDTO);
        } catch (CustomException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Endpoint para listar todas as vendas
    @GetMapping
    public ResponseEntity<List<SaleDTO>> listAllSales() {
        List<SaleDTO> sales = saleService.listAllSales();
        return ResponseEntity.ok(sales);
    }

    // Endpoint para buscar uma venda pelo ID (ajustado para String)
    @GetMapping("/{id}")
    public ResponseEntity<SaleDTO> getSaleById(@PathVariable String id) {
        SaleDTO saleDTO;
        try {
            saleDTO = saleService.getSaleById(id);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retorna 404 se a venda não for encontrada
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retorna 500 para outros erros
        }
        return ResponseEntity.ok(saleDTO);
    }

    // Endpoint para deletar uma venda pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSaleById(@PathVariable String id) {
        try {
            saleService.deleteSaleById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Venda excluída com sucesso.");
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao excluir a venda.");
        }
    }
}
