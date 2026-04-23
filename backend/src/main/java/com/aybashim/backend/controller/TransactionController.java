package com.aybashim.backend.controller;

import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.service.ExcelParserService;
import com.aybashim.backend.service.PdfParserService;
import com.aybashim.backend.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;
    private final PdfParserService parserService;
    private final ExcelParserService excelParserService;

    public TransactionController(TransactionService service, PdfParserService parserService, ExcelParserService excelParserService) {
        this.service = service;
        this.parserService = parserService;
        this.excelParserService = excelParserService;
    }

    @GetMapping
    public List<Transaction> getAll() {
        return service.getAll();
    }

    @PostMapping
    public Transaction save(@RequestBody Transaction transaction) {
        return service.save(transaction);
    }

    @PostMapping("/upload")
    public List<Transaction> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bankName") String bankName) throws IOException {

        return parserService.parsePdf(file, bankName);
    }

    @PostMapping("/upload/excel")
    public List<Transaction> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bankName") String bankName) throws IOException {

        return excelParserService.parseExcel(file, bankName);
    }
}