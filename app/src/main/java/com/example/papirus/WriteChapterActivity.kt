package com.example.papirus

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.papirus.data.ApiResponse
import com.example.papirus.data.CreateBookRequest
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WriteChapterActivity : AppCompatActivity() {

    private lateinit var etContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_chapter)

        val etChapterTitle = findViewById<EditText>(R.id.et_chapter_title)
        etContent = findViewById(R.id.et_chapter_content)
        val btnPublish = findViewById<MaterialButton>(R.id.btn_publish_chapter)
        val tvSave = findViewById<TextView>(R.id.tv_save_chapter)

        findViewById<ImageView>(R.id.iv_back_chapter).setOnClickListener { finish() }

        // 🎯 1. ADIM: CreateBookActivity'den gönderdiğimiz kitap ana bilgilerini teslim alıyoruz:
        val bookTitle = intent.getStringExtra("BOOK_TITLE") ?: ""
        val bookBlurb = intent.getStringExtra("BOOK_BLURB") ?: ""

        // KAYDET VE ÇIK (Şimdilik lokal uyarı)
        tvSave.setOnClickListener {
            Toast.makeText(this, "Taslak kaydetme özelliği yakında eklenecek.", Toast.LENGTH_SHORT).show()
        }

        // 🎯 2. ADIM: YAYINLA BUTONUNA BASILINCA VERİTABANINA KAYIT ATMA
        btnPublish.setOnClickListener {
            val chTitle = etChapterTitle.text.toString().trim()
            val chContent = etContent.text.toString().trim()

            if (chTitle.isEmpty() || chContent.isEmpty()) {
                Toast.makeText(this, "Bölüm adı ve içeriği boş bırakılamaz!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔐 3. ADIM: SharedPreferences içinden giriş yapmış yazarın gerçek UUID'sini çekiyoruz:
            val sharedPreferences = getSharedPreferences("PapirusSettings", Context.MODE_PRIVATE)
            val loggedInUserId = sharedPreferences.getString("current_user_id", "") ?: ""

            if (loggedInUserId.isEmpty()) {
                Toast.makeText(this, "Oturum hatası! Lütfen yeniden giriş yapın.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sunucuya gidecek olan istek gövdesini (Request Body) paketliyoruz:
            val request = CreateBookRequest(
                authorId = loggedInUserId,
                title = bookTitle,
                summary = bookBlurb,
                coverImageUrl = null, // Şimdilik kapak görseli boş geçiyor
                chapterTitle = chTitle,
                chapterContent = chContent
            )

            // 🚀 4. ADIM: RetrofitClient üzerinden Node.js API'sine isteği fırlatıyoruz:
            RetrofitClient.api.createBookWithChapter(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@WriteChapterActivity, "Kitabın ve ilk bölümün başarıyla yayınlandı! 🎉", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@WriteChapterActivity, ProfileActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        startActivity(intent)
                        finish()
                    } else {
                        // 🎯 İŞTE BURASI BİZİM HATA TESPİT ALANIMIZ:
                        val statusCode = response.code() // Sunucunun fırlattığı HTTP kodu (Örn: 500, 404, 400)
                        val rawErrorMessage = response.errorBody()?.string() ?: "Boş hata gövdesi"

                        // Ekranda devasa bir pencereyle hatayı gösteriyoruz ki gözümüzden kaçmasın
                        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this@WriteChapterActivity)
                            .setTitle("🚨 Sunucudan Gelen Hata Durumu ($statusCode)")
                            .setMessage("Veritabanı veya Rota Hatası Detayı:\n\n$rawErrorMessage")
                            .setPositiveButton("Anladım") { dialog, _ -> dialog.dismiss() }
                            .create()

                        alertDialog.show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // 🎯 EĞER İNTERNET VEYA PORT BAĞLANTISI HİÇ KURULAMAZSA BURASI ÇALIŞIR:
                    val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this@WriteChapterActivity)
                        .setTitle("💥 Ağ Bağlantı Hatası (Sunucuya Erişilemedi)")
                        .setMessage("Hata Mesajı:\n${t.message}\n\nLütfen Node.js sunucunun açık olduğundan ve emülatör internetinin çalıştığından emin ol!")
                        .setPositiveButton("Tamam") { dialog, _ -> dialog.dismiss() }
                        .create()

                    alertDialog.show()
                }
            })
        }

        // Metin Biçimlendirme Dinleyicileri
        findViewById<MaterialButton>(R.id.btn_bold).setOnClickListener { applyStyleToSelection(StyleSpan(Typeface.BOLD)) }
        findViewById<MaterialButton>(R.id.btn_italic).setOnClickListener { applyStyleToSelection(StyleSpan(Typeface.ITALIC)) }
        findViewById<MaterialButton>(R.id.btn_underline).setOnClickListener { applyStyleToSelection(UnderlineSpan()) }
    }

    private fun applyStyleToSelection(style: Any) {
        val start = etContent.selectionStart
        val end = etContent.selectionEnd
        if (start != end) {
            etContent.text.setSpan(style, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            Toast.makeText(this, "Lütfen önce metni seçin.", Toast.LENGTH_SHORT).show()
        }
    }
}