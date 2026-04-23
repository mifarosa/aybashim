package com.aybashim.backend.service;

import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.parser.GarantiParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ExcelParserService {

    public List<Transaction> parseExcel(MultipartFile file, String bankName) throws IOException {
        return switch (bankName.toUpperCase()) {
            case "GARANTI" -> new GarantiParser().parse(file.getInputStream());
            default -> throw new IllegalArgumentException("Bilinmeyen banka: " + bankName);
        };
    }
}