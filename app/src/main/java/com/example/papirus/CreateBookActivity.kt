package com.example.papirus

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.papirus.data.ApiResponse
import com.example.papirus.data.CreateBookRequest
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateBookActivity : AppCompatActivity() {

    private var isEditMode = false
    private var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_book)

        // UI Elemanları
        val etTitle = findViewById<EditText>(R.id.et_book_title)
        val etBlurb = findViewById<EditText>(R.id.et_book_blurb) // Tanıtım yazısı
        val spinnerCategory = findViewById<Spinner>(R.id.spinner_book_category)
        val btnSaveDraft = findViewById<MaterialButton>(R.id.btn_save_draft)
        val btnWriteFirst = findViewById<MaterialButton>(R.id.btn_write_first_chapter)

        // Spinner Ayarları
        val categories = listOf("Bilim Kurgu", "Fantastik", "Roman", "Kişisel Gelişim", "Korku")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Gelen Verileri Kontrol Et (Edit Mode kontrolü)
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        if (isEditMode) {
            bookId = intent.getStringExtra("BOOK_ID")
            etTitle.setText(intent.getStringExtra("BOOK_TITLE"))
            etBlurb.setText(intent.getStringExtra("BOOK_BLURB"))

            // Kategori pozisyonunu bul ve ayarla
            val category = intent.getStringExtra("BOOK_CATEGORY")
            spinnerCategory.setSelection(categories.indexOf(category))

            btnWriteFirst.text = "Değişiklikleri Kaydet"
        }

        // Geri Dönüş
        findViewById<ImageView>(R.id.iv_back_create).setOnClickListener { finish() }

       /* fun saveBookToDatabase(isDraft: Boolean) {
            val title = etTitle.text.toString().trim()
            val blurb = etBlurb.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()

            // Şimdilik test için statik 1 veriyoruz, normalde giriş yapan kullanıcının ID'si gelir
            val currentUserId = 1

            if (title.isEmpty() || blurb.isEmpty()) {
                Toast.makeText(this, "Lütfen kitap adı ve tanıtım alanını doldurun.", Toast.LENGTH_SHORT).show()
                return
            }

            // Kapak resmi olarak şimdilik beyaz_logo id'sini yolluyoruz
            val request = CreateBookRequest(title, R.drawable.beyaz_logo, currentUserId, category, blurb, isDraft)

            RetrofitClient.api.createBook(request).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@CreateBookActivity, "Kitap Başarıyla Veritabanına Eklendi!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@CreateBookActivity, "Bir hata oluştu.", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@CreateBookActivity, "Sunucu Bağlantı Hatası!", Toast.LENGTH_SHORT).show()
                }
            })
        }*/

       // btnSaveDraft.setOnClickListener { saveBookToDatabase(isDraft = true) }
        //btnWriteFirst.setOnClickListener { saveBookToDatabase(isDraft = false) }


        btnWriteFirst.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val blurb = etBlurb.text.toString().trim()

            // Validasyon: Alanlar boş mu?
            if (title.isEmpty() || blurb.isEmpty()) {
                Toast.makeText(this, "Lütfen kitap adı ve tanıtım yazısını doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verileri paketleyip yazı yazma ekranına (WriteChapterActivity) gönderiyoruz:
            val intent = Intent(this, WriteChapterActivity::class.java).apply {
                putExtra("BOOK_TITLE", title)
                putExtra("BOOK_BLURB", blurb)
                putExtra("IS_PUBLISHED", false) // Yeni kitap olduğu için yayınlanmadı durumunda
            }
            startActivity(intent)
            finish()
        }
    }
}