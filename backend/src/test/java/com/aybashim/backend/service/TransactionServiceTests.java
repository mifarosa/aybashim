package com.aybashim.backend.service;

import com.aybashim.backend.model.AppUser;
import com.aybashim.backend.model.Transaction;
import com.aybashim.backend.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTests {

    private final TransactionRepository repository = mock(TransactionRepository.class);
    private final CategoryClassifier categoryClassifier = mock(CategoryClassifier.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final TransactionService transactionService = new TransactionService(repository, categoryClassifier, currentUser);

    @Test
    void saveNormalizesTransactionTypeAndDescriptionBeforePersisting() {
        AppUser user = user();
        Transaction transaction = validTransaction();
        transaction.setDescription("  Market  ");
        transaction.setType(" debit ");

        when(currentUser.get()).thenReturn(user);
        when(repository.save(transaction)).thenReturn(transaction);

        Transaction saved = transactionService.save(transaction);

        assertThat(saved.getDescription()).isEqualTo("Market");
        assertThat(saved.getType()).isEqualTo("DEBIT");
        assertThat(saved.getUser()).isEqualTo(user);
        verify(categoryClassifier).categorize(transaction, user.getName());
    }

    @Test
    void saveRejectsInvalidTransactionBeforePersisting() {
        Transaction transaction = validTransaction();
        transaction.setAmount(BigDecimal.valueOf(-1));

        assertThatThrownBy(() -> transactionService.save(transaction))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");

        verify(repository, never()).save(transaction);
    }

    private Transaction validTransaction() {
        Transaction transaction = new Transaction();
        transaction.setDate(LocalDate.of(2026, 7, 10));
        transaction.setDescription("Market");
        transaction.setAmount(BigDecimal.TEN);
        transaction.setType("DEBIT");
        transaction.setBankName("ING");
        return transaction;
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setName("Sample User");
        user.setEmail("sample@example.com");
        user.setPasswordHash("hash");
        return user;
    }
}
