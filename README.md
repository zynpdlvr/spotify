# Spotify Benzeri Backend (öğrenme projesi)

Java 17, Spring Boot 3.4, Spring Security, JWT, Spring Data JPA/Hibernate, PostgreSQL, Spring HATEOAS ve Docker Compose ile hazırlanmış başlangıç projesi.

## Gereksinimler
- Docker Desktop + Docker Compose
- IntelliJ IDEA (isteğe bağlı, IDE üzerinden geliştirmek için)
- Postman (API denemeleri için)

## 1. Çalıştırma
Proje klasöründe terminal aç:

```bash
docker compose up --build
```

Backend: http://localhost:8080  
PostgreSQL: localhost:5432

Durdurmak için `Ctrl+C`, arka planda çalıştırmak için `docker compose up --build -d`; kapatmak için `docker compose down`. Veritabanı verilerini silmek için `docker compose down -v` (dikkat: kalıcı verileri siler).

> `.env.example` dosyasını `.env` olarak kopyala ve yerel geliştirme dışındaki ortamlarda güçlü, gizli değerler kullan. Bu örnek değerleri üretimde kullanma.

## 2. IntelliJ IDEA ile açma
1. ZIP'i çıkart.
2. IntelliJ IDEA > Open > `spotify-backend` klasörünü seç.
3. Maven projesinin yüklenmesini bekle.
4. Java SDK olarak 17 seç.
5. PostgreSQL'i Docker ile çalıştırıp uygulamayı IDE'den başlatmak istersen `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spotify`, `SPRING_DATASOURCE_USERNAME=spotify_user`, `SPRING_DATASOURCE_PASSWORD=change_me_local` ortam değişkenlerini ayarla.
6. `SpotifyBackendApplication` sınıfını çalıştır.

## 3. Postman ile deneme

### Kayıt
`POST http://localhost:8080/api/auth/register`
```json
{
  "displayName": "Ada",
  "email": "ada@example.com",
  "password": "GucluParola123"
}
```
Yanıttaki `accessToken` değerini kopyala.

### Giriş
`POST http://localhost:8080/api/auth/login`
```json
{
  "email": "ada@example.com",
  "password": "GucluParola123"
}
```

Postman'de Authorization > Bearer Token alanına token'ı yapıştır. Şarkı oluşturma, playlist ve favori endpoint'leri için token gerekir.

### Şarkı ekleme
`POST http://localhost:8080/api/songs`
Authorization: Bearer Token
```json
{
  "title": "Örnek Şarkı",
  "artist": "Örnek Sanatçı",
  "album": "İlk Albüm",
  "audioUrl": "https://example.com/audio.mp3"
}
```

### Şarkıları listeleme / arama
- `GET http://localhost:8080/api/songs`
- `GET http://localhost:8080/api/songs?q=örnek`

### Çalma listesi oluşturma
`POST http://localhost:8080/api/playlists`
```json
{"name":"Favori Yolculuklar","description":"Yol için seçtiklerim"}
```

### Playlist'e şarkı ekleme
`PUT http://localhost:8080/api/playlists/1/songs/1`

### Kendi çalma listelerim
`GET http://localhost:8080/api/playlists`

### Favori ekleme/listeleme/silme
- `PUT http://localhost:8080/api/me/favorites/1`
- `GET http://localhost:8080/api/me/favorites`
- `DELETE http://localhost:8080/api/me/favorites/1`

## 4. Endpoint özeti

| Method | Endpoint | Kimlik doğrulama | Açıklama |
|---|---|---|---|
| POST | `/api/auth/register` | Hayır | Kayıt ve token |
| POST | `/api/auth/login` | Hayır | Giriş ve token |
| GET | `/api/songs` | Hayır | Şarkıları listele/ara |
| GET | `/api/songs/{id}` | Hayır | Şarkı detayı |
| POST | `/api/songs` | Evet | Şarkı oluştur |
| PUT | `/api/songs/{id}` | Evet | Şarkıyı güncelle |
| DELETE | `/api/songs/{id}` | Evet | Şarkıyı sil |
| GET | `/api/playlists` | Evet | Kendi listelerini getir |
| POST | `/api/playlists` | Evet | Liste oluştur |
| GET | `/api/playlists/{id}` | Evet | Sahibi olduğun liste |
| PUT | `/api/playlists/{id}` | Evet | Listeyi güncelle |
| DELETE | `/api/playlists/{id}` | Evet | Listeyi sil |
| PUT | `/api/playlists/{id}/songs/{songId}` | Evet | Şarkı ekle |
| DELETE | `/api/playlists/{id}/songs/{songId}` | Evet | Şarkı çıkar |
| GET | `/api/me/favorites` | Evet | Kendi favorilerin |
| PUT | `/api/me/favorites/{songId}` | Evet | Favoriye ekle |
| DELETE | `/api/me/favorites/{songId}` | Evet | Favoriden çıkar |

## 5. Katmanlar
- `controller`: HTTP endpoint'leri ve HATEOAS bağlantıları.
- `service`: iş kuralları, doğrulama ve veri sahipliği kontrolleri.
- `repository`: Spring Data JPA veri erişimi.
- `mapper`: Entity-DTO dönüşümleri.
- `entity`: Hibernate/JPA ile eşlenen tablolar.
- `dto`: dış dünyaya açılan istek ve yanıt modelleri.
- `config`: Spring Security ve JWT ayarları.

## 6. Veritabanı ilişkileri
- `AppUser 1 -> N Playlist`: her çalma listesinin tek sahibi vardır.
- `Playlist N <-> N Song`: `playlist_songs` bağlantı tablosu üzerinden.
- `AppUser 1 -> N Favorite` ve `Song 1 -> N Favorite`: favoriler ayrı kayıtlar olarak tutulur; kullanıcı/şarkı ikilisi benzersizdir.

Hibernate başlangıç için `ddl-auto: update` ile tabloları oluşturur. Gerçek projede Flyway/Liquibase migration kullanılması önerilir.

## 7. Tasarım ve güvenlik notları
- Parolalar BCrypt ile hash'lenir; düz metin saklanmaz.
- JWT subject alanı kullanıcı ID'sidir. Playlist/favorite servisleri kullanıcı ID'sini token'dan alır; istemcinin gönderdiği bir `ownerId` değerine güvenmez.
- Başka kullanıcının playlist'ini tahmin edilen ID ile okumak/değiştirmek mümkün olmamalıdır; sahiplik sorgusu 404 döndürür.
- GET kaynakları, POST ile oluşturma, PUT ile idempotent ilişki ekleme ve DELETE ile silme gibi HTTP anlamları kullanılır.
- HATEOAS örnekleri `_links` üretir.
- Bu bir eğitim iskeletidir; üretim öncesi rate limiting, refresh token/rotation veya kimlik sağlayıcı, audit log, migration, pagination, kapsamlı hata şeması, CORS politikası, secret manager, yedekleme ve güvenlik testleri eklenmelidir.
- JWT sırrını kaynak koduna koyma; environment variable / secret manager kullan. Örnek sırrı yalnızca yerel geliştirme içindir.

## 8. Referanslar
- Spring Boot system requirements: https://docs.spring.io/spring-boot/3.4/system-requirements.html
- Spring Security JWT Resource Server: https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html
- Microsoft REST API Guidelines: https://github.com/microsoft/api-guidelines
