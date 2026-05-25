package com.example.papirus

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PapirusSettings"
    private val KEY_IS_DARK_MODE = "isDarkMode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        setupWindowInsets()
        setupThemeToggle()
        setupRecyclerViews()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_home
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupThemeToggle() {
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle_main)
        val systemNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)

        ivThemeToggle.setImageResource(if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon)

        ivThemeToggle.setOnClickListener {
            val newMode = !sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)
            sharedPreferences.edit { putBoolean(KEY_IS_DARK_MODE, newMode) }

            if (newMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupRecyclerViews() {
        val dummyBooks = listOf(
            Book("1", "Osmanlı'da Harem", R.drawable.beyaz_logo),
            Book("2", "Yapay Zeka 101", R.drawable.beyaz_logo),
            Book("3", "C# ile Serüven", R.drawable.beyaz_logo)
        )

        val dummyCurrentBooks = listOf(
            CurrentBook("4", "Kotlin Notları", R.drawable.beyaz_logo, 45),
            CurrentBook("5", "Siber Güvenlik", R.drawable.beyaz_logo, 80)
        )

        findViewById<RecyclerView>(R.id.rv_most_read).adapter =
            BookAdapter(dummyBooks) { secilenKitap ->
                Toast.makeText(this, "${secilenKitap.title} seçildi!", Toast.LENGTH_SHORT).show()
            }

        findViewById<RecyclerView>(R.id.rv_continue_reading).adapter =
            CurrentBookAdapter(dummyCurrentBooks) { secilenKitap ->
                Toast.makeText(this, "${secilenKitap.title} okumaya devam et!", Toast.LENGTH_SHORT)
                    .show()
            }

        findViewById<RecyclerView>(R.id.rv_recommended).adapter =
            BookAdapter(dummyBooks.reversed()) { secilenKitap ->
                Toast.makeText(this, "Öneri: ${secilenKitap.title}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java))
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_feed -> true
                R.id.nav_library -> { // 4. Simge: Kütüphaneye Geçiş
                    startActivity(Intent(this, LibraryActivity::class.java))
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}