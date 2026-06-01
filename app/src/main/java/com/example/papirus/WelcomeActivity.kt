package com.example.papirus

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        // Kenar boşlulalrının ayarlanması için dokunma
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // boşluk ayarı devam
            val density = resources.displayMetrics.density
            val padding32Px = (32 * density).toInt()

            //boşluk ayarı devam
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

            // animasyonlu değişmesi için
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        //yönlendirme için
        val btnWelcomeLogin = findViewById<MaterialButton>(R.id.btn_welcome_login)
        val btnWelcomeRegister = findViewById<MaterialButton>(R.id.btn_welcome_register)

        // Giriş Yap butonuna tıklandığında LoginActivity'ye gider
        btnWelcomeLogin.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        // Kayıt Ol butonuna tıklandığında RegisterActivity'ye gider
        btnWelcomeRegister.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}