package com.sso.ssopaser

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sso.ssopaser.utility.PrefManager
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private lateinit var authService: AuthorizationService
    private lateinit var authRequest: AuthorizationRequest

    private lateinit var prefManager: PrefManager

    private val clientId = "development_mobile"
    private val realms = "development"
    private val redirectUri = Uri.parse("com.sso.ssopaser:/oauth2redirect")
    private val authEndpoint = Uri.parse("https://sso.paserkab.go.id/realms/$realms/protocol/openid-connect/auth")
    private val tokenEndpoint = Uri.parse("https://sso.paserkab.go.id/realms/$realms/protocol/openid-connect/token")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        prefManager = PrefManager.getInstance(this)

        val idToken = prefManager.getString("id_token", null)
        val refreshToken = prefManager.getString("refresh_token", null)

        // Sudah ada token, langsung ke Dashboard
        if (!refreshToken.isNullOrEmpty() && !idToken.isNullOrEmpty()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            startLoginFlow()
        }
    }

    private fun startLoginFlow() {
        //konfigurasi OIDC dari Keycloak
        val serviceConfig = AuthorizationServiceConfiguration(authEndpoint, tokenEndpoint)

        //Buat request authorization
        authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri)
            .setScope("openid profile email")
            .build()

        authService = AuthorizationService(this)

        //proses login (launch browser)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, AUTH_REQUEST_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTH_REQUEST_CODE) {
            handleAuthResponse(data)
        }
    }

    private fun handleAuthResponse(intent: Intent?) {

        val resp = AuthorizationResponse.fromIntent(intent!!)
        val ex = AuthorizationException.fromIntent(intent)

        if (resp != null) {
            //Tukar authorization code → token
            val tokenRequest = resp.createTokenExchangeRequest()
            authService.performTokenRequest(tokenRequest) { tokenResponse, tokenEx ->
                runOnUiThread {
                    if (tokenResponse != null) {
                        // Export all response
                        // val gson = GsonBuilder().setPrettyPrinting().create()
                        // Log.d("AuthDebug", gson.toJson(tokenResponse))

                        val accessToken = tokenResponse.accessToken
                        val refreshToken = tokenResponse.refreshToken
                        val idToken = tokenResponse.idToken

                        Log.d("SSO-CHECK", "Check Access Token: $accessToken")
                        Log.d("SSO-CHECK", "Check Refresh Token: $refreshToken")
                        Log.d("SSO-CHECK", "Check ID Token: $idToken")

                        prefManager.saveTokens(accessToken, refreshToken, idToken)

                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Log.e("SSO-CHECK", "Token exchange failed", tokenEx)
                    }
                }
            }
        } else {
            Log.e("SSO-CHECK", "Login gagal: ${ex?.errorDescription}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::authService.isInitialized) {
            authService.dispose()
        }
    }

    companion object {
        private const val AUTH_REQUEST_CODE = 1001
    }
}