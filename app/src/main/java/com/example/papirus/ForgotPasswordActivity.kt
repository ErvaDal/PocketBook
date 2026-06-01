package com.example.papirus



import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.example.papirus.data.ApiResponse
import com.example.papirus.data.ForgotPasswordRequest
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        // kenar boşluğu için
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())


            val density = resources.displayMetrics.density
            val padding32Px = (32 * density).toInt()

            // boşluk devam
            v.setPadding(
                systemBars.left + padding32Px,
                systemBars.top + padding32Px,
                systemBars.right + padding32Px,
                systemBars.bottom + padding32Px
            )
            insets
        }

        // tema değiştirme
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

            // animasyonlu değiştirme için
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        val btnLoginRedirect = findViewById<TextView>(R.id.btn_forgot_password_login_redirect)
        btnLoginRedirect.setOnClickListener {
            val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // --- BACKEND ŞİFRE SIFIRLAMA MANTIĞI ---
        val etEmail = findViewById<TextInputEditText>(R.id.et_Email)
        val btnSubmit = findViewById<MaterialButton>(R.id.btn_forgot_password_submit)

        btnSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Lütfen e-posta adresinizi girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Backend'e gidecek veri modelini oluşturuyoruz
            val request = com.example.papirus.data.ForgotPasswordRequest(email)

            // İstek atma süreci
            RetrofitClient.api.forgotPassword(request).enqueue(object : retrofit2.Callback<com.example.papirus.data.ApiResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.example.papirus.data.ApiResponse>,
                    response: retrofit2.Response<com.example.papirus.data.ApiResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {

                        Toast.makeText(this@ForgotPasswordActivity, response.body()?.message ?: "Sıfırlama kodu gönderildi", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "E-posta bulunamadı veya bir hata oluştu.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.example.papirus.data.ApiResponse>, t: Throwable) {
                    Toast.makeText(this@ForgotPasswordActivity, "Bağlantı hatası!", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }
}