package com.example.login_auth_api.services.reports;

import com.example.login_auth_api.dto.ProductDTO;
import com.example.login_auth_api.services.PageNumberHandler;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportProductsService {
    //Método para gerar relatorio CSV dos produtos
    public ByteArrayInputStream generateProductsCsvReport(List<ProductDTO> products, Boolean deleted, Boolean profit) {
        StringBuilder csvData = new StringBuilder();

        // Cabeçalho do CSV
        csvData.append("ID do Produto;Nome;Categoria;Custo;Preço;Quantidade disponivel;Quantidade por tamanho disponivel;Deletado;Quantidade vendida");

        // Adiciona o cabeçalho de lucro se o parâmetro `profit` for verdadeiro
        if (Boolean.TRUE.equals(profit)) {
            csvData.append(";Lucro");
        }
        csvData.append("\n");

        // Adiciona os dados dos produtos
        for (ProductDTO product : products) {
            String tamanhosFormatados = product.getSizes().stream()
                    .map(size -> size.getQuantity() + "(" + size.getSize() + ")")
                    .collect(Collectors.joining(", "));

            csvData.append(product.getId()).append(";");
            csvData.append(product.getName()).append(";");
            csvData.append(product.getCategoryName()).append(";");
            csvData.append(product.getCost()).append(";");
            csvData.append(product.getPrice()).append(";");
            csvData.append(product.getQuantity()).append(";");
            csvData.append(tamanhosFormatados).append(";");
            csvData.append(product.isDeleted() ? "SIM" : "NÃO").append(";");
            csvData.append(product.getQuantitySold()).append(";");

            // Adiciona o lucro, se o parâmetro `profit` for verdadeiro
            if (Boolean.TRUE.equals(profit)) {
                BigDecimal lucro = calculateProfit(product);
                csvData.append(lucro);
            }

            csvData.append("\n");
        }

        csvData.append("\n");
        // Adiciona a linha indicando o estado do filtro 'deleted'
        csvData.append("Filtro 'Deletado' especificado: ").append(deleted != null ? (deleted ? "SIM" : "NÃO") : "Não especificado").append("\n");

        return new ByteArrayInputStream(csvData.toString().getBytes(StandardCharsets.UTF_8));
    }

    //Método para gerar relatorio PDF dos produtos
    public ByteArrayInputStream generateProductsPdfReport(List<ProductDTO> products, Boolean profit) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Configuração do PDF com orientação horizontal
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4.rotate()); // Configura a página A4 em orientação horizontal

        // Cria o documento
        Document document = new Document(pdfDoc);

        // Configuração do título
        Paragraph title = new Paragraph("LABELEJUH BAGS & SHOES")
                .setFontSize(20)
                .setBold();
        // Configuração do subtítulo
        Paragraph subTitle = new Paragraph("Relatório Detalhado de Produtos")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(20);

        // Adicionando título e subtítulo ao documento
        document.add(title);
        document.add(subTitle);

        float[] columnWidths = profit != null && profit ? new float[]{3, 5, 2, 2, 2, 2, 5, 1, 2, 2} : new float[]{3, 5, 2, 2, 2, 2, 5, 1, 2};

        // Criar a tabela com as larguras proporcionais das colunas
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Adicionando cabeçalhos da tabela com estilo e padding
        table.addHeaderCell(createCellWithPadding(new Paragraph("ID do Produto").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Nome do Produto").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Categoria").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Custo").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Preço").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Quantidade Disponível").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Quantidade por Tamanho").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Deletado").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        table.addHeaderCell(createCellWithPadding(new Paragraph("Quantidade vendida").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));

        if (profit != null && profit) {
            table.addHeaderCell(createCellWithPadding(new Paragraph("Lucro").setBold()).setBackgroundColor(new DeviceRgb(200, 200, 200)));
        }

        int totalProdutos = 0;

        // Preenchendo a tabela com os dados dos produtos
        for (ProductDTO product : products) {
            totalProdutos++;

            String tamanhosFormatados = product.getSizes().stream()
                    .map(size -> size.getQuantity() + " (" + size.getSize() + ")")
                    .collect(Collectors.joining(", "));

            table.addCell(createCellWithPadding(new Paragraph(product.getId()).setFontSize(8)));
            table.addCell(createCellWithPadding(new Paragraph(product.getName())));
            table.addCell(createCellWithPadding(new Paragraph(product.getCategoryName())));
            table.addCell(createCellWithPadding(new Paragraph("R$" + product.getCost().toString())));
            table.addCell(createCellWithPadding(new Paragraph("R$" + product.getPrice().toString())));
            table.addCell(createCellWithPadding(new Paragraph(product.getQuantity().toString())));
            table.addCell(createCellWithPadding(new Paragraph(tamanhosFormatados)));
            table.addCell(createCellWithPadding(new Paragraph(product.isDeleted() ? "Sim" : "Não")));
            table.addCell(createCellWithPadding(new Paragraph(product.getQuantitySold().toString())));

            if (profit != null && profit) {
                table.addCell(createCellWithPadding(new Paragraph("R$" + calculateProfit(product))));
            }
        }

        // Adicionando a tabela ao documento
        document.add(table);

        // Adicionando informações gerais no final do documento
        Paragraph infoVendas = new Paragraph("\nInformações Gerais:\n Total de Produtos = " + totalProdutos)
                .setFontSize(14)
                .setBold();
        document.add(infoVendas);

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

    private BigDecimal calculateProfit(ProductDTO product) {
        // Exemplo básico de cálculo do lucro: (preço - custo) * quantidade vendida
        BigDecimal revenue = product.getPrice().subtract(product.getCost());
        BigDecimal profit = revenue.multiply(new BigDecimal(product.getQuantitySold()));
        return profit;
    }
}
