package com.example.papirus

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.papirus.data.ApiResponse
import com.example.papirus.data.LikeRequest
import com.example.papirus.data.SingleChapterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReadChapterActivity : AppCompatActivity() {

    private lateinit var storyId: String
    private var chapterNumber: Int = 1

    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var tvLikeCount: TextView
    private lateinit var llLikeButton: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_chapter)

        // Üst bar ve geri butonu işlemleri
        findViewById<ImageView>(R.id.iv_back_read).setOnClickListener { finish() }

        tvTitle = findViewById(R.id.tv_read_chapter_title)
        tvContent = findViewById(R.id.tv_read_content)
        tvLikeCount = findViewById(R.id.tv_like_count)
        llLikeButton = findViewById(R.id.ll_like_button)

        // Gelen parametreleri teslim alıyoruz
        storyId = intent.getStringExtra("STORY_ID") ?: ""
        chapterNumber = intent.getIntExtra("CHAPTER_NUMBER", 1)

        tvTitle.text = "$chapterNumber. Bölüm"

        // Veritabanından gerçek bölüm içeriğini çekiyoruz
        loadChapterContent()

        // 🔥 BEĞENİ BUTONUNA TIKLAMA AKSİYONU BURADA BAŞLIYOR:
        llLikeButton.setOnClickListener {
            // 1. SharedPreferences'tan az önce kaydettiğimiz gerçek kullanıcı adını okuyoruz
            val sharedPreferences = getSharedPreferences("PapirusSettings", Context.MODE_PRIVATE)
            val currentLikerUsername = sharedPreferences.getString("current_username", "BilinmeyenOkur") ?: "BilinmeyenOkur"

            // 2. LikeRequest paketimizi hazırlıyoruz
            val likeRequest = LikeRequest(
                storyId = storyId,
                chapterNumber = chapterNumber,
                likerUsername = currentLikerUsername
            )

            // 3. Retrofit köprüsü üzerinden Node.js API'sine isteği uçuruyoruz
            RetrofitClient.api.likeChapter(likeRequest).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ReadChapterActivity, "Bölümü beğendiniz! ❤️", Toast.LENGTH_SHORT).show()

                        // 4. Arayüzdeki beğeni sayısını canlı olarak 1 arttırıp tazeleyelim
                        val currentLikes = tvLikeCount.text.toString().toIntOrNull() ?: 0
                        tvLikeCount.text = (currentLikes + 1).toString()
                    } else {
                        Toast.makeText(this@ReadChapterActivity, "Beğeni işlenemedi.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@ReadChapterActivity, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // İleri ve Geri butonları mantığı
        findViewById<TextView>(R.id.tv_prev_chapter).setOnClickListener {
            if (chapterNumber > 1) {
                chapterNumber--
                tvTitle.text = "$chapterNumber. Bölüm"
                loadChapterContent()
            }
        }

        findViewById<TextView>(R.id.tv_next_chapter).setOnClickListener {
            chapterNumber++
            tvTitle.text = "$chapterNumber. Bölüm"
            loadChapterContent()
        }
    }

    private fun loadChapterContent() {
        tvContent.text = "Bölüm yükleniyor..."

        RetrofitClient.api.getChapterContent(storyId, chapterNumber).enqueue(object : Callback<SingleChapterResponse> {
            override fun onResponse(call: Call<SingleChapterResponse>, response: Response<SingleChapterResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val chapterData = response.body()!!.chapter

                    if (chapterData.content != null) {
                        tvContent.text = Html.fromHtml(chapterData.content, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        tvContent.text = "Bu bölümün içeriği boş."
                    }

                    // Beğeni sayısını ekrana basıyoruz
                    tvLikeCount.text = chapterData.starCount.toString()
                } else {
                    tvContent.text = "Bölüm içeriği getirilemedi. Belki de bu son bölümdür!"
                }
            }

            override fun onFailure(call: Call<SingleChapterResponse>, t: Throwable) {
                Toast.makeText(this@ReadChapterActivity, "Bağlantı Hatası: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}