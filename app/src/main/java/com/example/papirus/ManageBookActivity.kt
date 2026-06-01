package com.example.papirus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.papirus.data.ApiResponse
import com.example.papirus.data.BookDetailsResponse
import com.example.papirus.data.DBChapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageBookActivity : AppCompatActivity() {

    private lateinit var storyId: String
    private lateinit var etSummary: EditText
    private lateinit var btnPublishToggle: MaterialButton
    private lateinit var rvChapters: RecyclerView
    private lateinit var cvBookCover: MaterialCardView
    private lateinit var ivBookCover: ImageView

    private var currentStatus = "PUBLISHED"
    private var currentCoverUrl: String? = null // Kapak URL'sini tutmak için ekledik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_book)

        // Profil sayfasından tıklanan kitabın veritabanı ID'sini yakalıyoruz
        storyId = intent.getStringExtra("STORY_ID") ?: ""

        etSummary = findViewById(R.id.et_manage_book_summary)
        btnPublishToggle = findViewById(R.id.btn_unpublish_book)
        rvChapters = findViewById(R.id.rv_chapters)

        // 🚀 Tasarımdaki Kapak elemanları kod yapısını bozmadan bağlandı:
        cvBookCover = findViewById(R.id.cv_manage_book_cover)
        ivBookCover = findViewById(R.id.iv_manage_book_cover)

        rvChapters.layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.iv_back_manage).setOnClickListener { finish() }

        // 🚀 1. VERİLERİ VERİTABANINDAN ÇEKME
        loadBookDataFromServer()

        // 🚀 2. DURUM GÜNCELLEME (YAYINLA / YAYINDAN KALDIR)
        btnPublishToggle.setOnClickListener {
            val newStatus = if (currentStatus == "PUBLISHED") "DRAFT" else "PUBLISHED"
            val body = mapOf("status" to newStatus)

            RetrofitClient.api.updateBookStatus(storyId, body).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        currentStatus = newStatus
                        updateStatusButtonUI()
                        Toast.makeText(this@ManageBookActivity, "Kitap durumu güncellendi!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {}
            })
        }

        // 🚀 KAPAK GÖRSELİ DEĞİŞTİRME MANTIĞI
        cvBookCover.setOnClickListener {
            val inputEditText = EditText(this).apply {
                hint = "https://example.com/resim.jpg"
                setText(currentCoverUrl ?: "")
                setPadding(32, 32, 32, 32)
            }

            AlertDialog.Builder(this)
                .setTitle("Kapak Görselini Güncelle")
                .setMessage("Lütfen kitabınız için yeni bir kapak resmi URL'si girin:")
                .setView(inputEditText)
                .setPositiveButton("Güncelle") { _, _ ->
                    val newUrl = inputEditText.text.toString().trim()
                    val currentSummary = etSummary.text.toString().trim()

                    val body = mapOf("summary" to currentSummary, "coverImageUrl" to newUrl)
                    RetrofitClient.api.updateBookDetails(storyId, body).enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                currentCoverUrl = newUrl
                                Toast.makeText(this@ManageBookActivity, "Kapak görseli başarıyla güncellendi! 🎨", Toast.LENGTH_SHORT).show()
                                ivBookCover.setImageResource(android.R.drawable.ic_menu_gallery)
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {}
                    })
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // 🚀 3. KİTABI SİLME OPERASYONU
        findViewById<MaterialButton>(R.id.btn_delete_book).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Kitabı Tamamen Sil?")
                .setMessage("Bu kitabı ve ona ait tüm bölümleri veritabanından kalıcı olarak silmek istediğinden emin misin?")
                .setPositiveButton("Evet, Sil") { _, _ ->
                    RetrofitClient.api.deleteStory(storyId).enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(this@ManageBookActivity, "Kitap başarıyla yok edildi.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {}
                    })
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // 🚀 4. ARTI BUTONUNA BASINCA YENİ BÖLÜM EKLEME
        findViewById<ImageView>(R.id.iv_add_chapter).setOnClickListener {
            val intent = Intent(this, WriteChapterActivity::class.java).apply {
                putExtra("STORY_ID", storyId)
                putExtra("IS_NEW_CHAPTER_MODE", true) // Yeni bölüm ekleme modunu tetikler
            }
            startActivity(intent)
        }
    }

    private fun loadBookDataFromServer() {
        RetrofitClient.api.getBookDetails(storyId).enqueue(object : Callback<BookDetailsResponse> {
            override fun onResponse(call: Call<BookDetailsResponse>, response: Response<BookDetailsResponse>) {
                // 🚀 HATA TESPİTİ: Sunucudan dönen ham veriyi ve HTTP kodunu logluyoruz
                val statusCode = response.code()

                if (response.isSuccessful && response.body()?.success == true) {
                    val storyData = response.body()!!.story
                    val chapterList = response.body()!!.chapters

                    etSummary.setText(storyData.summary)
                    currentCoverUrl = storyData.coverImageUrl // Gerçek kapak linkini aldık
                    currentStatus = "PUBLISHED"
                    updateStatusButtonUI()

                    rvChapters.adapter = ManageChaptersAdapter(chapterList) { selectedChapter ->
                        // Bölüme tıklanınca yapılacaklar
                    }

                    Toast.makeText(this@ManageBookActivity, "Veriler başarıyla yüklendi!", Toast.LENGTH_SHORT).show()
                } else {
                    // 🎯 EĞER BAŞARISIZ OLURSA BURASI ÇALIŞACAK:
                    val rawError = response.errorBody()?.string() ?: "Boş hata gövdesi"

                    // 🛠️ KESİN ÇÖZÜM: İmla hatası giderildi, tertemiz Android koduna dönüştürüldü!
                    AlertDialog.Builder(this@ManageBookActivity)
                        .setTitle("🚨 Yönetim Paneli Hatası ($statusCode)")
                        .setMessage("Sunucudan dönen ham mesaj:\n\n$rawError\n\nNot: Eğer kod 404 ise backend rotasında, 500 ise SQL sorgusunda pürüz vardır.")
                        .setPositiveButton("Anladım", null)
                        .show()
                }
            }

            override fun onFailure(call: Call<BookDetailsResponse>, t: Throwable) {
                // 🎯 İNTERNET VEYA SUNUCU BAĞLANTI HATASI BURAYA DÜŞER:
                AlertDialog.Builder(this@ManageBookActivity)
                    .setTitle("💥 Ağ Bağlantı Hatası")
                    .setMessage("Sunucuya erişilemedi: ${t.message}")
                    .setPositiveButton("Tamam", null)
                    .show()
            }
        })
    }

    private fun updateStatusButtonUI() {
        if (currentStatus == "PUBLISHED") {
            btnPublishToggle.text = "Yayından Kaldır"
        } else {
            btnPublishToggle.text = "Yayınla"
        }
    }
}

// 🌐 KİTABIN BÖLÜMLERİNİ LİSTELEYEN DİNAMİK YAZAR ADAPTÖRÜ
class ManageChaptersAdapter(
    private val chapters: List<DBChapter>,
    private val onChapterClick: (DBChapter) -> Unit
) : RecyclerView.Adapter<ManageChaptersAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tv_reader_chapter_title)
        val tvStats: TextView = v.findViewById(R.id.tv_reader_chapter_stats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reader_chapter, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = chapters[position]
        holder.tvTitle.text = "${ch.chapterNumber}. Bölüm"
        holder.tvStats.text = "⭐ Beğeni: ${ch.starCount}  |  💬 Yorum: ${ch.commentCount}"
        holder.itemView.setOnClickListener { onChapterClick(ch) }
    }

    override fun getItemCount(): Int = chapters.size
}