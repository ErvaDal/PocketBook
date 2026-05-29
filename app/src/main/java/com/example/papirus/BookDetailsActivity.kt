package com.example.papirus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView

data class ReaderChapter(val title: String, val reads: String, val likes: String)

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "PapirusSettings"
    private val KEY_IS_DARK_MODE = "isDarkMode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        findViewById<ImageView>(R.id.iv_back_book_details).setOnClickListener { finish() }

        // Yazar adına tıklama
        findViewById<TextView>(R.id.tv_details_author).setOnClickListener {
            val intent = Intent(this, AuthorProfileActivity::class.java)
            startActivity(intent)
        }

        // --- AY / GÜNEŞ TEMA AYARI MANTIĞI ---
        setupThemeToggle()

        // Bölümleri listele
        setupChapters()
    }

    private fun setupThemeToggle() {
        val ivThemeToggle = findViewById<ImageView>(R.id.iv_theme_toggle_details)
        val systemNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)

        // Başlangıçta ikonun ne olacağını seç
        ivThemeToggle.setImageResource(if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon)

        ivThemeToggle.setOnClickListener {
            val newMode = !sharedPreferences.getBoolean(KEY_IS_DARK_MODE, systemNightMode)
            sharedPreferences.edit { putBoolean(KEY_IS_DARK_MODE, newMode) }

            if (newMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupChapters() {
        val rvChapters = findViewById<RecyclerView>(R.id.rv_reader_chapters)

        val publishedChapters = listOf(
            ReaderChapter("1. Bölüm: Yıkım", "4.5K", "1.2K"),
            ReaderChapter("2. Bölüm: Uyanış", "3.2K", "950"),
            ReaderChapter("3. Bölüm: Mars'a Yolculuk", "2.8K", "820"),
            ReaderChapter("4. Bölüm: Kayıp Koloni", "1.1K", "410")
        )

        rvChapters.adapter = ReaderChapterAdapter(publishedChapters) { tiklananBolum ->
            val intent = Intent(this, ReadChapterActivity::class.java)
            intent.putExtra("CHAPTER_TITLE", tiklananBolum.title)
            startActivity(intent)
        }
    }
}

// ADAPTÖR
class ReaderChapterAdapter(
    private val chapterList: List<ReaderChapter>,
    private val onChapterClick: (ReaderChapter) -> Unit
) : RecyclerView.Adapter<ReaderChapterAdapter.ReaderViewHolder>() {

    class ReaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_reader_chapter_title)
        val tvStats: TextView = view.findViewById(R.id.tv_reader_chapter_stats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reader_chapter, parent, false)
        return ReaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReaderViewHolder, position: Int) {
        val chapter = chapterList[position]
        holder.tvTitle.text = chapter.title
        holder.tvStats.text = "👁 ${chapter.reads}  |  ❤ ${chapter.likes}"

        holder.itemView.setOnClickListener { onChapterClick(chapter) }
    }

    override fun getItemCount(): Int = chapterList.size
}