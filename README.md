# aybashim

> Bu proje aktif geliştirme aşamasındadır.

Kişisel finans takibi için geliştirilen bir backend uygulaması. Banka ekstrelerini PDF veya Excel formatında yükler, işlemleri parse eder, otomatik kategorilendirir ve kullanıcı bazlı olarak PostgreSQL veritabanında saklar.

## Tech Stack

**Backend**
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Apache PDFBox
- Apache POI

**Frontend**
- Vue.js
- Vite

## Desteklenen Ekstreler

| Banka | Format | Tür | Upload Değeri |
| --- | --- | --- | --- |
| ING | PDF | Kredi kartı ekstresi | `ING_CREDIT` |
| ING | PDF | Hesap ekstresi | `ING_ACCOUNT` |
| A101 Hadi | PDF | Kart ekstresi | `HADI` |
| Garanti BBVA | XLS | Hesap ekstresi | `GARANTI` |

## Mevcut Özellikler

- Kullanıcı kayıt ve giriş API'si
- Bearer token ile `/api/**` endpoint koruması
- PDF ve Excel ekstre yükleme
- ING kredi kartı ve hesap ekstresi parse etme
- Garanti BBVA hesap ekstresi parse etme
- Duplicate kayıt koruması
- İşlemleri kullanıcı bazlı PostgreSQL'e kaydetme
- Keyword tabanlı otomatik kategori atama
- Ana kategori ve alt kategori desteği
- Kategori, tarih, banka, tip ve açıklamaya göre filtreleme
- Mevcut işlemleri yeniden kategorilendirme
- Kullanıcının tam adına göre kendi hesapların arasındaki transferleri ayırt etme
- Kendine yapılan transferleri ayrıca listeleme
- Aylık gelir/gider ve kategori özetleri
- Vue tabanlı web arayüzü

## API

Auth:

```http
POST /api/auth/register
POST /api/auth/login
```

Transaction:

```http
GET  /api/transactions
POST /api/transactions
POST /api/transactions/upload
POST /api/transactions/upload/excel
POST /api/transactions/recategorize
GET  /api/transactions/bank/{bankName}
GET  /api/transactions/type/{type}
GET  /api/transactions/date?start=2026-01-01&end=2026-01-31
GET  /api/transactions/search?keyword=market
GET  /api/transactions/categories/main
GET  /api/transactions/categories/sub
GET  /api/transactions/main-category/{mainCategory}
GET  /api/transactions/sub-category/{subCategory}
GET  /api/transactions/self-transfers
GET  /api/transactions/month/{yyyy-MM}/main-category/{mainCategory}
GET  /api/transactions/month/{yyyy-MM}/sub-category/{subCategory}
GET  /api/transactions/summary/monthly
GET  /api/transactions/summary/monthly/main-category
GET  /api/transactions/summary/monthly/sub-category
```

Upload response'u kaydedilen ve duplicate olduğu için atlanan kayıt sayılarını içerir:

```json
{
  "parsedCount": 12,
  "savedCount": 10,
  "duplicateCount": 2,
  "savedTransactions": []
}
```

## Konfigürasyon

Lokal varsayılanlar `backend/src/main/resources/application.properties` içinde tutulur. Değerler environment variable ile override edilebilir:

| Değişken | Açıklama | Varsayılan |
| --- | --- | --- |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/aybashim` |
| `DB_USER` | Veritabanı kullanıcısı | `postgres` |
| `DB_PASSWORD` | Veritabanı şifresi | `secret` |
| `JPA_DDL_AUTO` | Hibernate schema modu | `update` |
| `JPA_SHOW_SQL` | SQL loglarını gösterir | `false` |
| `APP_JWT_SECRET` | Token imzalama secret'ı | `change-this-secret-before-production` |
| `APP_JWT_EXPIRATION_HOURS` | Token geçerlilik süresi | `24` |

Kendi hesap transferlerini ayırt etmek için kullanıcının kayıt sırasında verdiği tam ad kullanılır. Açıklamasında bu ad geçen transfer işlemleri `SELF_TRANSFER` olarak kategorilendirilir; normal liste ve özetlerden hariç tutulur, ancak `/api/transactions/self-transfers` üzerinden ayrıca görülebilir.

## Kurulum

Gereksinimler:

- Java 21+
- Maven 3.8+
- PostgreSQL

Çalıştırma:

```bash
createdb aybashim
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Vite arayüzü `http://localhost:5173` üzerinde çalışır ve `/api` isteklerini `http://localhost:8080` backend'ine yönlendirir.

Test:

```bash
cd backend
mvn test
```

## Kategorilendirme

İşlemler açıklama metnine göre normalize edilip keyword kurallarıyla kategorilendirilir. Her işlem için iki kategori tutulur:

- `mainCategory`
- `subCategory`

Örnek ana kategoriler:

- `INCOME`
- `HOUSING`
- `BILLS`
- `FOOD`
- `SHOPPING`
- `TRANSPORTATION`
- `HEALTH`
- `EDUCATION`
- `SUBSCRIPTION`
- `INVESTMENT`
- `TRANSFER`
- `CASH`
- `BANK_FEES`
- `OTHER`

Kategori kuralları güncellendiğinde mevcut kayıtlar yeniden kategorilendirilebilir:

```http
POST /api/transactions/recategorize
```

## Notlar

- HTTPie collection dosyası `backend/src/test/httpie_export/httpie-collection-aybashim.json` altında tutulur.
- Test ortamı PostgreSQL yerine H2 in-memory veritabanı kullanır.
- Gerçek ortamda `APP_JWT_SECRET` ve `DB_PASSWORD` environment variable olarak verilmelidir.
