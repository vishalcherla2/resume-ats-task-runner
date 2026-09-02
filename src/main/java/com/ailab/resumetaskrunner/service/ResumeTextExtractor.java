package com.ailab.resumetaskrunner.service;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class ResumeTextExtractor {

    public String extractText(byte[] fileBytes) throws IOException {

        try (PDDocument document = Loader.loadPDF(fileBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);
        }
    }
}