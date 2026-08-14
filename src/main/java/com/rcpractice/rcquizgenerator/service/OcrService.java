package com.rcpractice.rcquizgenerator.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

@Service
public class OcrService {

    private static final String[] COMMON_WORDS = {
            " the ", " and ", " of ", " to ", " in ", " a ", " is ", " that ", " for ", " on "
    };

    public String extractText(MultipartFile imageFile) throws IOException, TesseractException {
        String originalFilename = imageFile.getOriginalFilename();
        String extension = ".png";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        File tempFile = File.createTempFile("upload", extension);
        imageFile.transferTo(tempFile);

        BufferedImage originalImage = ImageIO.read(tempFile);
        if (originalImage == null) {
            tempFile.delete();
            throw new IOException("Could not read the uploaded image.");
        }

        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

        String bestText = "";
        double bestScore = -1;

        int[] rotations = {0, 90, 180, 270};
        for (int angle : rotations) {
            BufferedImage rotated = rotateImage(originalImage, angle);
            String text = tesseract.doOCR(rotated);
            double score = scoreReadability(text);

            if (score > bestScore) {
                bestScore = score;
                bestText = text;
            }
        }

        tempFile.delete();
        return cleanExtractedText(bestText);   // <-- CHANGED THIS LINE
    }

    private BufferedImage rotateImage(BufferedImage source, int angleDegrees) {
        if (angleDegrees == 0) return source;

        double radians = Math.toRadians(angleDegrees);
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        boolean swapDimensions = (angleDegrees == 90 || angleDegrees == 270);
        int newWidth = swapDimensions ? srcHeight : srcWidth;
        int newHeight = swapDimensions ? srcWidth : srcHeight;

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rotated.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, newWidth, newHeight);

        AffineTransform transform = new AffineTransform();
        transform.translate(newWidth / 2.0, newHeight / 2.0);
        transform.rotate(radians);
        transform.translate(-srcWidth / 2.0, -srcHeight / 2.0);

        g2d.drawImage(source, transform, null);
        g2d.dispose();

        return rotated;
    }

    private double scoreReadability(String text) {
        if (text == null || text.isBlank()) return 0;

        String lower = " " + text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ") + " ";

        int commonWordHits = 0;
        for (String word : COMMON_WORDS) {
            int index = 0;
            while ((index = lower.indexOf(word, index)) != -1) {
                commonWordHits++;
                index += word.length();
            }
        }

        long letterCount = text.chars().filter(Character::isLetter).count();
        double letterRatio = text.isEmpty() ? 0 : (double) letterCount / text.length();

        return (commonWordHits * 10.0) + (letterRatio * 5.0);
    }

    // NEW METHOD ADDED BELOW
    private String cleanExtractedText(String rawText) {
        if (rawText == null || rawText.isBlank()) return rawText;

        String[] lines = rawText.split("\n");
        StringBuilder cleaned = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String[] words = trimmed.split("\\s+");
            if (words.length < 4) continue; // skip very short lines outright

            int validWordCount = 0;
            for (String word : words) {
                // A "valid" word: 2+ letters, mostly alphabetic, no excessive symbols
                String stripped = word.replaceAll("[^a-zA-Z]", "");
                if (stripped.length() >= 2 && (double) stripped.length() / word.length() > 0.7) {
                    validWordCount++;
                }
            }

            double validWordRatio = (double) validWordCount / words.length;

            // Keep the line only if most of its words look like real words
            if (validWordRatio > 0.75) {
                cleaned.append(trimmed).append(" ");
            }
        }

        return cleaned.toString().trim();
    }
}