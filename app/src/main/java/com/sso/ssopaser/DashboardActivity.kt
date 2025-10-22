package com.sso.ssopaser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sso.ssopaser.databinding.ActivityDashboardBinding
import com.sso.ssopaser.utility.PrefManager
import com.sso.ssopaser.utility.decodeIdTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    val prefManager = PrefManager.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getData()

        binding.logout.setOnClickListener {
            // Tampilkan dialog konfirmasi
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Apakah kamu yakin ingin logout?")
                .setPositiveButton("Ya") { dialog, _ ->
                    dialog.dismiss()
                    logout() // panggil fungsi logout
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss() // batal logout
                }
                .show()
        }
    }

    private fun getData() {
        val idToken = prefManager.getString("id_token", null)

        if (!idToken.isNullOrEmpty()) {
            val userInfo = decodeIdTokens(idToken)
            userInfo?.let {
                val userId = it.optString("sub") // ID unik user di Keycloak
                val username = it.optString("preferred_username")
                val email = it.optString("email")
                val name = it.optString("name")

                Log.d("USER_INFO", "UserID: $userId : Username: $username : Name: $name : Email: $email")

                binding.idUser.text = userId
                binding.name.text = name
                binding.email.text = email
            }
        }
    }

    private fun logout() {
        // Ambil id_token untuk logout dari Keycloak (optional)
        val idToken = prefManager.getString("id_token", null)
        val refreshToken = prefManager.getString("refresh_token", null)

        if (!idToken.isNullOrEmpty()) {
            lifecycleScope.launch {
                logoutServer(refreshToken.toString())
            }
        }
    }

    private suspend fun logoutServer(refreshToken: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://sso.paserkab.go.id/realms/development/protocol/openid-connect/logout")
                val postData = "client_id=development&refresh_token=$refreshToken"

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    doOutput = true
                    outputStream.write(postData.toByteArray(Charsets.UTF_8))

                    val responseCode = responseCode
                    if (responseCode in 200..299) {
                        prefManager.remove("access_token")
                        prefManager.remove("refresh_token")
                        prefManager.remove("id_token")
                        Log.d("LOGOUT", "Server logout successful")

                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                    } else {
                        Log.e("LOGOUT", "Server logout failed: $responseCode")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}