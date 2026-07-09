package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IngAccountParserTests {

    @Test
    void parsesMultilineAccountTransactions() {
        String text = """
                01.05.2026 MARKET ALISVERIS
                ISTANBUL -125.50 1000.00
                02.05.2026 GELEN FAST 250.00 1250.00
                """;

        List<Transaction> transactions = new IngAccountParser().parse(text, file());

        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getDescription()).isEqualTo("MARKET ALISVERIS ISTANBUL");
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("125.50"));
        assertThat(transactions.get(0).getType()).isEqualTo("DEBIT");
        assertThat(transactions.get(1).getType()).isEqualTo("CREDIT");
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "ing-account.pdf", "application/pdf", new byte[0]);
    }
}
