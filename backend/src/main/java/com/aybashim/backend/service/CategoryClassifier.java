package com.aybashim.backend.service;

import com.aybashim.backend.model.SubCategory;
import com.aybashim.backend.model.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryClassifier {

    @Value("${app.self-transfer.keywords:Mehmet Faruk G\u00fcl}")
    private String selfTransferKeywords;

    private record Rule(SubCategory subCategory, List<String> keywords) {
    }

    private static final List<Rule> RULES = List.of(
            new Rule(SubCategory.ATM_WITHDRAWAL, List.of("atm para cekme", "atm cekim", "nakit cekim")),
            new Rule(SubCategory.ATM_DEPOSIT, List.of("atm para yatirma", "para yatirma")),
            new Rule(SubCategory.BANK_FEE, List.of("komisyon", "ucret", "kesinti", "masraf")),
            new Rule(SubCategory.PUBLIC_TRANSPORT, List.of("istanbulkart", "belbim", "ulasim", "metro", "otobus")),
            new Rule(SubCategory.TAXI, List.of("taksi", "bitaksi", "uber")),
            new Rule(SubCategory.GOLD, List.of(" alt:", "altin")),
            new Rule(SubCategory.SILVER, List.of(" gms:", "gumus alis", "gumus satis")),
            new Rule(SubCategory.FOREIGN_CURRENCY, List.of("doviz", "usd", "eur", "dolar", "euro")),
            new Rule(SubCategory.INTERNET, List.of("internet", "turknet", "superonline", "kablonet", "ttnet")),
            new Rule(SubCategory.MOBILE_PHONE, List.of("turkcell", "vodafone", "turk telekom")),
            new Rule(SubCategory.ELECTRICITY, List.of("elektrik", "bedas", "aedas", "aesas", "enerjisa")),
            new Rule(SubCategory.WATER, List.of("su faturasi", "iski", "aski")),
            new Rule(SubCategory.NATURAL_GAS, List.of("dogalgaz", "igdas", "gazdas")),
            new Rule(SubCategory.RENT, List.of("kira")),
            new Rule(SubCategory.APARTMENT_FEE, List.of("aidat")),
            new Rule(SubCategory.MARKET, List.of("market", "migros", "sok", "bim ", "bim-", "a101", "carrefour", "hakmar", "mopas", "manav", "tarim k", "ennova", "madencilik")),
            new Rule(SubCategory.RESTAURANT, List.of("restoran", "yemeksepeti", "getir yemek", "trendyol yemek", "doner", "firin", "gida", "beltur", "hd kadikoy")),
            new Rule(SubCategory.COFFEE, List.of("kahve", "kahvecisi", "kafe", "cafe", "starbucks", "coffee")),
            new Rule(SubCategory.E_COMMERCE, List.of("trendyol", "hepsiburada", "amazon", "n11", "e ticaret")),
            new Rule(SubCategory.CLOTHING, List.of("giyim", "lc waikiki", "lcwaikiki", "defacto", "zara", "mavi")),
            new Rule(SubCategory.ELECTRONICS, List.of("teknosa", "mediamarkt", "vatan", "elektronik")),
            new Rule(SubCategory.FUEL, List.of("benzin", "akaryakit", "petrol", "shell", "shel ", "opet", "bp")),
            new Rule(SubCategory.FLIGHT, List.of("thy", "pegasus", "ucak", "flight")),
            new Rule(SubCategory.TRAVEL, List.of("otel", "booking", "seyahat")),
            new Rule(SubCategory.PHARMACY, List.of("eczane", " ecz ")),
            new Rule(SubCategory.HOSPITAL, List.of("hastane", "medical", "saglik", "tip merk", "veteriner")),
            new Rule(SubCategory.ONLINE_COURSE, List.of("udemy", "coursera", "kurs")),
            new Rule(SubCategory.BOOK, List.of("kitap", "kirtasiye", "kitapyurdu", "dr.com.tr")),
            new Rule(SubCategory.DIGITAL_SUBSCRIPTION, List.of("netflix", "spotify", "youtube", "youtu", "apple.com", "ssportplus", "abonelik")),
            new Rule(SubCategory.STOCK_FUND, List.of("hisse", "fon", "borsa", "emeklilik"))
    );

    public void categorize(Transaction transaction) {
        if (transaction.getSubCategory() == null) {
            transaction.setSubCategory(classify(transaction));
        }

        transaction.setMainCategory(transaction.getSubCategory().getMainCategory());
    }

    public void recategorize(Transaction transaction) {
        transaction.setSubCategory(classify(transaction));
        transaction.setMainCategory(transaction.getSubCategory().getMainCategory());
    }

    private SubCategory classify(Transaction transaction) {
        String description = normalize(transaction.getDescription());

        if (description.contains("maas") || description.contains("salary")) {
            return SubCategory.SALARY;
        }

        if (description.contains("yatirilan tesekkurler")) {
            return SubCategory.DEBT_PAYMENT;
        }

        if (description.contains("aidat")) {
            return SubCategory.APARTMENT_FEE;
        }

        if (description.contains("kira")) {
            return SubCategory.RENT;
        }

        if (description.contains("altin karsiligi")) {
            return SubCategory.GIFT;
        }

        if (isTransferDescription(description) && containsSelfTransferKeyword(description)) {
            return SubCategory.SELF_TRANSFER;
        }

        if (description.contains("gelen eft")
                || description.contains("gelen havale")
                || description.contains("gelen fast")) {
            return SubCategory.MONEY_RECEIVED;
        }

        if (isTransferDescription(description)) {
            return "CREDIT".equalsIgnoreCase(transaction.getType())
                    ? SubCategory.MONEY_RECEIVED
                    : SubCategory.MONEY_SENT;
        }

        for (Rule rule : RULES) {
            if (rule.keywords().stream().anyMatch(description::contains)) {
                return rule.subCategory();
            }
        }

        if ("CREDIT".equalsIgnoreCase(transaction.getType())
                && transaction.getAmount() != null
                && transaction.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            return SubCategory.EXTRA_INCOME;
        }

        return SubCategory.UNKNOWN;
    }

    private boolean isTransferDescription(String description) {
        return description.contains("eft")
                || description.contains("havale")
                || description.contains("hvl")
                || description.contains("fast")
                || description.contains("para gonder");
    }

    private boolean containsSelfTransferKeyword(String description) {
        if (selfTransferKeywords == null || selfTransferKeywords.isBlank()) {
            return false;
        }

        return List.of(selfTransferKeywords.split(",")).stream()
                .map(this::normalize)
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .anyMatch(description::contains);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized
                .replace('\u0131', 'i')
                .replace('\u0130', 'i')
                .toLowerCase(Locale.ROOT);
    }
}
