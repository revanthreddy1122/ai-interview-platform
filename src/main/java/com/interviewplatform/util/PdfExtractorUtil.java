package com.interviewplatform.util;

import com.interviewplatform.exception.FileProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
public class PdfExtractorUtil {

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;

    public String extractText(MultipartFile file) {
        validateFile(file);

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            if (document.isEncrypted()) {
                throw new FileProcessingException("Cannot process an encrypted/password-protected PDF");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new FileProcessingException(
                        "No readable text found in the uploaded PDF. The file may be a scanned image.");
            }

            return text.trim();
        } catch (IOException ex) {
            log.error("Failed to extract text from PDF: {}", ex.getMessage(), ex);
            throw new FileProcessingException("Failed to read the uploaded PDF file: " + ex.getMessage(), ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("Uploaded file is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new FileProcessingException("Only PDF files are supported");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("application/pdf")) {
            throw new FileProcessingException("Invalid file type. Only PDF files are supported");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileProcessingException("File size exceeds the maximum limit of 10MB");
        }
    }
}
