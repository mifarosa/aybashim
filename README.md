# aybashim 💳

> 🚧 **Bu proje aktif geliştirme aşamasındadır.**

Banka ekstrelerini PDF ve Excel formatında yükleyerek harcamalarınızı otomatik olarak analiz eden ve veritabanında takip eden kişisel web uygulaması.

---

A personal web application that automatically analyzes and tracks your expenses by parsing bank statements in PDF and Excel formats.

---

## 🛠️ Teknolojiler / Tech Stack

**Backend**
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Apache PDFBox
- Apache POI

**Frontend**
- Vue.js

---

## 🏦 Desteklenen Bankalar / Supported Banks

| Banka | Format | Tür | Durum |
|-------|--------|-----|-------|
| ING | PDF | Kredi Kartı Ekstresi | ✅ Aktif |
| ING | PDF | Hesap Ekstresi | ✅ Aktif |
| Garanti BBVA | XLS | Hesap Ekstresi | ✅ Aktif |

---

## ⚙️ Kurulum / Setup

### Gereksinimler / Requirements
- Java 21+
- Maven 3.8+
- PostgreSQL

### Adımlar / Steps

```bash
# 1. Repoyu klonla
git clone https://github.com/kullanici-adin/aybashim.git
cd aybashim

# 2. Veritabanını oluştur
createdb aybashim

# 3. application.properties dosyasını düzenle
# DB_HOST, DB_PORT, DB_USER, DB_PASSWORD

# 4. Backend'i başlat
cd backend
mvn spring-boot:run
```

---

## 📌 Mevcut Özellikler / Current Features

- PDF ve Excel ekstre yükleme
- ING kredi kartı ekstresi parse etme
- ING hesap ekstresi parse etme
- Garanti BBVA hesap ekstresi parse etme
- Duplicate kayıt koruması
- İşlemleri PostgreSQL'e kaydetme
- REST API

## 🔜 Yakında / Upcoming

- Vue.js arayüzü
- Harcama kategorilendirme
- Aylık harcama grafikleri
- Gelir/gider takibi

---

## 📄 Lisans / License

MIT
