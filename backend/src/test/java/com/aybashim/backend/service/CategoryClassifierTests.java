package com.aybashim.backend.service;

import com.aybashim.backend.model.MainCategory;
import com.aybashim.backend.model.SubCategory;
import com.aybashim.backend.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryClassifierTests {

    private final CategoryClassifier classifier = new CategoryClassifier();

    @Test
    void categorizesMarketDescriptionsWithTurkishCharacters() {
        Transaction transaction = transaction("MİGROS MARKET", "DEBIT");

        classifier.categorize(transaction);

        assertThat(transaction.getSubCategory()).isEqualTo(SubCategory.MARKET);
        assertThat(transaction.getMainCategory()).isEqualTo(MainCategory.FOOD);
    }

    @Test
    void categorizesSelfTransfersWhenUserFullNameMatches() {
        Transaction transaction = transaction("Gelen EFT Sample User", "CREDIT");

        classifier.categorize(transaction, "Sample User");

        assertThat(transaction.getSubCategory()).isEqualTo(SubCategory.SELF_TRANSFER);
        assertThat(transaction.getMainCategory()).isEqualTo(MainCategory.TRANSFER);
    }

    private Transaction transaction(String description, String type) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setType(type);
        transaction.setAmount(BigDecimal.TEN);
        return transaction;
    }
}
