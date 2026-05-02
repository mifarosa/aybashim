package com.aybashim.backend.controller;

import com.aybashim.backend.model.MainCategory;
import com.aybashim.backend.model.SubCategory;
import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.service.ExcelParserService;
import com.aybashim.backend.service.PdfParserService;
import com.aybashim.backend.service.TransactionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
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

    @PostMapping("/recategorize")
    public List<Transaction> recategorizeAll() {
        return service.recategorizeAll();
    }

    @GetMapping("/bank/{bankName}")
    public List<Transaction> getByBank(@PathVariable String bankName) {
        return service.getByBank(bankName);
    }

    @GetMapping("/type/{type}")
    public List<Transaction> getByType(@PathVariable String type) {
        return service.getByType(type);
    }

    @GetMapping("/categories/main")
    public MainCategory[] getMainCategories() {
        return MainCategory.values();
    }

    @GetMapping("/categories/sub")
    public List<Map<String, String>> getSubCategories() {
        return Arrays.stream(SubCategory.values())
                .map(subCategory -> Map.of(
                        "code", subCategory.name(),
                        "displayName", subCategory.getDisplayName(),
                        "mainCategory", subCategory.getMainCategory().name()
                ))
                .toList();
    }

    @GetMapping("/main-category/{mainCategory}")
    public List<Transaction> getByMainCategory(@PathVariable MainCategory mainCategory) {
        return service.getByMainCategory(mainCategory);
    }

    @GetMapping("/sub-category/{subCategory}")
    public List<Transaction> getBySubCategory(@PathVariable SubCategory subCategory) {
        return service.getBySubCategory(subCategory);
    }

    @GetMapping("/date")
    public List<Transaction> getByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return service.getByDateRange(start, end);
    }

    @GetMapping("/month/{month}/main-category/{mainCategory}")
    public List<Transaction> getByMonthAndMainCategory(
            @PathVariable String month,
            @PathVariable MainCategory mainCategory) {
        return service.getByMonthAndMainCategory(YearMonth.parse(month), mainCategory);
    }

    @GetMapping("/month/{month}/sub-category/{subCategory}")
    public List<Transaction> getByMonthAndSubCategory(
            @PathVariable String month,
            @PathVariable SubCategory subCategory) {
        return service.getByMonthAndSubCategory(YearMonth.parse(month), subCategory);
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

    @GetMapping("/summary/monthly/main-category")
    public Map<String, Map<String, BigDecimal>> getMonthlyMainCategorySummary() {
        return service.getMonthlyMainCategorySummary();
    }

    @GetMapping("/summary/monthly/sub-category")
    public Map<String, Map<String, BigDecimal>> getMonthlySubCategorySummary() {
        return service.getMonthlySubCategorySummary();
    }
}
