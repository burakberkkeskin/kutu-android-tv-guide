# BASELINE-SUMMARY.md

## 1. Cihaz Kimliği (Device Spec)
- **Üretici (Manufacturer):** SkyworthDigital
- **Model:** UHD Google TV STB (`HPR314-Next`)
- **İşletim Sistemi (OS):** Android 14 (API Level 34)
- **Yapı Numarası (Build):** Next/HPR314-Next/RMQ:14/UTT1.250214.001/C1.0.8_20260520:user/release-keys
- **İşlemci / Mimari (SoC / ABI):** Realtek SoC / `armeabi-v7a` (32-bit userland)
- **Ekran Çözünürlüğü (Display):** 3840x2160 (Override: 1920x1080 @ 320 dpi)
- **Bağlantı Hedefi:** `192.168.1.147:5555`

## 2. Bellek ve Depolama (RAM & Storage)
- **Toplam RAM (MemTotal):** 1,966,112 kB (~1.92 GB)
- **Kullanılan RAM (Used RAM):** ~1,221,873 kB
- **Kullanılabilir RAM (MemAvailable):** ~820,124 kB (~820 MB)
- **zRAM / Swap:** 159,696 kB fiziksel alan, 433,436 kB takas alanı kullanılıyor (978,444 kB toplam swap)
- **Dahili Depolama (/data):** 26 GB toplam, 4.6 GB kullanılan (%18), 21 GB boş

## 3. Mevcut Başlatıcı (HOME Launcher)
- **Varsayılan HOME:** `com.google.android.apps.tv.launcherx/.home.HomeActivity` (Google TV Launcher)
- **Yüklü Alternatif Başlatıcı:** `com.spocky.projengmenu` (Projectivy Launcher Engine/Menu)

## 4. Paket Sayıları (Package Counts)
- **Toplam Paket:** 156
- **Sistem Paketi:** 134
- **Üçüncü Parti Paket:** 22
- **Başlangıçta Devre Dışı Olanlar (Dokunulmayacak):**
  - `com.next.iptv`
  - `com.jettv.player`

## 5. Başlıca Bellek Tüketen Süreçler (PSS Snapshot)
- `system`: ~134.8 MB
- `com.google.android.youtube.tv`: ~104.1 MB
- `com.google.android.gms`: ~77.5 MB
- `com.android.tv.settings`: ~75.1 MB
- `com.github.damontecres.wholphin`: ~64.4 MB
- `com.spocky.projengmenu`: ~59.3 MB
- `com.google.android.apps.tv.launcherx` + `coreservices`: ~87.2 MB
- `com.google.android.katniss` (Asistan/Sesli arama): ~56.1 MB
- `com.android.systemui`: ~41.9 MB
- `com.android.vending` (Play Store): ~37.9 MB
- `com.google.android.apps.mediashell` (Chromecast): ~27.5 MB
- `com.google.android.apps.tv.dreamx` (Ambient Mode): ~23.5 MB

## 6. Optimizasyon / Debloat Sınıflandırması
- **Grup A (Düşük Risk / Onaylanan İlk Paketler):**
  - `com.android.printspooler` (Yazdırma servisi)
  - `com.sdt.producttest` (Skyworth fabrika test aracı)
  - `com.oem.qa` (OEM QA test aracı)
  - `com.google.android.feedback` (Google TV hata raporlayıcı)
  - `com.android.tv.feedbackconsent` (Hata raporlama onayı)
- **Grup B (Kullanıma Bağlı - Kullanıcı Talebiyle Korunanlar):**
  - `com.google.android.youtube.tvmusic` (YouTube Music)
  - `com.google.android.apps.tv.dreamx` (Ekran Koruyucu)
  - `com.google.android.marvin.talkback` (TalkBack)
  - `com.spocky.projengmenu` (Projectivy Launcher)
- **Grup C (Kritik / Kesinlikle Dokunulmayacaklar):**
  - Ses/Görüntü: `com.dolby.android.audio.service`
  - Kumanda: `com.sdt.mecoolrcupair`, `com.sdt.globalkey`, `com.android.bluetooth`, `com.google.android.tv.remote.service`
  - Donanım / LED: `com.sdt.frontpanelledsservice`
  - Çekirdek: `com.google.android.gms`, `com.android.vending`, `com.google.android.katniss`, `com.google.android.apps.mediashell`
  - DRM / TV Providers: `com.android.providers.tv`, Widevine servisleri

## 7. Ağ ve Port Durumu
- Port 5555: ADB Daemon (Dinliyor)
- Port 7000 (AirPlay): Dinlemiyor (Yerleşik AirPlay yayını yok)
- Port 8008 / 8009: Google Cast / Chromecast (Aktif)
