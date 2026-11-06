# Mi Box Modern Launcher — v1.0

Mi Box S 4K (1. nesil) için Android TV ana ekranı. Kotlin ile yazılmıştır.

## Özellikler
- Kumandayla yönetilen 5 sütunlu modern koyu ana ekran
- Yüklü TV uygulamalarını listeleme, başlatma
- Uygulama gizleme/gösterme ve sıra değiştirme (cihazda saklanır)
- Uygulama kaldırma ekranına ve sistem ayarlarına geçiş
- Türkçe saat ve tarih

## APK oluşturma
1. Android Studio (JDK 17) ile bu klasörü açın.
2. Gradle senkronizasyonunu tamamlayın (internet gerekir).
3. **Build > Build APK(s)** seçin.
4. APK: `app/build/outputs/apk/debug/app-debug.apk`.
5. APK'yı USB veya ADB ile Mi Box'a yükleyin: `adb install -r app-debug.apk`.
6. TV'de **Mi Box Modern** uygulamasını açın. İsterseniz Home tuşu için varsayılan başlatıcı olarak seçin; Android sürümüne bağlı olarak bu seçenek görünmeyebilir.

## Sınırlar ve güvenlik
- Bu proje henüz gerçek Mi Box üzerinde test edilmedi; hazır derlenmiş APK içermez.
- Gizleme yalnızca bu launcher'da geçerlidir. Korunan sistem uygulamalarını kaldırmaz.
- Hava durumu dahil değildir; bunun için konum ve bir hava durumu servisi entegrasyonu gerekir.
- Varsayılan launcher değiştirme davranışı cihazın Android TV sürümüne bağlıdır.
- `QUERY_ALL_PACKAGES` cihazda kurulu uygulamaları listelemek için kullanılır; Google Play dağıtımında ek politika incelemesine tabidir.
- Cihazın orijinal ana ekranı kaldırılmaz. Geri dönüş için Ayarlar > Uygulamalar > Varsayılan uygulamalar bölümünü veya launcher varsayılanlarını temizlemeyi deneyin.

## GitHub ile bilgisayara Android Studio kurmadan APK oluşturma
1. GitHub'da yeni **public veya private** depo açın; README oluşturma seçeneğini işaretlemeyin.
2. Bu ZIP içindeki **MiBoxModernLauncher klasörünün içeriğini** (özellikle `.github` gizli klasörü dahil) deponun köküne yükleyin. GitHub web arayüzü gizli klasör yüklemede sorun çıkarırsa GitHub Desktop veya git kullanın.
3. GitHub'da **Actions** sekmesine girin; gerekirse workflows'u etkinleştirin.
4. **APK Olustur** > **Run workflow** > **Run workflow** tıklayın (veya main'e push sonrası otomatik derlemeyi bekleyin).
5. Yeşil tik oluşunca ilgili workflow çalışmasını açın, alttaki **Artifacts** bölümünden **MiBoxModernLauncher-APK** dosyasını indirin.
6. İndirilen artifact ZIP'ini açın: içindeki `app-debug.apk` Mi Box'a yüklenebilir dosyadır.

Not: Derleme GitHub üzerinde yapılır; cihazda test edilmemiştir. Actions kullanım kotası ve internet bağlantısı gerektirir. GitHub'da yüklenen ZIP dosyasını doğrudan depo dosyası olarak koymak yerine içeriğini açıp yükleyin.
