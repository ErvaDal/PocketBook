package com.example.papirus

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.papirus.data.ApiResponse
import com.example.papirus.data.LoginRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Kenar boşluklarının ayarlanması
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            val padding32Px = (32 * density).toInt()

            v.setPadding(
                systemBars.left + padding32Px,
                systemBars.top + padding32Px,
                systemBars.right + padding32Px,
                systemBars.bottom + padding32Px
            )
            insets
        }

        // Gece/Gündüz Teması Yönetimi
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        if (isDarkMode) {
            ivThemeToggle.setImageResource(R.drawable.ic_sun)
        } else {
            ivThemeToggle.setImageResource(R.drawable.ic_moon)
        }

        ivThemeToggle.setOnClickListener {
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Yönlendirmeler (Kayıt Ol)
        val btnRegisterRedirect = findViewById<TextView>(R.id.btn_login_register_redirect)
        btnRegisterRedirect.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Şifremi Unuttum Sayfasına Yönlendirme
        val tvForgotPassword = findViewById<TextView>(R.id.tv_login_forgot_password)
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // --- BACKEND GİRİŞ BAĞLANTI MANTIĞI ---
        val etEmail = findViewById<TextInputEditText>(R.id.et_Email)
        val etPassword = findViewById<TextInputEditText>(R.id.et_PasswordHash)
        val btnLoginSubmit = findViewById<MaterialButton>(R.id.btn_login_submit)

        btnLoginSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)

            //İSTEK ATAMA
            RetrofitClient.api.login(loginRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@LoginActivity, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()

                        //ID alma
                        val loggedInUserID = response.body()?.user?.id ?: ""
                        val loggedInUsername = response.body()?.user?.username ?: "BilinmeyenOkur"

                        val sharedPref = getSharedPreferences("PapirusSettings", MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("current_user_id", loggedInUserID)
                            putString("current_username", loggedInUsername) // Kullanıcı adını da hafızaya mühürledik!
                            apply()
                        }
                        // Ana ekrana (MainActivity) yönlendir
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = response.body()?.message ?: "Giriş başarısız, bilgileri kontrol edin."
                        Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Ağ Hatası: Sunucuya bağlanılamadı", Toast.LENGTH_SHORT).show()
                    Log.e("LOGIN_ERROR", t.message ?: "Bilinmeyen hata")
                }
            })
        }
    }
}