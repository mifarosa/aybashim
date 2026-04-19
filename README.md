# aybashim 💳

> 🚧 **Bu proje aktif geliştirme aşamasındadır.**

Banka kredi kartı ekstrelerini PDF formatında yükleyerek harcamalarınızı otomatik olarak analiz eden ve veritabanında takip eden web uygulaması.

---

A web application that automatically analyzes and tracks your expenses by parsing bank credit card statements in PDF format.

---

## 🛠️ Teknolojiler / Tech Stack

**Backend**
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Apache PDFBox

**Frontend**
- Vue.js

---

## 🏦 Desteklenen Bankalar / Supported Banks

| Banka | Durum |
|-------|-------|
| ING   | ✅ Aktif |

Yeni banka desteği yakında eklenecektir.

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

- PDF ekstre yükleme
- ING kredi kartı ekstresi parse etme
- İşlemleri PostgreSQL'e kaydetme
- REST API

## 🔜 Yakında / Upcoming

- Vue.js arayüzü
- Çoklu banka desteği
- Harcama kategorilendirme
- Aylık harcama grafikleri
- Aylık maaş ekleme ile gelir gider takibi

---

## 📄 Lisans / License

MIT
