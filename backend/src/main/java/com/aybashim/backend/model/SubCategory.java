package com.aybashim.backend.model;

public enum SubCategory {

    SALARY("Maaş", MainCategory.INCOME),
    EXTRA_INCOME("Ek Gelir", MainCategory.INCOME),

    RENT("Kira", MainCategory.HOUSING),
    APARTMENT_FEE("Aidat", MainCategory.HOUSING),

    INTERNET("İnternet", MainCategory.BILLS),
    MOBILE_PHONE("Mobil Hat", MainCategory.BILLS),
    ELECTRICITY("Elektrik", MainCategory.BILLS),
    WATER("Su", MainCategory.BILLS),
    NATURAL_GAS("Doğalgaz", MainCategory.BILLS),

    MARKET("Market", MainCategory.FOOD),
    RESTAURANT("Restoran", MainCategory.FOOD),
    COFFEE("Kafe", MainCategory.FOOD),

    E_COMMERCE("E-Ticaret", MainCategory.SHOPPING),
    CLOTHING("Giyim", MainCategory.SHOPPING),
    ELECTRONICS("Elektronik", MainCategory.SHOPPING),

    PUBLIC_TRANSPORT("Toplu Taşıma", MainCategory.TRANSPORTATION),
    TAXI("Taksi", MainCategory.TRANSPORTATION),
    FUEL("Yakıt", MainCategory.TRANSPORTATION),
    FLIGHT("Uçak", MainCategory.TRANSPORTATION),
    TRAVEL("Seyahat", MainCategory.TRANSPORTATION),

    PHARMACY("Eczane", MainCategory.HEALTH),
    HOSPITAL("Hastane", MainCategory.HEALTH),

    ONLINE_COURSE("Online Kurs", MainCategory.EDUCATION),
    BOOK("Kitap", MainCategory.EDUCATION),

    DIGITAL_SUBSCRIPTION("Dijital Abonelik", MainCategory.SUBSCRIPTION),

    GOLD("Altın", MainCategory.INVESTMENT),
    SILVER("Gümüş", MainCategory.INVESTMENT),
    FOREIGN_CURRENCY("Döviz", MainCategory.INVESTMENT),
    STOCK_FUND("Hisse/Fon", MainCategory.INVESTMENT),

    MONEY_SENT("Gönderilen Para", MainCategory.TRANSFER),
    MONEY_RECEIVED("Gelen Para", MainCategory.TRANSFER),
    DEBT_PAYMENT("Borç Ödeme", MainCategory.TRANSFER),
    SELF_TRANSFER("Kendime Transfer", MainCategory.TRANSFER),

    ATM_WITHDRAWAL("ATM Para Çekme", MainCategory.CASH),
    ATM_DEPOSIT("ATM Para Yatırma", MainCategory.CASH),

    BANK_FEE("Banka Kesintisi", MainCategory.BANK_FEES),

    UNKNOWN("Bilinmeyen", MainCategory.OTHER);

    private final String displayName;
    private final MainCategory mainCategory;

    SubCategory(String displayName, MainCategory mainCategory) {
        this.displayName = displayName;
        this.mainCategory = mainCategory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MainCategory getMainCategory() {
        return mainCategory;
    }
}
