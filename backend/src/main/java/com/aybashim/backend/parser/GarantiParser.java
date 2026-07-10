package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    private static final int FIRST_TRANSACTION_ROW = 15;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public List<Transaction> parse(InputStream is, MultipartFile file) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (Workbook workbook = new HSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = FIRST_TRANSACTION_ROW; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Transaction transaction = parseRow(row, file);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    private Transaction parseRow(Row row, MultipartFile file) {
        Cell dateCell = row.getCell(0);
        Cell descriptionCell = row.getCell(1);
        Cell amountCell = row.getCell(3);

        if (dateCell == null || amountCell == null || amountCell.getCellType() != CellType.NUMERIC) {
            return null;
        }

        LocalDate date = parseDate(dateCell);
        if (date == null) {
            return null;
        }

        double rawAmount = amountCell.getNumericCellValue();

        Transaction transaction = new Transaction();
        transaction.setDate(date);
        transaction.setDescription(descriptionCell != null ? descriptionCell.getStringCellValue() : "");
        transaction.setAmount(BigDecimal.valueOf(Math.abs(rawAmount)));
        transaction.setType(rawAmount >= 0 ? "CREDIT" : "DEBIT");
        transaction.setSourceFile(file.getOriginalFilename());
        transaction.setBankName("Garanti");
        return transaction;
    }

    private LocalDate parseDate(Cell dateCell) {
        if (dateCell.getCellType() == CellType.NUMERIC) {
            Date date = dateCell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (dateCell.getCellType() == CellType.STRING) {
            return LocalDate.parse(dateCell.getStringCellValue().trim(), DATE_FORMATTER);
        }
        return null;
    }
}
