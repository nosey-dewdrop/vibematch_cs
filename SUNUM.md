# VibeMatch — Sunum Notları

Bu benim (Damla) kısmım: backend + arayüzü backend'e bağlama.
Her bölümde: NE DİYECEĞİN + gerekirse jüri sorunca CEVAP.

---

## 0. AÇILIŞ — bir cümlede ne yaptım

> "Bu projede benim işim backend'di. Ekip arayüzü hazırlamıştı ama o arayüz
> sadece hafızada çalışıyordu — uygulamayı kapatınca hesaplar, mesajlar
> siliniyordu. Ben gerçek bir istemci-sunucu sistemi, bir veritabanı ve socket
> haberleşmesi yazdım, sonra arayüzün her ekranını bu sisteme bağladım. Artık
> iki kişi ayrı bilgisayardan aynı anda mesajlaşabiliyor ve veriler kalıcı."

---

## 1. MİMARİ — Client-Server

> "VibeMatch bir client-server uygulaması. Tek bir sunucu çalışıyor, kullanıcı
> uygulamaları (istemciler) ona TCP socket üzerinden bağlanıyor.
>
> Neden böyle? Çünkü çok kullanıcılı bir sistem — benim yazdığım mesajın karşı
> tarafa gitmesi lazım. Herkes kendi hafızasında çalışsaydı bu imkansızdı. O
> yüzden veriyi tek bir merkezde, sunucuda tuttuk; herkes o sunucuya bağlanıp
> aynı veriyi görüyor.
>
> Sistem şöyle açılıyor: önce sunucu ayağa kalkıyor. `ServerMain` sırayla üç iş
> yapıyor — `Db.connect()` ile veritabanını açıyor, örnek verileri yüklüyor,
> sonra `ChatServer` 5050 portunda bir socket açıp istemcileri dinlemeye
> başlıyor. Her istemci bağlandığında ona ayrı bir thread açıyoruz — buna
> thread-per-client deniyor. Böylece birçok kullanıcı aynı anda, birbirini
> bekletmeden çalışıyor."

**Jüri: "Thread'ler aynı anda DB'ye yazınca race condition olmaz mı?"**
> "Oluyordu, çözdük. SQLite bağlantısı thread-safe değil, o yüzden tüm DB
> erişimini tek bir kilitle (ReentrantLock) sıraya soktuk. Her istek kilidi
> alıp bırakıyor. 12 istemci, her biri 80 işlemle test ettik, sıfır hata."

Anahtar terimler: client-server, TCP socket, ServerSocket, accept loop,
thread-per-client, ReentrantLock, race condition.

---

## 2. VERİTABANI — SQLite + DAO

> "Verileri SQLite'ta saklıyoruz — tek dosyalık, gömülü bir veritabanı. Ayrı
> bir DB sunucusu kurmuyoruz, `vibematch.db` dosyası her şeyi tutuyor. Proje
> ölçeği için doğru araç.
>
> 11 tablomuz var: users, messages, communities, memberships, posts, comments,
> friendships, notifications, user_interests, spotify_profiles ve community_tags.
>
> Tablolara DAO pattern ile erişiyoruz — Data Access Object. Her tablonun kendi
> sınıfı var: UserDao sadece users tablosuyla, MessageDao sadece mesajlarla
> ilgilenir. Böylece SQL kodu ekranlara dağılmıyor, her tablonun erişimi tek bir
> sınıfta toplanıyor. Ekran 'bana bu kullanıcıyı getir' der, arkada SQL
> çalıştığını bilmez."

**Güvenlik (bunu mutlaka söyle):**
> "Şifreleri asla düz metin saklamıyoruz. Her kullanıcıya rastgele bir salt
> üretip şifreyi PBKDF2 ile hash'liyoruz — yavaş, kırması zor bir algoritma.
> Veritabanı çalınsa bile şifreler okunamaz. Ayrıca tüm SQL sorgularımız
> prepared statement — yani SQL injection'a kapalı."

**Jüri: "SQL injection nedir, nasıl engelledin?"**
> "Kullanıcının yazdığını SQL cümlesine yapıştırmıyoruz. Cümlede yerine soru
> işareti koyup girdiyi ayrı gönderiyoruz. Böylece kullanıcı ne yazarsa yazsın
> — komut bile olsa — veritabanı onu düz veri sayar, çalıştırmaz."

Anahtar: SQLite, gömülü DB, DAO pattern, salt, PBKDF2, prepared statement,
SQL injection.

---

## 3. MESAJLAŞMA + CANLI BİLDİRİM (push)

> "Mesajları veritabanında saklıyoruz. Biri mesaj gönderince önce iki kontrol
> yapıyoruz: mesaj boş mu, ve iki kişi gerçekten arkadaş mı — arkadaş değilse
> reddediyoruz. Kontroller geçince mesajı messages tablosuna kaydediyoruz,
> kalıcı olarak.
>
> Sonra işin canlı kısmı: sunucu kimlerin çevrimiçi olduğunu bir listede tutuyor
> (online map). Alıcı o an çevrimiçiyse, mesajı beklemeden ona itiyoruz — buna
> push deniyor. Alıcının ekranındaki bir reader thread bu push'u yakalayıp
> sohbeti kendiliğinden yeniliyor. Kimse 'yenile'ye basmıyor, mesaj anında
> beliriyor. Aynı anda bildirim çanına da düşüyor."

**Jüri: "Polling mi push mu kullandın, farkı ne?"**
> "Push. Polling'de uygulama sürekli 'yeni mesaj var mı' diye sorar, verimsiz.
> Push'ta sunucu olay olunca kendisi haber veriyor — anlık ve verimli."

Anahtar: push, polling, online map (HashMap + synchronized), reader thread,
listener.

---

## 4. ARAYÜZÜ BACKEND'E BAĞLAMA (asıl entegrasyon işim)

> "Ekip arayüzünde 13 ekran vardı ama hepsi hafızada çalışıyordu. Ben her birini
> tek tek gerçek backend'e bağladım. Bunun için bir Api sınıfı yazdım — 343
> satır, 52 metot — ekran ile sunucu arasındaki köprü. Ekran mesela `api.login()`
> diyor, Api bunu JSON'a paketleyip socket'ten sunucuya yolluyor, gelen cevabı
> nesneye çevirip geri veriyor. Ekran socket'i, JSON'u hiç görmüyor.
>
> Bağlarken birkaç uyumsuzluğu köprülemem gerekti:
> - Arayüz email ile çalışıyordu, backend username istiyordu — email'in @
>   öncesinden username türettim.
> - Kişilik testi 3 soruydu, backend 16 soru istiyor — testi gerçek 16 soruyla
>   yeniden kurdum.
> - Arayüz grup sohbeti varsayıyordu ama backend birebir mesajlaşma yapıyor —
>   düzeni koruyup 1-1 mesajlaşmaya bağladım.
>
> Ayrıca donmayı önlemek için tüm sunucu çağrılarını arka thread'e aldım
> (SwingWorker), böylece bir istek beklerken pencere donmuyor."

**Ekstra kattığım (isteğe bağlı söyle):**
> "Ekipte olmayan iki şey de ekledim: canlı sayaçlı bir bildirim ekranı, ve
> sohbet ekranına arkadaş ekleme + gelen istekleri kabul etme."

Anahtar: Api katmanı, köprü, JSON serialize, SwingWorker (donma önleme),
uyumsuzluk köprüleme.

---

## 5. CANLI DEMO — ne göstereceğin

1. Server'ı başlat: `./run-server.sh` → "listening on port 5050" bekle
2. İki client aç: `./run.sh` → biri `ada`, biri `mert` (şifre `vibe1234`)
3. Göster: "iki ayrı kullanıcı aynı sunucuya bağlandı, server log'da iki
   'client connected' var"
4. Chats → Add friend ile arkadaş ekle → öbür pencerede Friend requests → kabul
5. Mesaj at → öbür pencerede ANINDA belirsin + Notifications sayacı artsın
6. Kapat-aç → veriler duruyor (kalıcılık)

**Demo kuralı:** server "listening on port 5050" yazmadan client açma.

---

## RAKAMLAR (doğru veri, güvenle söyle)
- 14 server-tarafı sınıf, 7 DAO, 11 tablo
- Api: 343 satır, 52 metot (köprü katmanı)
- 13 ekran backend'e bağlandı
- Eşzamanlılık testi: 12 istemci × 80 işlem, 0 hata
