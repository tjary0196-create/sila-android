# Sila Messenger

تطبيق مراسلة آمن مبني بـ Kotlin + Jetpack Compose + Firebase.

## المتطلبات
- Android Studio Hedgehog (2023.1.1) أو أحدث
- JDK 17
- Firebase Project مفعّل

## الإعداد

1. أنشئ مشروع Firebase جديد
2. أضف تطبيق Android بـ Package Name: `com.sila.messaging`
3. حمّل `google-services.json` إلى مجلد `app/`
4. أضف `FIREBASE_WEB_CLIENT_ID` إلى `local.properties`
5. شغّل `./gradlew assembleDebug`

## الميزات المُنفَّذة
- ✅ تسجيل الدخول بـ Google (Credential Manager)
- ✅ Onboarding كامل (صورة، اسم، username، bio، لغة)
- ✅ قائمة المحادثات مع تبويبات
- ✅ طلبات الرسائل (قبول/رفض)
- ✅ شاشة المحادثة مع نص/صور/ملفات
- ✅ ردود (Reply) وتوجيه (Forward)
- ✅ حذف عندي / للجميع
- ✅ Reactions (Emoji)
- ✅ ملف شخصي + تعديل
- ✅ عرض ملف مستخدم آخر + حظر/إبلاغ
- ✅ إعدادات + خصوصية + محظورون + أجهزة نشطة
- ✅ Firestore Security Rules
- ✅ Firebase Cloud Functions
- ✅ FCM Notifications (جاهز للتفعيل)

## الميزات القادمة
- 🔄 المكالمات الصوتية/الفيديو (WebRTC/Agora)
- 🔄 E2EE حقيقي (Signal Protocol)
- 🔄 مكالمات الفيديو

## الأمان
- لا يوجد مفتاح API في الكود
- Firestore Rules صارمة
- App Check مفعّل
