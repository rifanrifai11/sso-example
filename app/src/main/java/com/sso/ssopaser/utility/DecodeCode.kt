package com.sso.ssopaser.utility

import android.util.Base64
import org.json.JSONObject

fun decodeIdTokens(idToken: String): JSONObject? {
    return try {
        val parts = idToken.split(".")
        if (parts.size != 3) return null
        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
        JSONObject(payload)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}