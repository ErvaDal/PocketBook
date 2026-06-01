package com.example.papirus

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

// Yazar için güncellenmiş Bölüm Veri Modeli
data class AuthorChapter(
    val title: String,
    val isPublished: Boolean,
    val reads: String,
    val likes: String,
    val comments: String
)

class ManageBookActivity : AppCompatActivity() {

    private var isBookPublished = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_book)

        findViewById<ImageView>(R.id.iv_back_manage).setOnClickListener { finish() }

        // TEMA AYARI
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle_manage)
        val isNightMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        if (isNightMode) ivThemeToggle.setImageResource(R.drawable.ic_sun) else ivThemeToggle.setImageResource(R.drawable.ic_moon)

        ivThemeToggle.setOnClickListener {
            if (isNightMode) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        // YAYINDAN KALDIR / YAYINLA
        val btnPublishToggle = findViewById<MaterialButton>(R.id.btn_unpublish_book)
        btnPublishToggle.setOnClickListener {
            isBookPublished = !isBookPublished
            if (isBookPublished) {
                btnPublishToggle.text = "Yayından Kaldır"
                Toast.makeText(this, "Kitap tekrar okuyuculara açıldı!", Toast.LENGTH_SHORT).show()
            } else {
                btnPublishToggle.text = "Yayınla"
                Toast.makeText(this, "Kitap yayından kaldırıldı (Taslak oldu).", Toast.LENGTH_SHORT).show()
            }
        }

        // KİTABI SİL
        findViewById<MaterialButton>(R.id.btn_delete_book).setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Emin misin?")
                .setMessage("Bu kitabı silmek istediğinden emin misin? Bu işlemi geri alamazsın.")
                .setPositiveButton("Evet, Sil") { _, _ ->
                    Toast.makeText(this, "Kitap tamamen silindi.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        findViewById<ImageView>(R.id.iv_add_chapter).setOnClickListener {
            startActivity(Intent(this, WriteChapterActivity::class.java))
        }

        setupChapters()
    }


    //sahte veri
    private fun setupChapters() {
        val rvChapters = findViewById<RecyclerView>(R.id.rv_chapters)

        // Yeni istatistik verileri eklendi
        val chapters = listOf(
            AuthorChapter("1. Bölüm: Yıkım", true, "4.5K", "1.2K", "84"),
            AuthorChapter("2. Bölüm: Sırlar", false, "0", "0", "0") // Taslak olduğu için sıfır
        )

        rvChapters.adapter = ChapterAdapter(chapters,
            onChapterClick = { tiklananBolum ->
                val intent = Intent(this, WriteChapterActivity::class.java)
                intent.putExtra("IS_PUBLISHED", tiklananBolum.isPublished)
                startActivity(intent)
            },
            onOptionsClick = {
                Toast.makeText(this, "Bölüm ayarları açıldı.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}