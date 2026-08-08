package com.sila.messaging.util

/**
 * ⚠️ تنبيه صريح:
 * - التشفير الحقيقي End-to-End يتطلب بروتوكول Signal Protocol (X3DH + Double Ratchet)
 * - هذا يحتاج: توليد مفاتيح على الجهاز، تبادل مفاتيح، Perfect Forward Secrecy
 * - Firebase يوفر تشفير النقل (TLS/HTTPS) فقط — هذا ليس E2EE
 * - لتنفيذ E2EE حقيقي: استخدم مكتبة libsignal-client أو خدمة مثل Stream Chat
 */
object SecurityUtils {
    fun hashDeviceInfo(deviceInfo: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .run {
                update(deviceInfo.toByteArray())
                digest().joinToString("") { "%02x".format(it) }
            }
    }
}
