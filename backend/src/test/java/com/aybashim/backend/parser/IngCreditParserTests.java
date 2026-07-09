package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IngCreditParserTests {

    @Test
    void parsesDebitAndCreditLines() {
        String text = """
                01/05/2026 MIGROS MARKET 123.45
                02/05/2026 IADE ISLEMI 10.00 +
                """;

        List<Transaction> transactions = new IngCreditParser().parse(text, file());

        assertThat(transactions).hasSize(2);
        assertThat(transactions.get(0).getDescription()).isEqualTo("MIGROS MARKET");
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("123.45"));
        assertThat(transactions.get(0).getType()).isEqualTo("DEBIT");
        assertThat(transactions.get(1).getType()).isEqualTo("CREDIT");
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "ing.pdf", "application/pdf", new byte[0]);
    }
}
