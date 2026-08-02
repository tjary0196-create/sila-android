package com.sila.messaging.data

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

object LocationUtils {
    fun detectCountry(context: Context): String {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val iso = tm?.networkCountryIso?.takeIf { it.isNotBlank() }
                ?: tm?.simCountryIso?.takeIf { it.isNotBlank() }
                ?: Locale.getDefault().country
            if (iso.isBlank()) "" else Locale("", iso.uppercase()).displayCountry
        } catch (e: Exception) {
            Locale.getDefault().country.let { if (it.isBlank()) "" else Locale("", it).displayCountry }
        }
    }
}
