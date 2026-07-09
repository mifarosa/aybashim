package com.aybashim.backend.dto;

import com.aybashim.backend.model.Transaction;

import java.util.List;

public record TransactionImportResponse(
        int parsedCount,
        int savedCount,
        int duplicateCount,
        List<Transaction> savedTransactions
) {
}
