package com.example.papirus

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.papirus.data.BookDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        findViewById<ImageView>(R.id.iv_back_book_details).setOnClickListener { finish() }

        val storyId = intent.getStringExtra("STORY_ID") ?: ""

        if (storyId.isEmpty()) {
            Toast.makeText(this, "Kitap bilgisi alınamadı!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // İstek başlamadan önce statik XML yazılarını temizleyelim ki kafamız karışmasın
        findViewById<TextView>(R.id.tv_details_title).text = "Yükleniyor..."
        findViewById<TextView>(R.id.tv_details_blurb).text = "Lütfen bekleyin, veriler çekiliyor..."

        loadBookDetails(storyId)
    }

    private fun loadBookDetails(storyId: String) {
        val tvTitle = findViewById<TextView>(R.id.tv_details_title)
        val tvAuthor = findViewById<TextView>(R.id.tv_details_author)
        val tvBlurb = findViewById<TextView>(R.id.tv_details_blurb)
        val rvChapters = findViewById<RecyclerView>(R.id.rv_reader_chapters)
        rvChapters.layoutManager = LinearLayoutManager(this)

        RetrofitClient.api.getBookDetails(storyId).enqueue(object : Callback<BookDetailsResponse> {
            override fun onResponse(call: Call<BookDetailsResponse>, response: Response<BookDetailsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!

                    // Veritabanından gelen GERÇEK dinamik veriler basılıyor!
                    tvTitle.text = data.story.title
                    tvAuthor.text = "Yazar: ${data.story.authorName ?: "Bilinmeyen Yazar"}"
                    tvBlurb.text = data.story.summary

                    // Bölümler RecyclerView'a bağlanıyor
                    rvChapters.adapter = NewReaderChapterAdapter(data.chapters) { tiklananBolum ->
                        val intent = Intent(this@BookDetailsActivity, ReadChapterActivity::class.java).apply {
                            putExtra("STORY_ID", tiklananBolum.storyId)
                            putExtra("CHAPTER_NUMBER", tiklananBolum.chapterNumber)
                        }
                        startActivity(intent)
                    }
                } else {
                    tvTitle.text = "Hata"
                    tvBlurb.text = "İçerik yüklenemedi."
                    Toast.makeText(this@BookDetailsActivity, "Sunucu Hatası: Rota bulunamadı.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<BookDetailsResponse>, t: Throwable) {
                Toast.makeText(this@BookDetailsActivity, "Ağ Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}