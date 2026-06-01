package com.example.papirus

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WriteChapterActivity : AppCompatActivity() {

    private lateinit var etContent: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_chapter)

        etContent = findViewById(R.id.et_chapter_content)
        val btnPublish = findViewById<MaterialButton>(R.id.btn_publish_chapter)
        val tvSave = findViewById<TextView>(R.id.tv_save_chapter)
        val ivMore = findViewById<ImageView>(R.id.iv_chapter_more)

        findViewById<ImageView>(R.id.iv_back_chapter).setOnClickListener { finish() }

        // ManageBookActivity'den durumu aldık
        val isPublished = intent.getBooleanExtra("IS_PUBLISHED", false)

        if (isPublished) {
            btnPublish.text = "Güncelle"
            ivMore.visibility = View.VISIBLE // Zaten yayındaysa 3 noktayı göster (Yayından Kaldır seçeneği için)
        } else {
            btnPublish.text = "Yayınla"
            ivMore.visibility = View.GONE    // Taslaksa gizle
        }

        // KAYDET VE ÇIK
        tvSave.setOnClickListener {
            Toast.makeText(this, "Değişiklikler taslak olarak kaydedildi.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // YAYINLA VEYA GÜNCELLE
        btnPublish.setOnClickListener {
            if (isPublished) Toast.makeText(this, "Bölüm güncellendi!", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Bölüm yayınlandı!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // YAYINDAN KALDIR (Sadece yayındaki bölümlerde çıkar)
        ivMore.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Yayından Kaldır")
                .setMessage("Bu bölümü yayından kaldırmak istiyor musunuz? Okuyucular artık bu bölümü göremeyecek.")
                .setPositiveButton("Evet, Kaldır") { _, _ ->
                    Toast.makeText(this, "Bölüm yayından kaldırıldı (Taslaklara alındı).", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        // Format ve Medya İşlemleri...
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