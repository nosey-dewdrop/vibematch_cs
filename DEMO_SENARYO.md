# vibematch — canlı sunum senaryosu

her adımda: NE YAZACAĞIN + NEYE BASACAĞIN + NE GÖSTERECEĞİN + NE DİYECEĞİN.
sırayla git, atlama. iki terminal + iki uygulama penceresi kullanılıyor.

---

## HAZIRLIK (sunumdan 5 dk önce, sınıfta)

1. iki terminal aç.
2. temiz başlamak istersen (önerilir, seed hesaplar hazır gelir):
   ```
   cd ~/damla_projects_2026/cs_school_projects/vibematch_cs
   rm -f vibematch.db
   ```
   > db silinince açılışta 5 demo hesap + 8 topluluk + örnek forumlar
   > otomatik kurulur. ada / mert / zeynep / can / elif — şifre hepsi: vibe1234

3. wifi'ye bağlı ol (spotify + mail için lazım).

---

## BÖLÜM 1 — MİMARİ (konuşma, ekran: proje klasörü / slayt)

DE Kİ:
- "vibematch bir istemci–sunucu uygulaması. tek sunucu var, veritabanını o
  tutuyor. herkes ona socket ile bağlanıyor."
- "katmanlar ayrı: model / service / server / data / net / screens."
- "gerçek bir tcp sunucu, gerçek sqlite veritabanı, gerçek canlı push var —
  mesaj gelince ekran kendi yenileniyor, yenile'ye basmıyorsun."

GÖSTER: repo klasör yapısı (README.md'deki "Repo structure" tablosu).

---

## BÖLÜM 2 — SUNUCUYU BAŞLAT (Terminal 1)

YAZ:
```
cd ~/damla_projects_2026/cs_school_projects/vibematch_cs && ./run-server.sh
```

GÖSTERECEĞİN ÇIKTI:
```
compiling...
starting server...
vibematch server listening on port 5050
```

DE Kİ: "sunucu ayakta, 5050 portunu dinliyor. bu terminal açık kalacak."

> bu terminali KAPATMA.

---

## BÖLÜM 3 — VERİTABANI OPERASYONLARI (kayıt → doğrulama → giriş)

### 3a. birinci uygulamayı aç (Terminal 2)
YAZ:
```
cd ~/damla_projects_2026/cs_school_projects/vibematch_cs && ./run.sh
```
> login penceresi açılır.

### 3b. gerçek kayıt (canlı db'ye yazma + gerçek mail)
- "Sign up" linkine bas.
- doldur:
  - isim: istediğin
  - kullanıcı adı: `demo1`
  - email: KENDİ bilkent mailin (`...@ug.bilkent.edu.tr`) — sadece bilkent kabul
  - şifre: `vibe1234`
- "Create account"a bas.

DE Kİ: "hesap şu an veritabanına yazıldı, ve gerçekten bir doğrulama maili
gitti." → doğrulama ekranı açılır.

### 3c. doğrulama
- mailine gelen 6 haneli kodu gir (spam'e de bak). gelmezse ekranda
  "Resend" var.
- "Verify"e bas → ilgi alanı ekranına geçer.

DE Kİ: "email doğrulaması gerçek — kod veritabanı değil, gelen mailden."

> NOT: kayıt yapıp doğrulamadan uygulamayı KAPATMA. (kapatırsan bile artık
> giriş ekranı seni tekrar doğrulamaya yönlendiriyor — ama sunumda düz git.)

---

## BÖLÜM 4 — EŞLEŞME (ilgi + kişilik testi + yüzde)

### 4a. ilgi alanları
- en az 3 ilgi seç (müzik, kodlama, kahve...).
- "Continue"ya bas.

### 4b. vibe testi (16 soru = MBTI)
- 16 soruyu A/B seçerek geç.
- son soruda tip çıkar (örn INFP) + eksen barları.

DE Kİ: "kişilik tipi sunucuda hesaplanıyor, istemcide değil — sonuç ile
kaydedilen asla çelişmesin diye."

### 4c. ana akış — eşleşme yüzdesi
- ana ekranda topluluklar, her birinde bir "match %".

DE Kİ: "yüzde iki şeyden: ilgi etiketi örtüşmesi (%65) + kişilik yakınlığı
(%35). bu senin için hesaplanmış gerçek bir skor."

---

## BÖLÜM 5 — TOPLULUK + FORUM (CRUD: oluştur/sil)

- bir topluluğa gir → "Join community"ye bas (kart "Open"a döner).
- forumda "New post" → başlık + içerik yaz → paylaş.
  > boş başlık/içerik denersen sunucu reddediyor — göster istersen.
- kendi postuna yorum yaz, birine "Reply" ile cevap ver (iç içe).
- kendi postunda/yorumunda "Delete" linki var → sil.

DE Kİ: "her oluşturmanın tersi var — sil, ayrıl. başkasının postunu
silemezsin, sadece kendininkini." (sunucu yetki kontrolü yapıyor.)

---

## BÖLÜM 6 — İKİNCİ KULLANICI + CANLI MESAJLAŞMA (asıl gösteri)

### 6a. ikinci uygulamayı aç (Terminal 3, YENİ terminal)
YAZ:
```
cd ~/damla_projects_2026/cs_school_projects/vibematch_cs && ./run.sh
```
- ikinci pencerede giriş yap: kullanıcı `mert`, şifre `vibe1234`.

DE Kİ: "aynı sunucuya ikinci bir kullanıcı bağlandı. iki kişi aynı anda."

### 6b. arkadaşlık
- birinci pencerede (demo1) → arkadaş ekle → `mert`.
- ikinci pencerede (mert) → gelen istek görünür → kabul et.

DE Kİ: "istek anında karşıya düştü — canlı push, yenileme yok."

### 6c. canlı mesaj
- demo1 → mert'e mesaj yaz, gönder.
- İKİNCİ pencereye bak: mesaj ANINDA orada + bildirim çanı arttı.

DE Kİ: "mesaj sunucudan diğer istemciye canlı itildi. kimse yenile'ye
basmadı." (bu demonun en güçlü anı — iki pencereyi yan yana tut.)

> arkadaş olmayanlara mesaj engelli, boş mesaj engelli — istersen dene.

---

## BÖLÜM 7 — SPOTIFY API (canlı bağlanma)

- birinci pencerede (demo1) → Profile → "Connect Spotify"ye bas.
- tarayıcı açılır → Spotify → "Agree"ye bas.
- tarayıcı "connected ✓" der → uygulamaya dön.
- profilde: top sanatçıların + "music:" etiketleri ilgi alanına eklendi.

DE Kİ: "OAuth (PKCE) ile gerçek Spotify hesabı bağlandı. en çok dinlediğin
sanatçılar çekildi ve eşleşme algoritmasını besliyor — aynı sanatçıları
dinleyenlerle eşleşmen güçleniyor."

DÜRÜST NOT (sorulursa): "Spotify tür verisini yeni geliştirici
uygulamalarına Kasım 2024'ten beri kapatıyor (403). Biz de tür yerine
sanatçı adıyla eşleştiriyoruz — daha somut aslında."

- "Disconnect Spotify"ye basınca müzik etiketleri ilgiden temizleniyor,
  senin elle seçtiklerin kalıyor. (her aksiyonun tersi.)

---

## BÖLÜM 8 — KALICILIK (isteğe bağlı, güçlü kapanış)

- bir uygulamayı kapat, `./run.sh` ile tekrar aç, aynı hesapla gir.
- hesabın, toplulukların, mesajların, Spotify bağlantın hâlâ orada.

DE Kİ: "her şey sqlite'ta kalıcı. uygulamayı kapatıp açıyorsun, hiçbir şey
kaybolmuyor."

---

## KAPANIŞ — KİM NE YAPTI (takım slaytı için)

[buraya takım üyelerini ve kimin hangi katmanı yazdığını koy —
 server/data, screens/ui, service/eşleşme, net/protocol, spotify vb.]

---

## ACİL DURUM / TAKILIRSAN

- uygulama açılmıyor / "compile failed": terminaldeki ilk hata satırını oku.
- "No server" uyarısı: Terminal 1'de server çalışıyor mu bak, çalışmıyorsa
  `./run-server.sh` tekrar.
- Spotify tarayıcı açılmadı: birkaç saniye bekle; olmazsa "Disconnect" yok
  demektir, tekrar "Connect"e bas.
- port 8888 hatası: Spotify'ı yarıda bıraktıysan 1 dk bekle veya
  terminalleri kapatıp baştan aç.
- her şey karışırsa: iki uygulamayı kapat, `rm -f vibematch.db`,
  server'ı yeniden başlat, baştan.
```
