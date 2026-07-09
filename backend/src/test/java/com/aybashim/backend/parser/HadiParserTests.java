package com.aybashim.backend.parser;

import com.aybashim.backend.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HadiParserTests {

    @Test
    void parsesHadiCardStatementLines() {
        String text = """
                İşlem Tarihi İşlemler Tutar (TL) Kalan Tutar/Taksit
                18/06/2026 A101*H121*NAECICEGI*ATAISTANBUL TR 538.00
                24/06/2026 ODEME ICIN TESEKKURLER +15,000.00
                03/07/2026 MIGROS*150226*USKUDAR ISTANBUL TR 182.80
                Toplam 391.93
                """;

        List<Transaction> transactions = new HadiParser().parse(text, file());

        assertThat(transactions).hasSize(3);
        assertThat(transactions.get(0).getDescription()).isEqualTo("A101*H121*NAECICEGI*ATAISTANBUL TR");
        assertThat(transactions.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("538.00"));
        assertThat(transactions.get(0).getType()).isEqualTo("DEBIT");
        assertThat(transactions.get(0).getBankName()).isEqualTo("Hadi");
        assertThat(transactions.get(1).getAmount()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(transactions.get(1).getType()).isEqualTo("CREDIT");
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "hadi.pdf", "application/pdf", new byte[0]);
    }
}
