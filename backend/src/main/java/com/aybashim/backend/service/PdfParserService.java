package com.aybashim.backend.service;

import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.parser.BankParser;
import com.aybashim.backend.parser.BankParserFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class PdfParserService {

    public List<Transaction> parsePdf(MultipartFile file, String bankName) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            BankParser parser = BankParserFactory.getParser(bankName);
            return parser.parse(text, file);
        }
    }
}
