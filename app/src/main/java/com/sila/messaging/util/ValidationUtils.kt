package com.sila.messaging.util

object ValidationUtils {

    fun validateFullName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("الاسم مطلوب")
            name.length < 2 -> ValidationResult.Error("الاسم قصير جداً (٢ حروف على الأقل)")
            name.length > 60 -> ValidationResult.Error("الاسم طويل جداً (٦٠ حرف كحد أقصى)")
            !name.matches(Regex("^[\u0600-\u06FFa-zA-Z\\s]+$")) -> 
                ValidationResult.Error("الاسم يجب أن يحتوي على حروف فقط")
            else -> ValidationResult.Success
        }
    }

    fun validateUsername(username: String): ValidationResult {
        val clean = username.trim().lowercase()
        return when {
            clean.isBlank() -> ValidationResult.Error("اسم المستخدم مطلوب")
            clean.length < 3 -> ValidationResult.Error("٣ حروف على الأقل")
            clean.length > 20 -> ValidationResult.Error("٢٠ حرف كحد أقصى")
            !clean.matches(Regex("^[a-z0-9_.]+$")) -> 
                ValidationResult.Error("مسموح: حروف إنجليزية، أرقام، _ و . فقط")
            else -> ValidationResult.Success
        }
    }

    fun validateBio(bio: String): ValidationResult {
        return when {
            bio.length > 120 -> ValidationResult.Error("١٢٠ حرف كحد أقصى")
            else -> ValidationResult.Success
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        return when {
            phone.isBlank() -> ValidationResult.Success
            !phone.matches(Regex("^[+]?[0-9]{8,15}$")) -> 
                ValidationResult.Error("رقم هاتف غير صالح")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
