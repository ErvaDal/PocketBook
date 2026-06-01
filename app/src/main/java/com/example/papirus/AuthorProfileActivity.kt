package com.example.papirus

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Toast

class AuthorProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_author_profile)

        // Mesaj At İkonu
        val ivMessage = findViewById<ImageView>(R.id.iv_message_author)
        ivMessage.visibility = View.VISIBLE // Başkasının profilinde görünür yap

        ivMessage.setOnClickListener {
            // Mesajlaşma aktivitesine git
            Toast.makeText(this, "Mesaj ekranı açılıyor...", Toast.LENGTH_SHORT).show()
        }

        // Diğer verileri yükle (Sadece herkese açık kitaplar ve postlar)
        loadAuthorPublicData()
    }

    // AuthorProfileActivity.kt içinde
    private fun loadAuthorPublicData() {
        val rvPublished = findViewById<RecyclerView>(R.id.rv_author_published_books)
        val rvPublicLibs = findViewById<RecyclerView>(R.id.rv_author_public_libraries)


        val publishedBooks = listOf<Book>()

        rvPublished.adapter = BookAdapter(publishedBooks) {
            // Kitap detayına git
        }

        // Aynı şekilde kütüphaneler için de bir liste oluşturun
        val publicLibraries = listOf<Book>()
        rvPublicLibs.adapter = BookAdapter(publicLibraries) {
            // Kütüphane detayına git
        }
    }
}