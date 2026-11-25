# Delta SMS Forwarder — 0.1.0 TEST
نسخه مستقل اندروید برای آزمایش ارسال نمونه‌های ساختگی به تلگرام. این برنامه پیامک واقعی، رمز واقعی یا اعلان برنامه‌های دیگر را نمی‌خواند. پکیج مستقل ir.delta.smsforwarder.

## Build
GitHub Actions → Build Delta SMS Forwarder demo → Artifacts → Delta-SMS-Forwarder-demo-APK. Extract app-debug.apk.
JDK 17, Android SDK 35, Gradle 8.9. Local: gradle :app:testDebugUnitTest :app:assembleDebug.

## Test
ربات آزمایشی خود را با /start فعال کنید. توکن و Chat ID را فقط در گوشی وارد کنید. ارسال نمونه را فعال کرده و تست اتصال را بزنید. توکن در Android Keystore رمزگذاری می‌شود. برای تست صف، اینترنت را قطع کنید و یک نمونه بفرستید.
این نسخه debug است. آزمون روی گوشی انجام نشده. در صورت قطع ارتباط پس از پذیرش درخواست، تلاش مجدد ممکن است پیام نمونه را تکرار کند. تاریخچه محلی پیام‌های تلگرام را حذف نمی‌کند.
