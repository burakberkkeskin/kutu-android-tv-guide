# DEBLOAT-LOG.md

Bu dosya, bu proje kapsamında hedef cihazda (`SkyworthDigital UHD Google TV STB`) yapılan her paket ve sistem değişikliğinin denetim günlüğüdür.

## Başlangıçta Devre Dışı Olan Paketler (Dokunulmayacak - Proje Öncesi Durum)
- `com.next.iptv`
- `com.jettv.player`

## Değişiklik Günlüğü (Audit Log)

| Zaman (Timestamp) | Paket Adı | Gerekçe | Uygulanan Komut | Geri Alma Komutu (Undo) | Durum / Test Sonucu |
|---|---|---|---|---|---|
| (Henüz değişiklik yapılmadı) | - | - | - | - | - |
| 2026-09-16 16:39:23 | com.android.printspooler | Yazdırma Arka Plan Servisi | `pm disable-user --user 0 com.android.printspooler` | `pm enable --user 0 com.android.printspooler` | Package com.android.printspooler new state: disabled-user |
| 2026-09-16 16:39:28 | com.sdt.producttest | Skyworth Fabrika Test Uygulaması | `pm disable-user --user 0 com.sdt.producttest` | `pm enable --user 0 com.sdt.producttest` | Package com.sdt.producttest new state: disabled-user |
| 2026-09-16 16:39:31 | com.oem.qa | OEM QA Test Aracı | `pm disable-user --user 0 com.oem.qa` | `pm enable --user 0 com.oem.qa` | Package com.oem.qa new state: disabled-user |
| 2026-09-16 16:39:31 | com.google.android.feedback | Google TV Hata Geri Bildirim Servisi | `pm disable-user --user 0 com.google.android.feedback` | `pm enable --user 0 com.google.android.feedback` | Package com.google.android.feedback new state: disabled-user |
| 2026-09-16 16:39:31 | com.android.tv.feedbackconsent | Hata Bildirim Onay Bileşeni | `pm disable-user --user 0 com.android.tv.feedbackconsent` | `pm enable --user 0 com.android.tv.feedbackconsent` | Package com.android.tv.feedbackconsent new state: disabled-user |
| 2026-09-16 17:04:21 | com.boost.airplay.receiver | Kutu Mirror kurulduktan sonra gereksiz 3. parti alıcı kaldırıldı | `pm uninstall com.boost.airplay.receiver` | Play Store'dan yeniden kurulabilir | Success |
| 2026-09-16 17:04:21 | com.nero.swiftlink.mirror.tv | Kutu Mirror kurulduktan sonra gereksiz 3. parti alıcı kaldırıldı | `pm uninstall com.nero.swiftlink.mirror.tv` | Play Store'dan yeniden kurulabilir | Success |
| 2026-09-16 18:09:07 | com.next.roket | Next OEM Roket başlatıcı / lisans hatası veren atıl araç | `pm disable-user --user 0 com.next.roket` | `pm enable --user 0 com.next.roket` | Package com.next.roket new state: disabled-user |
| 2026-09-16 18:31:51 | com.google.android.videos | Google TV Filmler / Video mağazası (kullanılmıyor) | `pm disable-user --user 0 com.google.android.videos` | `pm enable --user 0 com.google.android.videos` | Package com.google.android.videos new state: disabled-user |

