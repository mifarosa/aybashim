package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import java.util.List;

public interface BankParser {
    List<Transaction> parse(String text);
}