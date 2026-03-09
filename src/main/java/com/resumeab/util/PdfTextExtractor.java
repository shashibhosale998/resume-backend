package com.resumeab.util;



import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfTextExtractor {

    public static String extractText(InputStream inputStream) throws Exception {

        PDDocument document = PDDocument.load(inputStream);

        PDFTextStripper pdfStripper = new PDFTextStripper();

        String text = pdfStripper.getText(document);

        document.close();

        return text;
    }
}