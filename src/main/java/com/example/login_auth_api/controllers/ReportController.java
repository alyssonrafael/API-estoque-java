package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.products.Product;
import com.example.login_auth_api.dto.ProductDTO;
import com.example.login_auth_api.dto.SaleDTO;
import com.example.login_auth_api.services.ProductService;
import com.example.login_auth_api.services.reports.ReportProductsService;
import com.example.login_auth_api.services.reports.ReportSalesService;
import com.example.login_auth_api.services.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportSalesService reportSalesService;

    @Autowired
    private ReportProductsService reportProductsService;

    @Autowired
    private ProductService productService; // Serviço que lida com os produto

    @Autowired
    private SaleService saleService; // Serviço que busca as vendas do banco de dados


    //Rota para listar todas as vendas ou vendas por data no formato PDF ou CSV
    @GetMapping("/sales")
    public ResponseEntity<byte[]> getSalesReport(
            @RequestParam(required = false, defaultValue = "csv") String format,  // Parâmetro obrigatório que especifica o formato do relatório (csv ou pdf).
            @RequestParam(required = false) String start,  // Parâmetro opcional que define a data e hora de início do intervalo de datas no formato "yyyy-MM-dd'T'HH:mm:ss".
            // Parâmetro opcional que define a data e hora de fim do intervalo de datas no formato "yyyy-MM-dd'T'HH:mm:ss".
            @RequestParam(required = false) String end) throws IOException {

        // Declara uma lista de vendas que será preenchida com os dados das vendas.
        List<SaleDTO> sales;

        // Define o formato para analisar as datas fornecidas.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // Inicializa variáveis para armazenar as datas de início e fim, com valor padrão nulo.
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        // Verifica se o parâmetro de data e hora de início foi fornecido e não está vazio.
        if (start != null && !start.isEmpty()) {
            // Converte o parâmetro de string para LocalDateTime usando o formato especificado.
            startDateTime = LocalDateTime.parse(start, formatter);
        }

        // Verifica se o parâmetro de data e hora de fim foi fornecido e não está vazio.
        if (end != null && !end.isEmpty()) {
            // Converte o parâmetro de string para LocalDateTime usando o formato especificado.
            endDateTime = LocalDateTime.parse(end, formatter);
        }

        // Se ambas as datas de início e fim foram fornecidas e convertidas, busca as vendas dentro do intervalo de datas.
        if (startDateTime != null && endDateTime != null) {
            sales = saleService.listSalesByDateRange(startDateTime, endDateTime);
        } else {
            // Se as datas não foram fornecidas ou apenas uma delas foi fornecida, busca todas as vendas.
            sales = saleService.listAllSales();
        }

        // Gera o relatório no formato solicitado e retorna a resposta com o relatório.
        return generateReportWithDateRange(format, sales, startDateTime, endDateTime);
    }


    //rota para listar produto
    @GetMapping("/products")
    public ResponseEntity<byte[]> getProductsReport(
            @RequestParam(required = false, defaultValue = "csv") String format,
            @RequestParam(required = false) Boolean deleted) throws IOException {

        List<ProductDTO> productDTOs;

        List<Product> products;
        if (deleted != null) {
            if (deleted) {
                products = productService.getAllProductsDeleted();
            } else {
                products = productService.getAllProducts();
            }
        } else {
            products = productService.findAllProducts();
        }

        productDTOs = products.stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());

        ByteArrayInputStream report;
        String filename = "relatorio_produtos";

        if ("pdf".equalsIgnoreCase(format)) {
            report = reportProductsService.generateProductsPdfReport(productDTOs);
            filename += ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            return ResponseEntity.ok().headers(headers).body(report.readAllBytes());
        } else if ("csv".equalsIgnoreCase(format)) {
            report = reportProductsService.generateProductsCsvReport(productDTOs, deleted);
            filename += ".csv";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            return ResponseEntity.ok().headers(headers).body(report.readAllBytes());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Formato não suportado".getBytes());
        }
    }

    //Método para gerar e retornar um relatório no formato CSV ou PDF com tratamento de erro
    private ResponseEntity<byte[]> generateReportWithDateRange(String format, List<SaleDTO> sales, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        ByteArrayInputStream reportStream;
        try {
            if ("csv".equalsIgnoreCase(format)) {
                reportStream = reportSalesService.generateSalesCsvReport(sales, startDateTime, endDateTime);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_vendas.csv")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(reportStream.readAllBytes());
            } else if ("pdf".equalsIgnoreCase(format)) {
                reportStream = reportSalesService.generateSalesPdfReport(sales, startDateTime, endDateTime);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_vendas.pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(reportStream.readAllBytes());
            } else {
                return ResponseEntity.badRequest().body("Formato não suportado.".getBytes());
            }
        } catch (IOException e) {
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar o relatório.".getBytes());
        }
    }

}
