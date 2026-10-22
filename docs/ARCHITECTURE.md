# Mimari kararlar

## Request flow
HTTP request -> Controller -> Service -> Repository -> PostgreSQL.
Controller'lar HTTP katmanında kalır. İş kuralları Service katmanındadır. Repository arayüzlerini Spring Data JPA uygular. EntityMapper DTO ve entity arasında dönüşüm yapar.

## Güvenlik
- `/api/v1/auth/**` herkese açıktır.
- Şarkı GET endpoint'leri herkese açıktır; yazma endpoint'leri JWT gerektirir.
- Playlist ve favorites endpoint'leri JWT gerektirir.
- Kullanıcı kimliği JWT `sub` claim'inden elde edilir.
- Bu örnekte JWT HS256 ile imzalanır. Secret en az 32 byte olmalıdır.

## Geliştirme sırası
1. Docker Compose ile servisleri ayağa kaldır.
2. Register/login endpoint'lerini Postman'de dene.
3. JWT ile şarkı oluştur.
4. Aynı token ile playlist oluştur, şarkı ekle.
5. Favori ekle ve listele.
6. Başka bir kullanıcı oluşturup ilk kullanıcının playlist'ine erişemediğini test et.
