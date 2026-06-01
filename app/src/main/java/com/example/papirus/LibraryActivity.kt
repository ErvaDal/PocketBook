package com.example.papirus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial

class LibraryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PapirusSettings"
    private val KEY_IS_DARK_MODE = "isDarkMode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_library)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        setupWindowInsets()
        setupThemeToggle()
        setupRecyclerViews()
        setupAddLibraryButton()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation_library).selectedItemId = R.id.nav_library
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_library)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupThemeToggle() {
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle_library)
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
        val dummyLibraries = mutableListOf(
            UserLibrary("1", "Favorilerim", 4, false),
            UserLibrary("2", "Okunacaklar", 2, true),
            UserLibrary("3", "Bilim Kurgu Arşivi", 6, false)
        )

        val dummyCurrentBooks = listOf(
            CurrentBook("4", "Kotlin Notları", R.drawable.beyaz_logo, 45),
            CurrentBook("5", "Siber Güvenlik", R.drawable.beyaz_logo, 80),
            CurrentBook("6", "Mobil Geliştirme Temelleri", R.drawable.beyaz_logo, 12)
        )

        val rvLibraries = findViewById<RecyclerView>(R.id.rv_user_libraries)

        val libraryAdapter = UserLibraryAdapter(dummyLibraries) { secilenKutuphane ->

            val bottomSheetDialog = BottomSheetDialog(this@LibraryActivity)
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_library_details, null)
            bottomSheetDialog.setContentView(bottomSheetView)

            val tvTitle = bottomSheetView.findViewById<TextView>(R.id.tv_bs_library_title)
            val tvPrivacy = bottomSheetView.findViewById<TextView>(R.id.tv_bs_library_privacy)
            val switchPrivacy = bottomSheetView.findViewById<SwitchMaterial>(R.id.switch_privacy)
            val rvBooks = bottomSheetView.findViewById<RecyclerView>(R.id.rv_bs_library_books)

            tvTitle.text = secilenKutuphane.libraryName
            tvPrivacy.text = if (secilenKutuphane.isPublic) "Herkese Açık Liste" else "Gizli Liste (Sadece Sen Görebilirsin)"
            switchPrivacy.isChecked = secilenKutuphane.isPublic

            switchPrivacy.setOnCheckedChangeListener { _, isChecked ->
                secilenKutuphane.isPublic = isChecked
                tvPrivacy.text = if (isChecked) "Herkese Açık Liste" else "Gizli Liste (Sadece Sen Görebilirsin)"

                rvLibraries.adapter?.notifyDataSetChanged()
                Toast.makeText(this@LibraryActivity, "Liste gizliliği güncellendi!", Toast.LENGTH_SHORT).show()
            }

            val libraryBooks = listOf(
                Book("101", "Uzay Serüveni", R.drawable.beyaz_logo, false),
                Book("102", "Zaman Makinesi", R.drawable.beyaz_logo, false),
                Book("103", "Yapay Zeka Macerası", R.drawable.beyaz_logo, false),
                Book("104", "Kara Delik", R.drawable.beyaz_logo, false)
            )

            rvBooks.layoutManager = LinearLayoutManager(this@LibraryActivity)

            rvBooks.adapter = BookVerticalAdapter(libraryBooks) { secilenKitap ->
                Toast.makeText(this@LibraryActivity, "${secilenKitap.title} açılıyor...", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        rvLibraries.adapter = libraryAdapter

        val rvContinue = findViewById<RecyclerView>(R.id.rv_library_continue_reading)
        rvContinue.adapter = CurrentBookAdapter(dummyCurrentBooks) { secilenKitap ->
            Toast.makeText(this, "Okumaya devam et: ${secilenKitap.title}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAddLibraryButton() {
        val btnAdd = findViewById<ImageView>(R.id.iv_add_library)
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Yeni Kütüphane Oluşturma Penceresi Açılacak", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_library)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_feed -> {
                    // İŞTE BURAYI GÜNCELLEDİK! Artık Feed sayfasına gidecek.
                    val intent = Intent(this, FeedActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_library -> true
                else -> false
            }
        }
    }
}