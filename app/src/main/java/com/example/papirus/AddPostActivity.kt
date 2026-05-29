package com.example.papirus

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class AddPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_post)

        setupWindowInsets()
        setupClickListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_post)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Sadece üst ve alt boşlukları ayarla ki klavye açıldığında tasarım bozulmasın
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        val tvCancel = findViewById<TextView>(R.id.tv_cancel_post)
        val btnShare = findViewById<MaterialButton>(R.id.btn_share_post)
        val etContent = findViewById<EditText>(R.id.et_post_content)
        val ivGallery = findViewById<ImageView>(R.id.iv_add_gallery)
        val ivCamera = findViewById<ImageView>(R.id.iv_add_camera)

        // Klavyeyi otomatik aç
        etContent.requestFocus()

        // İptal Butonu -> Sayfayı kapatır ve akışa geri döner
        tvCancel.setOnClickListener {
            finish()
        }

        // Paylaş Butonu
        btnShare.setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isNotEmpty()) {
                Toast.makeText(this, "Gönderiniz başarıyla paylaşıldı!", Toast.LENGTH_SHORT).show()
                finish() // Paylaştıktan sonra sayfayı kapat
            } else {
                Toast.makeText(this, "Lütfen bir şeyler yazın.", Toast.LENGTH_SHORT).show()
            }
        }

        // Medya Butonları (Şimdilik sadece mesaj gösteriyor, backend kısmında galeriyi açacak)
        ivGallery.setOnClickListener {
            Toast.makeText(this, "Galeri açılıyor...", Toast.LENGTH_SHORT).show()
        }

        ivCamera.setOnClickListener {
            Toast.makeText(this, "Kamera açılıyor...", Toast.LENGTH_SHORT).show()
        }
    }
}