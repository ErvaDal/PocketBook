package com.example.papirus

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // kanar boşluğu için
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
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


        val btnRegisterRedirect = findViewById<TextView>(R.id.btn_login_register_redirect)

        btnRegisterRedirect.setOnClickListener {
            // Yeni bir aktivite başlatma niyeti (Intent) oluşturulur
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        val tvForgotPassword = findViewById<TextView>(R.id.tv_login_forgot_password)
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}