# Sila — Android (Kotlin + Jetpack Compose) — Phase 1

تطبيق مراسلة Android Native، مبني بـ Kotlin و Jetpack Compose، متصل بـ Firebase (Authentication: Google Sign-In, Firestore).

## ✅ إصلاحات تمت على هذا المشروع
- إزالة الاعتماد على Gradle Version Catalog (كان ناقص `gradle/libs.versions.toml`) واستبداله بتصريح مباشر للمكتبات بنفس الإصدارات بملفي `build.gradle.kts` (root و app) عشان يصير المشروع متجانس وقابل للبناء.
- إضافة كل مكتبات Firebase الناقصة (`firebase-bom`, `firebase-auth-ktx`, `firebase-firestore-ktx`, `firebase-functions-ktx`, `firebase-appcheck-playintegrity`, `firebase-appcheck-debug`) و `kotlinx-coroutines-play-services` (لازمة لـ `.await()`).
- تفعيل وتطبيق بلجن `com.google.gms.google-services` بملف `app/build.gradle.kts` (كان معرّف بس مش مطبّق).
- تفعيل `buildFeatures.buildConfig = true` (لازم عشان `BuildConfig.DEBUG` المستخدم بـ `SilaApp.kt`).
- تسجيل `SilaApp` كـ Application class بـ `AndroidManifest.xml` (`android:name=".messaging.SilaApp"`) — بدونها الـ Firebase/App Check ما كانوا رح يشتغلوا أبدًا.
- إضافة الكلاسات الناقصة `ChatMessage` و `ChatSummary` بـ `domain/chat/ChatRepository.kt` — كانوا مستخدمين بالكود بس غير معرّفين، وهاد كان رح يوقف الـ compile فورًا.
- إضافة `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.7).
- إضافة `app/google-services.json` **placeholder** (قيم وهمية بس بنفس الشكل الصحيح JSON) — **لازم تستبدله بالملف الحقيقي من Firebase Console قبل البناء.**

## ✅ تحديث (بعد ربط Firebase الفعلي)
- استبدلت الملف الوهمي بـ `google-services.json` **الحقيقي** اللي حمّلته من Firebase Console (مشروع `sila-messaging`).
- لاحظت إن التطبيق المسجَّل بـ Firebase كان بحزمة (package name) هي `com.sila.messaging`، بينما المشروع كان معرّف بـ `applicationId = "com.sila"` — هاد كان رح يسبب خطأ عند البناء ("No matching client found for package name"). صلّحته وصار `applicationId = "com.sila.messaging"` بملف `app/build.gradle.kts` (الـ `namespace` بقي `com.sila` لأنه هيك متطابق مع بنية حزم الكوتلن الموجودة بالمشروع، وهاد مسموح ومش لازم يكونوا متطابقين).
- بصمة SHA-1 اللي ضفتها بـ Firebase Console (`22:39:b7:d3:bd:bd:49:fe:bb:36:6b:ce:c9:d8:37:05:2a:00:f4:ac`) متطابقة مع اللي بملف `google-services.json`، فتسجيل الدخول بـ Google جاهز من هالناحية.

## ⚠️ خطوة وحيدة باقية
**ولّد ملفات Gradle Wrapper الكاملة** (`gradlew`, `gradlew.bat`, و `gradle-wrapper.jar`) — ما قدرت أعملها من هون لعدم وجود اتصال إنترنت. بتشغيل الأمر التالي مرة وحدة بجذر المشروع (لازم يكون عندك Gradle مثبت محليًا، أو ببساطة افتح المشروع بـ Android Studio وهو رح يعرضلك يولّدهم تلقائيًا أول ما تفتحه):
```
gradle wrapper --gradle-version 8.7
```

## الإعداد المحلي
1. ✅ `google-services.json` موجود بالفعل بـ `app/google-services.json` (الملف الحقيقي).
2. أنشئ ملف `local.properties` في جذر المشروع وأضف:
