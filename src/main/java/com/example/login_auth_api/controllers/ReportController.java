package com.example.login_auth_api.controllers;

import com.example.login_auth_api.dto.SaleDTO;
import com.example.login_auth_api.repositories.ProductRepository;
import com.example.login_auth_api.services.ProductService;
import com.example.login_auth_api.services.ReportService;
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

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private SaleService saleService; // Serviço que busca as vendas do banco de dados

    @Autowired
    private ProductService productService; // Serviço que retorna os dados dos produtos
    @Autowired
    private ProductRepository productRepository;

    //Rota para listar todas as vendas ou vendas por data no formato PDF ou CSV
    @GetMapping("/sales")
    public ResponseEntity<byte[]> getSalesReport(
            @RequestParam String format,  // Parâmetro obrigatório que especifica o formato do relatório (csv ou pdf).
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

    @GetMapping("/products")




    //Método para gerar e retornar um relatório no formato CSV ou PDF com tratamento de erro
    private ResponseEntity<byte[]> generateReportWithDateRange(String format, List<SaleDTO> sales, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        ByteArrayInputStream reportStream;
        try {
            if ("csv".equalsIgnoreCase(format)) {
                reportStream = reportService.generateSalesCsvReport(sales, startDateTime, endDateTime);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_vendas.csv")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(reportStream.readAllBytes());
            } else if ("pdf".equalsIgnoreCase(format)) {
                reportStream = reportService.generateSalesPdfReport(sales, startDateTime, endDateTime);
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
