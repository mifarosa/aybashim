package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GarantiParser {

    public List<Transaction> parse(InputStream is, MultipartFile file) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        Workbook workbook = new HSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 15; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell dateCell   = row.getCell(0);
            Cell descCell   = row.getCell(1);
            Cell amountCell = row.getCell(3);

            if (amountCell == null || amountCell.getCellType() != CellType.NUMERIC) continue;

            // Tarih
            LocalDate localDate;
            if (dateCell.getCellType() == CellType.NUMERIC) {
                Date date = dateCell.getDateCellValue();
                localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else if (dateCell.getCellType() == CellType.STRING) {
                localDate = LocalDate.parse(dateCell.getStringCellValue().trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                continue;
            }

            // Açıklama
            String description = descCell != null ? descCell.getStringCellValue() : "";

            // Tutar
            double rawAmount = amountCell.getNumericCellValue();
            BigDecimal amount = BigDecimal.valueOf(Math.abs(rawAmount));
            String type = rawAmount >= 0 ? "CREDIT" : "DEBIT";

            Transaction tx = new Transaction();
            tx.setDate(localDate);
            tx.setDescription(description);
            tx.setAmount(amount);
            tx.setType(type);
            tx.setSourceFile(file.getOriginalFilename());
            tx.setBankName("Garanti");
            transactions.add(tx);
        }

        workbook.close();
        return transactions;
    }
}