#!/usr/bin/env bash
# RESTORE-ALL.sh
# Bu betik, bu proje kapsamında devre dışı bırakılan tüm paketleri ve orijinal HOME başlatıcısını geri yükler.
set -euo pipefail

TARGET_DEVICE="${1:-192.168.1.147:5555}"

echo "=== KUTU ANDROID TV GERİ YÜKLEME (RESTORE) BAŞLATILIYOR ==="
echo "Hedef Cihaz: ${TARGET_DEVICE}"

ADB_CMD="adb -s ${TARGET_DEVICE} shell"

# 1. Proje kapsamında kapatılan paketleri yeniden etkinleştir
DEBLOATED_PACKAGES=(
    "com.android.printspooler"
    "com.sdt.producttest"
    "com.oem.qa"
    "com.google.android.feedback"
    "com.android.tv.feedbackconsent"
    "com.next.roket"
    "com.google.android.videos"
)

for pkg in "${DEBLOATED_PACKAGES[@]}"; do
    echo "[+] Paket etkinleştiriliyor: ${pkg}"
    ${ADB_CMD} "pm enable --user 0 ${pkg}" 2>/dev/null || echo "    (Uyarı: ${pkg} etkinleştirilemedi veya bulunamadı)"
done

# 2. Orijinal Stok Başlatıcıyı Geri Yükle (Eğer değiştirilmişse)
echo "[+] Stok başlatıcı geri atanıyor (Google TV Launcher)..."
${ADB_CMD} "pm enable --user 0 com.google.android.apps.tv.launcherx" 2>/dev/null || true
${ADB_CMD} "cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity" 2>/dev/null || true
${ADB_CMD} "settings put secure enabled_accessibility_services com.spocky.projengmenu/com.spocky.projengmenu.services.ProjectivyAccessibilityService" 2>/dev/null || true

echo "=== GERİ YÜKLEME TAMAMLANDI ==="
echo "Mevcut aktif HOME:"
${ADB_CMD} "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
