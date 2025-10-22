package com.sso.ssopaser

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LogoutCallbackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setelah logout dari Keycloak, arahkan kembali ke LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}