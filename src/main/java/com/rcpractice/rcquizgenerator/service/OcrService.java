package com.rcpractice.rcquizgenerator.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class OcrService {

    public String extractText(MultipartFile imageFile) throws IOException, TesseractException {
        // Save the uploaded image to a temp file (Tesseract needs a File, not a stream)
        File tempFile = File.createTempFile("upload", ".png");
        imageFile.transferTo(tempFile);

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

        String extractedText = tesseract.doOCR(tempFile);

        tempFile.delete(); // clean up

        return extractedText;
    }
}