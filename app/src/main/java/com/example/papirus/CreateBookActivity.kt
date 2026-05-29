package com.example.papirus

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

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

        // Kaydetme İşlemleri
        btnSaveDraft.setOnClickListener {
            if (etTitle.text.isEmpty()) {
                Toast.makeText(this, "Lütfen bir kitap adı girin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "Kitap taslak olarak kaydedildi.", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnWriteFirst.setOnClickListener {
            val title = etTitle.text.toString()
            val category = spinnerCategory.selectedItem.toString()

            if (title.isEmpty()) {
                Toast.makeText(this, "Lütfen bir kitap adı girin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                // GÜNCELLEME MANTIĞI
                Toast.makeText(this, "Kitap güncellendi!", Toast.LENGTH_SHORT).show()
            } else {
                // YENİ KAYIT MANTIĞI
                val intent = Intent(this, WriteChapterActivity::class.java).apply {
                    putExtra("BOOK_TITLE", title)
                    putExtra("BOOK_CATEGORY", category)
                    putExtra("IS_PUBLISHED", false)
                }
                startActivity(intent)
            }
            finish()
        }
    }
}