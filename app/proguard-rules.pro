# Sila release ProGuard/R8 rules.
#
# Most of Firebase/Coil/Compose ship their own "consumer" ProGuard rules
# bundled in their AARs, which R8 picks up automatically — the rules below
# are the small, well-known set that commonly still needs to be explicit,
# kept intentionally minimal so shrinking still does real work.

# --- Firebase (Auth / Firestore / Functions / App Check) ---
# Firestore can deserialize documents into POJOs via reflection when a typed
# model class is used with toObject()/@DocumentId; keep the safety net even
# though this app currently maps fields manually.
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class com.sila.messaging.domain.** {
    <fields>;
    <init>(...);
}
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# --- Kotlin coroutines / kotlinx ---
-dontwarn kotlinx.coroutines.**
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# --- Coil (image loading) ---
-dontwarn coil.**

# --- Google Play services / Sign-In ---
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# --- Kotlin metadata (keeps data class componentN/copy reflection-safe) ---
-keepclassmembers class * {
    public <init>(...);
}
-keepattributes InnerClasses,EnclosingMethod
