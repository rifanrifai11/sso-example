# 📱 Proyek Android Studio Kotlin - Integrasi Keycloak SSO

[![Kotlin Logo](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Keycloak Logo](https://img.shields.io/badge/Keycloak-EE0000?style=for-the-badge&logo=keycloak&logoColor=white)](https://www.keycloak.org/)

Proyek ini mendemonstrasikan integrasi **Single Sign-On (SSO)** menggunakan **Keycloak** dalam aplikasi Android yang dikembangkan dengan **Kotlin** di **Android Studio**.

| Teknologi | Deskripsi |
| :---: | :--- |
| **Kotlin** | Bahasa pemrograman utama untuk pengembangan aplikasi Android. |
| **Android Studio** | IDE resmi untuk pengembangan Android. |
| **Keycloak** | Solusi Identity and Access Management (IAM) *open source* untuk SSO. |
| **SSO** | Memungkinkan pengguna untuk masuk sekali dan mengakses beberapa aplikasi. |

---

## 🚀 Fitur Utama

* **Otentikasi SSO**: Menggunakan Keycloak untuk mengelola proses login pengguna melalui alur **OAuth 2.0 Authorization Code Flow with PKCE**.
* **Aplikasi Klien Android**: Klien native yang terhubung ke *realm* Keycloak.
* **Penanganan Token**: Mengambil, menyimpan, dan me-refresh token akses (**Access Token**, **ID Token**, **Refresh Token**).
* **Integrasi Pustaka**: Pemanfaatan pustaka **[Nama Pustaka yang Digunakan, contoh: AppAuth]** untuk otentikasi OIDC.

---

## 🛠️ Persyaratan Sistem

Pastikan Anda memiliki hal-hal berikut untuk menjalankan dan mengembangkan proyek ini:

* **Android Studio** versi terbaru.
* **SDK Android** (min. level 21 atau sesuai konfigurasi `build.gradle`).
* **Keycloak Server** yang sudah berjalan dan dikonfigurasi.
* **Java Development Kit (JDK)**.

---

## ⚙️ Konfigurasi Keycloak (Sisi Server)

Sebelum menjalankan aplikasi, Anda harus menyiapkan konfigurasi Keycloak pada server Anda:

1.  **Buat Realm**: Buat *realm* baru (misalnya, `android-realm`).
2.  **Buat Klien**: Daftarkan klien baru (misalnya, `android-app`) dalam *realm* tersebut dengan pengaturan:
    * **Client Protocol**: `openid-connect`
    * **Access Type**: `public`
    * **Standard Flow Enabled**: **ON**
    * **Valid Redirect URIs**: Tambahkan URI *redirect* aplikasi Anda (Contoh: `myapp://callback`).
    * **Web Origins**: `+` (untuk mengizinkan semua CORS saat pengembangan)
3.  **Buat Pengguna**: Buat pengguna uji dalam *realm* tersebut.

---

## 💻 Instalasi & Pengaturan Proyek (Sisi Android)

1.  **Clone Repositori**:
    ```bash
    git clone [URL_Repositori_Anda]
    cd [Nama_Folder_Proyek]
    ```
2.  **Buka di Android Studio**.
3.  **Konfigurasi Parameter Keycloak**:
    Ubah nilai-nilai konfigurasi Keycloak di file **`res/values/strings.xml`** atau konstanta yang sesuai:

    ```xml
    <string name="keycloak_client_id">[ID Klien Anda, contoh: android-app]</string>
    <string name="keycloak_redirect_uri">[URI Redirect Anda, contoh: myapp://callback]</string>
    <string name="keycloak_auth_endpoint">http://[URL_KEYCLOAK]/realms/[NAMA_REALM]/protocol/openid-connect/auth</string>
    <string name="keycloak_token_endpoint">http://[URL_KEYCLOAK]/realms/[NAMA_REALM]/protocol/openid-connect/token</string>
    ```

4.  **Konfigurasi Intent Filter**:
    Pastikan `AndroidManifest.xml` memiliki *Intent Filter* untuk menangani *redirect URI*:

    ```xml
    <activity android:name=".AuthActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.VIEW"/>
            <category android:name="android.intent.category.DEFAULT"/>
            <category android:name="android.intent.category.BROWSABLE"/>
            <data android:scheme="myapp"/>
        </intent-filter>
    </activity>
    ```

5.  **Sync Gradle**: Biarkan Android Studio men-sinkronisasi proyek.

---

## 🔑 Implementasi Kode

Implementasi utama berada di:

* **`build.gradle` (Module)**: Menambahkan dependensi OIDC/OAuth 2.0 (misalnya, `net.openid:appauth`).
* **`AuthManager.kt`**: Logika untuk inisiasi otorisasi, penukaran kode (Code Exchange), dan *refresh token*.
* **`AuthActivity.kt`**: Aktivitas yang menangani hasil *redirect* dari browser/Custom Tab.

### Contoh Logika Login (Sederhana)

```kotlin
// Inisiasi otorisasi
fun performAuthorization(context: Context) {
    // ... Buat AuthorizationRequest dengan CLIENT_ID, REDIRECT_URI, SCOPE, dsb.
    // ... Gunakan AuthorizationService untuk meluncurkan Intent ke Keycloak.
}
