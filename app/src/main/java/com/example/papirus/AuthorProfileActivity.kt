package com.example.papirus

import android.os.Bundle
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.papirus.data.ProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.View
import android.widget.Toast

class AuthorProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_author_profile)

        // Mesaj At İkonu
        val ivMessage = findViewById<ImageView>(R.id.iv_message_author)
        ivMessage.visibility = View.VISIBLE
        ivMessage.setOnClickListener {
            Toast.makeText(this, "Mesaj ekranı açılıyor...", Toast.LENGTH_SHORT).show()
        }
        //Kullanıcı ID
        val sharedPreferences = getSharedPreferences("PapirusSettings", Context.MODE_PRIVATE)
        val loggedInUserId = sharedPreferences.getString("current_user_id", "")

        // Eğer kullanıcı ID'si bulunamazsa (oturum açılmamışsa) hata verdirip kapatıyoruz
        if (loggedInUserId.isNullOrEmpty()) {
            Toast.makeText(this, "Kullanıcı oturumu bulunamadı! Lütfen tekrar giriş yapın.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadAuthorRealData(loggedInUserId)
    }

    private fun loadAuthorRealData(userId: String) {
        val tvRealName = findViewById<TextView>(R.id.tv_author_real_name)
        val rvPublished = findViewById<RecyclerView>(R.id.rv_author_published_books)
        val rvPublicLibs = findViewById<RecyclerView>(R.id.rv_author_public_libraries)

        // Arkadaşının eklediği RecyclerView'ların tasarım düzenlerini (LayoutManager) tanımlıyoruz
        rvPublished.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPublicLibs.layoutManager = LinearLayoutManager(this)

        // Sunucudan (Node.js) gerçek profil verilerini talep ediyoruz
        RetrofitClient.api.getUserProfile(userId).enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(call: Call<ProfileResponse>, response: Response<ProfileResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val profileData = response.body()!!

                    // 🎯 E-posta adresinden kullanıcı adını gösterme mantığı:
                    // Arayüzdeki TextView alanına "kullanici_adi (eposta@domain.com)" şeklinde basıyoruz
                    tvRealName.text = "${profileData.user.username} \n(${profileData.user.email})"

                    // 🎯 Veritabanından gelen gerçek yayınlanmış kitapları listeliyoruz
                    val realBooks = profileData.publishedBooks
                    rvPublished.adapter = BookAdapter(realBooks) { secilenKitap ->
                        Toast.makeText(this@AuthorProfileActivity, "${secilenKitap.title} detaylarına gidiliyor...", Toast.LENGTH_SHORT).show()
                    }

                    // Veritabanından gelen gerçek herkese açık kütüphaneleri listeliyoruz
                    val realLibraries = profileData.publicLibraries
                    rvPublicLibs.adapter = UserLibraryAdapter(realLibraries) { secilenKutuphane ->
                        Toast.makeText(this@AuthorProfileActivity, "${secilenKutuphane.libraryName} içeriği açılıyor...", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@AuthorProfileActivity, "Profil verileri yüklenirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Toast.makeText(this@AuthorProfileActivity, "Ağ Hatası: Sunucu bağlantısı başarısız.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}