package com.example.papirus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.papirus.data.ProfileResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ---  SHAREDPREFERENCES İLE OTURUM KONTROLÜ ---
        val sharedPreferences = getSharedPreferences("PapirusSettings", Context.MODE_PRIVATE)
        val loggedInUserId = sharedPreferences.getString("current_user_id", "")

        Log.d("PAPIRUS_PROFIL", "Profil sayfasına giriş yapıldı!")
        Log.d("PAPIRUS_PROFIL", "Hafızadan okunan ID değeri: '$loggedInUserId'")
        if (loggedInUserId.isNullOrEmpty()) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı! Lütfen tekrar giriş yapın.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // ---  TABLAYOUT GEÇİŞ MANTIĞI  ---
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_profile)
        val llTabBooks = findViewById<LinearLayout>(R.id.ll_tab_books)
        val llTabPosts = findViewById<LinearLayout>(R.id.ll_tab_posts)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    llTabBooks.visibility = View.VISIBLE
                    llTabPosts.visibility = View.GONE
                } else {
                    llTabBooks.visibility = View.GONE
                    llTabPosts.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // ---  YENİ KİTAP EKLEME BUTONU ---
        findViewById<ImageView>(R.id.iv_add_new_book).setOnClickListener {
            startActivity(Intent(this, CreateBookActivity::class.java))
        }

        // ---  ALT MENÜ YÖNLENDİRMESİ ---
        setupBottomNavigation()

        // --- ️ GERÇEK VERİTABANI VERİLERİNİ YÜKLEME ---
        loadRealDatabaseProfile(loggedInUserId)
    }

    // 🚀 SAYFA HER ÖNE ÇIKTIĞINDA VERİLERİ CANLI TAZELEME DÖNGÜSÜ:
    override fun onResume() {
        super.onResume()
        // Alt menüdeki profil ikonunu seçili hale getiriyoruz
        findViewById<BottomNavigationView>(R.id.bottom_navigation_profile).selectedItemId = R.id.nav_profile

        // Hafızadan kullanıcı ID'sini tekrar okuyoruz
        val sharedPreferences = getSharedPreferences("PapirusSettings", Context.MODE_PRIVATE)
        val loggedInUserId = sharedPreferences.getString("current_user_id", "")

        // Eğer ID varsa veritabanından güncel kitap listesini anında çekip arayüze basıyoruz
        if (!loggedInUserId.isNullOrEmpty()) {
            loadRealDatabaseProfile(loggedInUserId)
        }
    }

    private fun loadRealDatabaseProfile(userId: String) {
        val tvTopUsername = findViewById<TextView>(R.id.tv_profile_top_username)
        val tvRealName = findViewById<TextView>(R.id.tv_profile_real_name)
        val rvBooks = findViewById<RecyclerView>(R.id.rv_profile_books)
        val rvLibraries = findViewById<RecyclerView>(R.id.rv_profile_libraries)

        rvBooks.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvLibraries.layoutManager = LinearLayoutManager(this)

        RetrofitClient.api.getUserProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val profileData = response.body()!!

                    // 🎯 Sunucudan gelen gerçek veriler tam buraya giydiriliyor:
                    tvTopUsername?.text = "@${profileData.user.username}"
                    tvRealName?.text = "${profileData.user.username}\n(${profileData.user.email})"

                    // Kitaplarım listesi gerçek veritabanı verileriyle besleniyor
                    rvBooks.adapter = BookAdapter(profileData.publishedBooks) { secilenKitap ->
                        val intent = Intent(this@ProfileActivity, ManageBookActivity::class.java).apply {
                            putExtra("STORY_ID", secilenKitap.storyId)
                        }
                        startActivity(intent)
                    }

                    rvLibraries.adapter = UserLibraryAdapter(profileData.publicLibraries) { }

                    Toast.makeText(this@ProfileActivity, "Veriler başarıyla çekildi!", Toast.LENGTH_SHORT).show()

                } else {
                    val serverCode = response.code()
                    val errorMsg = response.errorBody()?.string() ?: "Bilinmeyen sunucu hatası"
                    Toast.makeText(this@ProfileActivity, "Sunucu Hatası ($serverCode): $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Ağ/Bağlantı Hatası: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_profile)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP })
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP })
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_feed -> {
                    startActivity(Intent(this, FeedActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP })
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_library -> {
                    startActivity(Intent(this, LibraryActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP })
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}