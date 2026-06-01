package com.example.papirus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. Menü ve UI bağlantıları
        setupBottomNavigation()
        setupTabs()
        loadBooks()
        loadPosts()

        // 2. Kitap Oluşturma (Bağlantı)
        findViewById<ImageView>(R.id.iv_add_new_book).setOnClickListener {
            startActivity(Intent(this, CreateBookActivity::class.java))
        }

        // 3. Ayarlar (Tıklama örneği)
        findViewById<ImageView>(R.id.iv_settings).setOnClickListener {
            // SettingsActivity sınıfını oluşturduğunda buraya bağlayabilirsin
            // startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_profile)

        // Tasarımın Feed ile aynı görünmesi için:
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_UNLABELED

        bottomNav.setOnItemSelectedListener { item ->
            // Zaten bu sayfadaysak hiçbir şey yapma
            if (item.itemId == bottomNav.selectedItemId) return@setOnItemSelectedListener true

            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                R.id.nav_feed -> {
                    startActivity(Intent(this, FeedActivity::class.java))
                    finish()
                }
                R.id.nav_library -> {
                    startActivity(Intent(this, LibraryActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    // Zaten buradayız
                }
            }
            true
        }
    }

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_profile)
        val llPosts = findViewById<LinearLayout>(R.id.ll_tab_posts)
        val llBooks = findViewById<LinearLayout>(R.id.ll_tab_books)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    llBooks.visibility = View.VISIBLE
                    llPosts.visibility = View.GONE
                } else {
                    llBooks.visibility = View.GONE
                    llPosts.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadBooks() {
        val rvBooks = findViewById<RecyclerView>(R.id.rv_profile_books)
        val rvLibraries = findViewById<RecyclerView>(R.id.rv_profile_libraries)

        // Burada Adapter'larını tanımla
        rvBooks.adapter = BookVerticalAdapter(listOf()) { book ->
            val intent = Intent(this, ManageBookActivity::class.java)
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            startActivity(intent)
        }
        rvLibraries.adapter = BookVerticalAdapter(listOf()) { }
    }

    private fun loadPosts() {
        val rvPosts = findViewById<RecyclerView>(R.id.rv_profile_posts)
        // Örnek veri ile doldur
        val myPosts = listOf(
            Post("101", "@benim_hesabim", R.drawable.ic_person, "Profilim!", null, "1g", 15, 2)
        )
        rvPosts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvPosts.adapter = PostAdapter(myPosts) { }
    }
}