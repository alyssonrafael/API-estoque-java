package com.example.login_auth_api.services;

import com.example.login_auth_api.domain.products.Product;
import com.example.login_auth_api.dto.ProductDTO;
import com.example.login_auth_api.dto.SaleDTO;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    // Método para gerar o relatório em CSV das vendas
    public ByteArrayInputStream generateSalesCsvReport(List<SaleDTO> sales, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        StringBuilder csvData = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Cabeçalho do CSV
        csvData.append("ID da venda;Data;Vendedor;Produtos;Quantidade de tamanhos;Valor da venda;Valor do desconto;Subtotal da venda;Metodo de pagamento;Presente;Observação\n");

        // Adiciona os dados das vendas
        for (SaleDTO sale : sales) {
            // Formata os nomes dos produtos e quantidades com tamanhos
            String produtos = sale.getItems().stream()
                    .map(item -> item.getProductName() + " tam. (" + item.getSizeName() + ")")
                    .collect(Collectors.joining(", "));

            String quantidades = sale.getItems().stream()
                    .map(item -> String.valueOf(item.getQuantity()) + " (" + item.getSizeName() + ")")
                    .collect(Collectors.joining(", "));

            // Adiciona as informações da venda ao CSV
            csvData.append(sale.getId()).append(";");
            csvData.append(sale.getSaleDate().format(formatter)).append(";");
            csvData.append(sale.getUserName()).append(";");
            csvData.append(produtos).append(";");
            csvData.append(quantidades).append(";");
            csvData.append(sale.getTotalAmount()).append(";");
            csvData.append(sale.getDiscount()).append(";");
            csvData.append(sale.getSubtotal()).append(";");
            csvData.append(sale.getPaymentMethod()).append(";");
            csvData.append(sale.isIsGift() ? "SIM" : "NÃO").append(";");
            csvData.append(sale.getObservation()).append(";");
            csvData.append("\n");
        }

        // Adiciona as datas de pesquisa ao CSV
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (startDateTime != null && endDateTime != null) {
            csvData.append("\nPeríodo de Pesquisa: ")
                    .append(startDateTime.format(dateFormatter))
                    .append(" a ")
                    .append(endDateTime.format(dateFormatter))
                    .append(";");
        } else {
            csvData.append("\nPeríodo de Pesquisa: Não especificado todas as vendas listadas;");
        }

        return new ByteArrayInputStream(csvData.toString().getBytes());
    }

    // Método para gerar o relatório em PDF das vendas
    public ByteArrayInputStream generateSalesPdfReport(List<SaleDTO> sales, LocalDateTime startDateTime, LocalDateTime endDateTime) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Configuração do PDF com orientação horizontal
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4.rotate()); // Configura a página A4 em orientação horizontal

        // Cria o documento
        Document document = new Document(pdfDoc);

        // Adiciona a data do intervalo pesquisado no canto superior direito, se disponível
        if (startDateTime != null && endDateTime != null) {
            addDateRangeHeader(document, startDateTime, endDateTime);
        }

        // Configuração do título
        Paragraph title = new Paragraph("LABELEJUH " + "BAGS & SHOES")
                .setFontSize(20)
                .setBold();
        // Configuração do subtítulo
        Paragraph subTitle = new Paragraph("Relatório Detalhado de Vendas")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(20);

        // Adicionando título e subtítulo ao documento
        document.add(title);
        document.add(subTitle);

        // Criando uma tabela com colunas distribuídas de forma proporcional
        Table table = new Table(new float[]{1, 2, 2, 5, 1.5f, 2, 2, 2, 1});
        table.setWidth(UnitValue.createPercentValue(100));

        // Adicionando cabeçalhos da tabela com estilo e padding
        table.addHeaderCell(createCellWithPadding(new Paragraph("ID da venda").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Data").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Vendedor").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Produtos").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Quantidade de tamanhos").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Valor da venda").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Valor do desconto").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Subtotal da venda").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Método de pagamento").setBold()).setBackgroundColor(new DeviceRgb(Color.GRAY)));

        // Definindo o formato de data para o padrão brasileiro
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Variáveis para armazenar os totais
        int totalVendas = 0;
        int totalProdutosVendidos = 0;
        BigDecimal valorTotalVendas = BigDecimal.ZERO;

        // Preenchendo a tabela com os dados das vendas
        for (SaleDTO sale : sales) {
            totalVendas++; // Incrementa o contador de vendas

            // Concatenando produtos, tamanhos e quantidades
            String produtos = sale.getItems().stream()
                    .map(item -> item.getProductName() + " tam. (" + item.getSizeName() + ")")
                    .collect(Collectors.joining(", "));

            String quantidades = sale.getItems().stream()
                    .map(item -> String.valueOf(item.getQuantity()) + " (" + item.getSizeName() + ")")
                    .collect(Collectors.joining(", "));

            // Calculando o total de produtos vendidos e o valor total das vendas
            totalProdutosVendidos += sale.getItems().stream().mapToInt(SaleDTO.SaleItemDTO::getQuantity).sum();
            valorTotalVendas = valorTotalVendas.add(sale.getTotalAmount());

            // Adicionando uma única linha por venda com os produtos e quantidades concatenados
            table.addCell(createCellWithPadding(new Paragraph(sale.getId()).setFontSize(8)));
            table.addCell(createCellWithPadding(new Paragraph(sale.getSaleDate().toLocalDate().format(formatter))));
            table.addCell(createCellWithPadding(new Paragraph(sale.getUserName())));
            table.addCell(createCellWithPadding(new Paragraph(produtos)));
            table.addCell(createCellWithPadding(new Paragraph(quantidades)));
            table.addCell(createCellWithPadding(new Paragraph("R$" + sale.getTotalAmount().toString())));
            table.addCell(createCellWithPadding(new Paragraph("R$" + sale.getDiscount().toString())));
            table.addCell(createCellWithPadding(new Paragraph("R$" + sale.getSubtotal().toString())));
            table.addCell(createCellWithPadding(new Paragraph(sale.getPaymentMethod().toString())));
        }

        // Adicionando a tabela ao documento
        document.add(table);

        // Adicionando informações gerais no final do documento
        Paragraph totalInfo = new Paragraph("\nInformações Gerais")
                .setFontSize(14)
                .setBold();

        Paragraph vendasInfo = new Paragraph("Total de Vendas: " + totalVendas)
                .setFontSize(12);

        Paragraph produtosInfo = new Paragraph("Quantidade de Produtos Vendidos: " + totalProdutosVendidos)
                .setFontSize(12);

        Paragraph valorInfo = new Paragraph("Valor Total de Vendas: R$" + valorTotalVendas.toString())
                .setFontSize(12);

        // Adicionando os parágrafos ao documento
        document.add(totalInfo);
        document.add(vendasInfo);
        document.add(produtosInfo);
        document.add(valorInfo);

        // Adicionando números de página
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, new PageNumberHandler());

        // Fechando o documento
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Método auxiliar para criar células com padding
    private Cell createCellWithPadding(Paragraph content) {
        return new Cell().add(content).setPadding(2);
    }

    //Método auxiliar para colocar a data de pesquisa no pdf
    private void addDateRangeHeader(Document document, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Obtém as dimensões da página
        float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth();
        float pageHeight = document.getPdfDocument().getDefaultPageSize().getHeight();

        // Define o texto com o intervalo de datas
        String text = "Período: " +
                startDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                " a " +
                endDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Define a posição do texto no canto superior direito
        float x = pageWidth - 50;  // Margem a partir da borda direita
        float y = pageHeight - 30; // Margem a partir da borda superior

        // Cria o parágrafo com o texto
        Paragraph p = new Paragraph(text)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT);

        // Adiciona o texto ao documento
        document.add(p);
    }
}
