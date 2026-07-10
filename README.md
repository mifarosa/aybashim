# aybashim

Kisisel finans takibi icin gelistirilen bir Spring Boot + Vue uygulamasi.
Banka ekstrelerini ice aktarir, islemleri otomatik kategorilendirir, aylik gelir/gider ozetlerini ve transferleri kullanici bazli olarak gosterir.

## Tech Stack

Backend:
- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- Apache PDFBox
- Apache POI

Frontend:
- Vue 3
- Vite

## Desteklenen Ekstreler

| Banka | Format | Tur | Upload Degeri |
| --- | --- | --- | --- |
| ING | PDF | Kredi karti ekstresi | `ING_CREDIT` |
| ING | PDF | Hesap ekstresi | `ING_ACCOUNT` |
| A101 Hadi | PDF | Kart ekstresi | `HADI` |
| Garanti BBVA | XLS | Hesap ekstresi | `GARANTI` |

## Ozellikler

- Kullanici kayit, giris ve profil guncelleme
- Bearer token ile `/api/**` endpoint korumasi
- PDF ve Excel ekstre yukleme
- Duplicate kayit korumasi
- Kullanici bazli PostgreSQL veri saklama
- Keyword tabanli otomatik kategori atama
- Kategori, tarih, banka, tip ve aciklamaya gore filtreleme
- Tablo kolon siralama
- Aylik gelir/gider ve kategori ozetleri
- Tam ada gore kendi hesaplarin arasindaki transferleri ayirma
- Vue tabanli dashboard arayuzu

## API

Auth:

```http
POST /api/auth/register
POST /api/auth/login
PUT  /api/auth/me
```

Transactions:

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

Upload response:

```json
{
  "parsedCount": 12,
  "savedCount": 10,
  "duplicateCount": 2,
  "savedTransactions": []
}
```

## Konfigurasyon

Backend varsayilanlari `backend/src/main/resources/application.properties` icindedir ve environment variable ile override edilebilir.

| Degisken | Aciklama | Varsayilan |
| --- | --- | --- |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/aybashim` |
| `DB_USER` | Veritabani kullanicisi | `postgres` |
| `DB_PASSWORD` | Veritabani sifresi | `secret` |
| `JPA_DDL_AUTO` | Hibernate schema modu | `update` |
| `JPA_SHOW_SQL` | SQL loglarini gosterir | `false` |
| `JPA_OPEN_IN_VIEW` | JPA open-in-view modu | `false` |
| `APP_JWT_SECRET` | Token imzalama secret'i | `change-this-secret-before-production` |
| `APP_JWT_EXPIRATION_HOURS` | Token gecerlilik suresi | `24` |

Gercek ortamda `APP_JWT_SECRET` ve `DB_PASSWORD` mutlaka environment variable olarak verilmelidir. Gercek secret veya parola repository'ye eklenmemelidir.

## Kurulum

Gereksinimler:

- Java 21+
- PostgreSQL
- Node.js ve npm

Backend calistirma:

```powershell
cd backend
$env:JAVA_HOME='C:\Users\mfg\.jdks\ms-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Vite arayuzu `http://localhost:5173` uzerinde calisir ve `/api` isteklerini `http://localhost:8080` backend'ine yonlendirir.

## Build ve Test

Backend:

```powershell
cd backend
$env:JAVA_HOME='C:\Users\mfg\.jdks\ms-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd test
```

Frontend:

```bash
cd frontend
npm run build
```

Bu repoda su an ayri bir lint, formatter, type-check veya static-analysis script'i tanimli degildir.

## Kategorilendirme

Islemler aciklama metnine gore normalize edilip keyword kurallariyla kategorilendirilir. Her islem icin iki kategori tutulur:

- `mainCategory`
- `subCategory`

Kurallar guncellendiginde mevcut kayitlar tekrar kategorilendirilebilir:

```http
POST /api/transactions/recategorize
```

## Notlar

- HTTPie collection dosyasi `backend/src/test/httpie_export/httpie-collection-aybashim.json` altinda tutulur.
- Test ortami PostgreSQL yerine H2 in-memory veritabani kullanir.
- Kullanici tam adi, kendi hesaplarina yaptigi transferleri tespit etmek icin kullanilir. Bu kayitlar normal liste ve ozetlerden haric tutulur, `/api/transactions/self-transfers` ile ayri gorulebilir.
