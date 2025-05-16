package com.nekobyte.englishtek.util;

import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Component
public class PdfConverter {
    
    public byte[] convertMarkdownToPdf(String title, String description, List<String> markdownPages) throws IOException {
        // Create a temporary directory if it doesn't exist
        Path tempDir = Path.of(System.getProperty("java.io.tmpdir"), "englishtek_temp");
        Files.createDirectories(tempDir);
        
        // Create temporary files
        String tempFileName = UUID.randomUUID().toString();
        Path mdPath = tempDir.resolve(tempFileName + ".md");
        Path pdfPath = tempDir.resolve(tempFileName + ".pdf");
        
        try {
            // Create markdown content with title and description
            StringBuilder markdownContent = new StringBuilder();
            markdownContent.append("# ").append(title).append("\n\n");
            if (description != null && !description.isEmpty()) {
                markdownContent.append(description).append("\n\n");
            }
            markdownContent.append("---\n\n"); // Horizontal rule after header
            
            // Add lesson content pages
            markdownContent.append(String.join("\n\n---\n\n", markdownPages));
            
            // Write to file
            Files.writeString(mdPath, markdownContent.toString());
            
            // Convert to PDF
            Document doc = new Document();
            doc.loadFromFile(mdPath.toString());
            doc.saveToFile(pdfPath.toString(), FileFormat.PDF);
            doc.dispose();
            
            // Read the PDF into byte array
            return Files.readAllBytes(pdfPath);
        } finally {
            // Clean up temporary files
            Files.deleteIfExists(mdPath);
            Files.deleteIfExists(pdfPath);
        }
    }
}
