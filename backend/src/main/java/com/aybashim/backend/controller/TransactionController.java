package com.aybashim.backend.controller;

import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.service.ExcelParserService;
import com.aybashim.backend.service.PdfParserService;
import com.aybashim.backend.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

        List<Transaction> transactions = parserService.parsePdf(file, bankName);
        return service.saveAll(transactions);
    }

    @PostMapping("/upload/excel")
    public List<Transaction> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bankName") String bankName) throws IOException {

        List<Transaction> transactions = excelParserService.parseExcel(file, bankName);
        return service.saveAll(transactions);
    }

    @GetMapping("/bank/{bankName}")
    public List<Transaction> getByBank(@PathVariable String bankName) {
        return service.getByBank(bankName);
    }

    @GetMapping("/type/{type}")
    public List<Transaction> getByType(@PathVariable String type) {
        return service.getByType(type);
    }

    @GetMapping("/date")
    public List<Transaction> getByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return service.getByDateRange(start, end);
    }

    @GetMapping("/bank/{bankName}/type/{type}")
    public List<Transaction> getByBankAndType(
            @PathVariable String bankName,
            @PathVariable String type) {
        return service.getByBankAndType(bankName, type);
    }

    @GetMapping("/search")
    public List<Transaction> getByDescription(@RequestParam String keyword) {
        return service.getByDescription(keyword);
    }

    @GetMapping("/summary/monthly")
    public Map<String, Map<String, BigDecimal>> getMonthlySummary() {
        return service.getMonthlySummary();
    }
}