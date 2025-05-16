package com.nekobyte.englishtek.util;

import com.lowagie.text.DocumentException;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class HtmlPdfConverter {
    
    public byte[] convertHtmlToPdf(String html) throws IOException {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);
            
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Failed to convert HTML to PDF", e);
        }
    }
}
