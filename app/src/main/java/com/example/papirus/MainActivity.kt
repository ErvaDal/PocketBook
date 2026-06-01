package com.example.papirus

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.papirus.data.AllBooksResponse
import com.example.papirus.data.ApiResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // Bildirim sayfası yönlendirmesi
        findViewById<ImageView>(R.id.iv_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // BACKEND DATABASE BAĞLANTI TESTİ
        RetrofitClient.api.testDb().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d("API", "SUCCESS: ${response.body()}")
                } else {
                    Log.e("API", "SERVER ERROR")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API", "FAIL: ${t.message}")
            }
        })
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
        val rvMostRead = findViewById<RecyclerView>(R.id.rv_most_read)
        val rvContinueReading = findViewById<RecyclerView>(R.id.rv_continue_reading)
        val rvRecommended = findViewById<RecyclerView>(R.id.rv_recommended)

        // 🚀 1. ADIM: LayoutManager tanımlamalarını kesinlikle ana thread üzerinde netleştiriyoruz:
        rvMostRead.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvContinueReading.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvRecommended.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Sunucudan kitapları talep ediyoruz
        RetrofitClient.api.getAllStories().enqueue(object : Callback<AllBooksResponse> {
            override fun onResponse(call: Call<AllBooksResponse>, response: Response<AllBooksResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val dbBooks = response.body()!!.books

                    // Eğer veritabanından gelen liste boşsa ekranda uyarı verelim (Test etmek için çok önemli)
                    if (dbBooks.isEmpty()) {
                        Toast.makeText(this@MainActivity, "⚠️ Bağlantı başarılı ama veritabanında 'PUBLISHED' durumunda hiç kitap yok!", Toast.LENGTH_LONG).show()
                        return
                    }

                    // 🚀 2. ADIM: Adaptörleri tek tek oluşturuyoruz
                    val mostReadAdapter = NewBookAdapter(dbBooks) { secilenKitap ->
                        val intent = Intent(this@MainActivity, BookDetailsActivity::class.java).apply {
                            putExtra("STORY_ID", secilenKitap.storyId)
                        }
                        startActivity(intent)
                    }

                    val recommendedAdapter = NewBookAdapter(dbBooks.reversed()) { secilenKitap ->
                        val intent = Intent(this@MainActivity, BookDetailsActivity::class.java).apply {
                            putExtra("STORY_ID", secilenKitap.storyId)
                        }
                        startActivity(intent)
                    }

                    // 🚀 3. ADIM: 3 listeye de verileri zorla giydiriyoruz:
                    rvMostRead.adapter = mostReadAdapter
                    rvContinueReading.adapter = mostReadAdapter // Boş kalmasın diye mevcut kitapları buraya da bağlıyoruz
                    rvRecommended.adapter = recommendedAdapter

                    // 🔥 4. ADIM: SİHİRLİ DOKUNUŞ - Android'e listeyi ekrana çizmesi için emir veriyoruz:
                    mostReadAdapter.notifyDataSetChanged()
                    recommendedAdapter.notifyDataSetChanged()

                    Toast.makeText(this@MainActivity, "📚 ${dbBooks.size} adet kitap başarıyla listelendi!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Sunucudan olumsuz yanıt geldi.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AllBooksResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ağ Hatası: Sunucuya erişilemedi! ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // navbar kısmı
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_feed -> {
                    val intent = Intent(this, FeedActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_library -> {
                    val intent = Intent(this, LibraryActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }

                R.id.nav_profile -> {
                    // profil ekranımız olan ProfileActivity'yi açılımı!
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}