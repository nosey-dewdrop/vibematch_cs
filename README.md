# vibematch

Bilkent öğrencileri için topluluk eşleştirme uygulaması. İlgi alanlarını seçiyorsun, kısa bir kişilik testi yapıyorsun, uygulama sana uyan toplulukları gösteriyor. Topluluklara katılabilir, sohbet edebilir, forum açabilir, arkadaş ekleyebilirsin.

## nasıl çalışır?

Bir sunucu ve bir istemci var. Sunucu tek makinede çalışır ve veritabanını tutar. Diğerleri ağ üzerinden bağlanır. Bir şey olduğunda (mesaj, arkadaşlık isteği) sana anında iletilir, yenileme gerekmez.

## çalıştırmak için

Önce sunucuyu başlat:
```
./run-server.sh
```

Sonra başka bir terminalde istemciyi aç:
```
./run.sh
```

Farklı bir laptoptan bağlanıyorsan:
```
./run.sh <sunucu makinenin ip adresi>
```

Sunucunun ip adresini öğrenmek için: `ipconfig getifaddr en0`

## demo hesapları

Veritabanı ilk çalıştırmada kendini oluşturur. Bu hesaplardan herhangiyle giriş yapabilirsin:

| kullanıcı adı | email | şifre |
|---|---|---|
| ada | ada@ug.bilkent.edu.tr | vibe1234 |
| mert | mert@ug.bilkent.edu.tr | vibe1234 |
| zeynep | zeynep@ug.bilkent.edu.tr | vibe1234 |
| can | can@ug.bilkent.edu.tr | vibe1234 |
| elif | elif@ug.bilkent.edu.tr | vibe1234 |

Gerçek Bilkent mailiyle de kayıt olunabilir. Sadece @ug.bilkent.edu.tr kabul eder.

## repo yapısı

```
model/        veri sınıfları (User, Community, Message ...)
data/         veritabanı katmanı, sqlite
server/       tcp sunucu, her istemci için ayrı thread
net/          istemci tarafı soket bağlantısı
service/      eşleştirme algoritması, mbti puanlama, auth
screens/      tüm ekranlar
protocol/     soket üzerindeki json formatı
ui/           ortak swing bileşenleri ve tema
model_cs/     ödev için yazılan orijinal model sınıfları
controller/   ödev için yazılan orijinal controller sınıfları
```

## eşleştirme algoritması

İki şeye bakıyor. Topluluğun etiketleriyle ilgi alanlarının ne kadar örtüştüğü (yüzde 65 ağırlık) ve kişilik tipinin o topluluğun tipik üyesiyle ne kadar uyuştuğu (yüzde 35). Sonuç her kart üzerinde eşleşme yüzdesi olarak çıkıyor.

## notlar

- Veriler çalıştırmalar arasında kalıcı. Sqlite dosyası: vibematch.db.
- Doğrulama emaili isteğe bağlı. Smtp kurulu değilse kod sunucu konsoluna yazılır.
- Farklı bir makineden bağlanmak için sunucu ve istemci aynı ağda olmalı.
