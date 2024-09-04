package com.example.login_auth_api.services;

import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

//Método auxiliar para enumerar as paginas dos relatorios em pdf
public class PageNumberHandler implements IEventHandler {
    @Override
    public void handleEvent(Event event) {
        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdf = docEvent.getDocument();
        PdfPage page = docEvent.getPage();

        int pageNumber = pdf.getPageNumber(page);
        int totalPages = pdf.getNumberOfPages();

        String text = String.format("Page %d of %d", pageNumber, totalPages);

        // Obtém as dimensões da página
        float pageWidth = pdf.getDefaultPageSize().getWidth();
        float pageHeight = pdf.getDefaultPageSize().getHeight();

        // Define a posição do texto no canto inferior direito
        float x = pageWidth - 50;  // Margem a partir da borda direita
        float y = 20;             // Margem a partir da borda inferior

        Document document = new Document(pdf);
        Paragraph p = new Paragraph(text)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.BOTTOM);

        document.showTextAligned(p, x, y, pdf.getPageNumber(page), TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);    }
}
