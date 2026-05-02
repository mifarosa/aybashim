# aybashim 💳

> 🚧 **Bu proje aktif geliştirme aşamasındadır.**

Banka ekstrelerini PDF ve Excel formatında yükleyerek harcamalarınızı otomatik olarak analiz eden, kategorilendiren ve veritabanında takip eden kişisel web uygulaması.

---

A personal web application that automatically analyzes, categorizes, and tracks your expenses by parsing bank statements in PDF and Excel formats.

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

## 📌 Mevcut Özellikler / Current Features

- PDF ve Excel ekstre yükleme
- ING kredi kartı ekstresi parse etme
- ING hesap ekstresi parse etme
- Garanti BBVA hesap ekstresi parse etme
- Duplicate kayıt koruması
- İşlemleri PostgreSQL'e kaydetme
- REST API
- Otomatik harcama kategorilendirme
- Ana kategori ve alt kategori desteği
- Kategoriye göre işlem listeleme
- Mevcut işlemleri yeniden kategorilendirme
- Kişinin kendi hesapları arasındaki transferleri ayırt etme

---

## 🧾 Kategorilendirme / Categorization

Uygulama, yüklenen banka işlemlerini açıklama metinlerine göre otomatik olarak kategorilendirir.

Her işlem için iki kategori bilgisi tutulur:

- **Ana kategori / Main Category**
- **Alt kategori / Sub Category**

Örnek kategoriler:

| Ana Kategori | Alt Kategori Örnekleri |
|-------------|-------------------------|
| Gelir | Maaş, Diğer Gelir |
| Gıda | Market, Restoran, Kahve |
| Faturalar | Elektrik, Su, Doğalgaz, İnternet, Telefon |
| Ulaşım | Akaryakıt, Toplu Taşıma, Taksi |
| Alışveriş | Giyim, Elektronik, Online Alışveriş |
| Sağlık | Eczane, Hastane |
| Eğitim | Okul, Kurs, Kitap |
| Abonelik | Dijital Abonelik, Yazılım Aboneliği |
| Transfer | EFT, Havale, FAST, Kendi Hesapları Arası Transfer |
| Banka Masrafları | Banka Ücreti, Faiz |
| Diğer | Bilinmeyen |

Kategori belirleme işlemi keyword tabanlıdır. Örneğin market, abonelik, fatura, ulaşım ve transfer işlemleri açıklama metnine göre otomatik olarak sınıflandırılır.

---

## 🔁 Yeniden Kategorilendirme / Recategorization

Mevcut işlemler sonradan yeniden kategorilendirilebilir.

Bu özellik, kategori kuralları güncellendiğinde daha önce kaydedilmiş işlemlere yeni kuralların uygulanmasını sağlar.

---

## 🔌 API Endpointleri / API Endpoints

Temel transaction endpointlerine ek olarak kategori işlemleri için aşağıdaki endpointler desteklenir:

```http
POST /transactions/recategorize
GET  /transactions/categories/main
GET  /transactions/categories/sub
GET  /transactions/main-category/{mainCategory}
GET  /transactions/sub-category/{subCategory}
```

### Açıklama

| Endpoint | Açıklama |
|---------|----------|
| `POST /transactions/recategorize` | Tüm mevcut işlemleri yeniden kategorilendirir |
| `GET /transactions/categories/main` | Ana kategori listesini döndürür |
| `GET /transactions/categories/sub` | Alt kategori listesini döndürür |
| `GET /transactions/main-category/{mainCategory}` | Ana kategoriye göre işlemleri listeler |
| `GET /transactions/sub-category/{subCategory}` | Alt kategoriye göre işlemleri listeler |

---

## ⚙️ Kurulum / Setup

### Gereksinimler / Requirements

- Java 21+
- Maven 3.8+
- PostgreSQL

### Adımlar / Steps

```bash
# 1. Repoyu klonla
git clone https://github.com/mifarosa/aybashim.git
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

## ⚙️ Konfigürasyon / Configuration

`application.properties` içinde kendi hesaplarınız arasındaki transferlerin ayırt edilmesi için keyword tanımlanabilir:

```properties
app.self-transfer.keywords=Ad Soyad
```

Birden fazla keyword kullanılacaksa virgül ile ayrılabilir:

```properties
app.self-transfer.keywords=Ad Soyad,IBAN,Kullanıcı Adı
```

---

## 🔜 Yakında / Upcoming

- Vue.js arayüzü
- Aylık harcama grafikleri
- Gelir/gider takibi
- Kategori bazlı harcama raporları
- Dashboard ekranı
- Bütçe limiti takibi

---

## 📄 Lisans / License

MIT