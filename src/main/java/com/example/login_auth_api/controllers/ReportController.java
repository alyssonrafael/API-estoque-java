package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.products.Product;
import com.example.login_auth_api.domain.sales.PaymentMethod;
import com.example.login_auth_api.dto.ProductDTO;
import com.example.login_auth_api.dto.SaleDTO;
import com.example.login_auth_api.services.ProductService;
import com.example.login_auth_api.services.reports.ReportNumbersService;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ReportNumbersService reportNumbersService;

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
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(required = false) Boolean profit) throws IOException {

        List<ProductDTO> productDTOs;
        List<Product> products;

        // Determina se busca produtos deletados, não deletados, ou todos
        if (deleted != null) {
            if (deleted) {
                products = productService.getAllProductsDeleted();
            } else {
                products = productService.getAllProducts();
            }
        } else {
            products = productService.findAllProducts();
        }

        // Mapeia os produtos para DTOs
        productDTOs = products.stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());

        ByteArrayInputStream report;
        String filename = "relatorio_produtos";

        // Geração do relatório em PDF
        if ("pdf".equalsIgnoreCase(format)) {
            // Chama o serviço para gerar o relatório PDF, considerando se o lucro deve ser incluído
            report = reportProductsService.generateProductsPdfReport(productDTOs, profit);
            filename += ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            return ResponseEntity.ok().headers(headers).body(report.readAllBytes());

            // Geração do relatório em CSV
        } else if ("csv".equalsIgnoreCase(format)) {
            // Chama o serviço para gerar o relatório CSV, considerando se o lucro deve ser incluído
            report = reportProductsService.generateProductsCsvReport(productDTOs, deleted, profit);
            filename += ".csv";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
            return ResponseEntity.ok().headers(headers).body(report.readAllBytes());

            // Caso o formato seja inválido
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

    //Rotas para relatorio de numeros

    //rota para numero de vendas por metodo de pagamento
    @GetMapping("/sales-by-payment-method")
    public Map<String, Integer> getSalesByPaymentMethod() {
        Map<PaymentMethod, Integer> salesByMethod = reportNumbersService.getSalesByPaymentMethod();

        // Converte o mapa com PaymentMethod como chave para um mapa com String (em maiúsculas) como chave
        return salesByMethod.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> formatPaymentMethod(entry.getKey()), // Chave formatada
                        Map.Entry::getValue // Valor
                ));
    }

    // Endpoint para retornar a quantidade de vendas com isGift = true
    @GetMapping("/gift-sales-count")
    public long getCountOfGiftSales() {
        return reportNumbersService.getCountOfGiftSales();
    }

    //rotas para retornar vendas dia mes e ano
    @GetMapping("/sales-today")
    public long getCountOfSalesToday() {
        return reportNumbersService.getCountOfSalesToday();
    }

    @GetMapping("/sales-this-month")
    public long getCountOfSalesThisMonth() {
        return reportNumbersService.getCountOfSalesThisMonth();
    }

    @GetMapping("/sales-this-year")
    public long getCountOfSalesThisYear() {
        return reportNumbersService.getCountOfSalesThisYear();
    }

    @GetMapping("/sales-last-three-months")
    public Map<String, Integer> getSalesByMonthLastThreeMonths() {
        return reportNumbersService.getSalesByMonthLastThreeMonths();
    }
    //rotas para retorno de valores dia mes ano
    @GetMapping("/total-sales-today")
    public ResponseEntity<BigDecimal> getTotalSalesToday() {
        return ResponseEntity.ok(reportNumbersService.getTotalSalesToday());
    }

    @GetMapping("/total-sales-this-month")
    public ResponseEntity<BigDecimal> getTotalSalesThisMonth() {
        return ResponseEntity.ok(reportNumbersService.getTotalSalesThisMonth());
    }

    @GetMapping("/total-sales-this-year")
    public ResponseEntity<BigDecimal> getTotalSalesThisYear() {
        return ResponseEntity.ok(reportNumbersService.getTotalSalesThisYear());
    }

    @GetMapping("/total-sales-by-month-last-three-months")
    public Map<String, BigDecimal> getTotalSalesByMonthLastThreeMonths() {
        return reportNumbersService.getTotalSalesByMonthLastThreeMonths();
    }

    //rota para rank de categoria mais vendida do dia mes ano
    @GetMapping("/category-top-today")
    public Map<String, Long> getTopCategoriesForToday() {
        return reportNumbersService.getTopCategoriesForToday();
    }
    @GetMapping("/category-top-this-month")
    public Map<String, Long> getTopCategoriesForMonth() {
        return reportNumbersService.getTopCategoriesForMonth();
    }
    @GetMapping("/category-top-this-year")
    public Map<String, Long> getTopCategoriesForYear() {
        return reportNumbersService.getTopCategoriesForYear();
    }

    //Método auxiliar para formatar o retorno do tipo de pagamento
    private String formatPaymentMethod(PaymentMethod method) {
        switch (method) {
            case DINHEIRO: return "DINHEIRO";
            case DEBITO: return "DÉBITO";
            case CREDITO: return "CRÉDITO";
            case PIX: return "PIX";
            case OUTRO: return "OUTRO";
            default: return method.name().toUpperCase(); // Por segurança, se adicionar novos métodos
        }
    }
}
