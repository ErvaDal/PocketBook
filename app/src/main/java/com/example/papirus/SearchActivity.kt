package com.example.papirus

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.papirus.data.AllBooksResponse
import com.example.papirus.data.DBBook
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PapirusSettings"
    private val KEY_IS_DARK_MODE = "isDarkMode"

    private lateinit var rvSearchResults: RecyclerView
    private lateinit var rvSearchHistory: RecyclerView
    private lateinit var llHistory: LinearLayout
    private lateinit var llResults: LinearLayout

    // Arama geçmişini yerel bir listede simüle edip canlı tutuyoruz
    private val historyList = mutableListOf<DBBook>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // UI Elemanları Eşleştirme
        rvSearchResults = findViewById(R.id.rv_search_results)
        rvSearchHistory = findViewById(R.id.rv_search_history)
        llHistory = findViewById(R.id.ll_search_history)
        llResults = findViewById(R.id.ll_search_results)

        setupWindowInsets()
        setupThemeToggle()
        setupSearchLogic()
        setupAIBubble()
        setupBottomNavigation()
        setupRecyclerViews()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation_search).selectedItemId = R.id.nav_search
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupThemeToggle() {
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle_search)
        val systemNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)

        ivThemeToggle.setImageResource(if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon)

        ivThemeToggle.setOnClickListener {
            val newMode = !sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)
            sharedPreferences.edit { putBoolean(KEY_IS_DARK_MODE, newMode) }

            if (newMode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupRecyclerViews() {
        // RecyclerView yönlerini yatay olarak yapılandırıyoruz
        rvSearchHistory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvSearchResults.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Geçmiş temizleme butonu
        findViewById<TextView>(R.id.tv_clear_history).setOnClickListener {
            historyList.clear()
            rvSearchHistory.adapter = NewBookAdapter(historyList) {}
            llHistory.visibility = View.GONE
            Toast.makeText(this, "Arama geçmişi temizlendi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchLogic() {
        val etSearchInput = findViewById<EditText>(R.id.et_search_input)

        etSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()

                if (query.isEmpty()) {
                    // Kelime silindiyse geçmiş paneline dön
                    llHistory.visibility = if (historyList.isNotEmpty()) View.VISIBLE else View.GONE
                    llResults.visibility = View.GONE
                } else {
                    llHistory.visibility = View.GONE
                    llResults.visibility = View.VISIBLE

                    // 🚀 GERÇEK ZAMANLI VERİTABANI SORGUSU TETİKLENİYOR
                    fetchLiveSearchResults(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchLiveSearchResults(query: String) {
        RetrofitClient.api.searchStories(query).enqueue(object : Callback<AllBooksResponse> {
            override fun onResponse(call: Call<AllBooksResponse>, response: Response<AllBooksResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val matchingBooks = response.body()!!.books

                    // 🚀 Canlı verileri daha önce yazdığımız NewBookAdapter'e bağlayıp listeliyoruz!
                    val searchAdapter = NewBookAdapter(matchingBooks) { secilenKitap ->
                        // Bir kitaba tıklanırsa önce arama geçmişine ekleyelim:
                        if (!historyList.contains(secilenKitap)) {
                            historyList.add(0, secilenKitap) // En başa ekle
                            rvSearchHistory.adapter = NewBookAdapter(historyList) { gecmisKitap ->
                                openBookDetails(gecmisKitap)
                            }
                        }
                        // Sonra detay sayfasına pasla
                        openBookDetails(secilenKitap)
                    }
                    rvSearchResults.adapter = searchAdapter
                }
            }

            override fun onFailure(call: Call<AllBooksResponse>, t: Throwable) {
                // Canlı arama esnasında ağ hatalarını loglayabiliriz
            }
        })
    }

    private fun openBookDetails(book: DBBook) {
        val intent = Intent(this, BookDetailsActivity::class.java).apply {
            putExtra("STATUS_LOG", true) // Takip için log
            putExtra("STORY_ID", book.storyId)
        }
        startActivity(intent)
    }

    private fun setupAIBubble() {
        val cvAiBubble = findViewById<MaterialCardView>(R.id.cv_ai_bubble)
        cvAiBubble.setOnClickListener {
            Toast.makeText(this, "AI Öneri asistanı aktif!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_search)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_search -> true
                R.id.nav_library -> {
                    startActivity(Intent(this, LibraryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}