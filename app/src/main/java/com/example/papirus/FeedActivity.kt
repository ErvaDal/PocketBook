package com.example.papirus

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class FeedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feed)

        setupWindowInsets()
        setupFeedRecyclerView()
        setupAddPostButton()
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation_feed).selectedItemId = R.id.nav_feed
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_feed)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupFeedRecyclerView() {
        val dummyPosts = listOf(
            Post("1", "@yazilimci_gunlugu", R.drawable.ic_person, "Yeni bir projeye başladım! Arayüz tasarımı gerçekten backend'den daha yorucu olabiliyor \uD83D\uDE05 #Android #Kotlin", null, "10 dk önce", 45, 8),
            Post("2", "@kitap_kurdu", R.drawable.ic_person, "Bugün kütüphaneme yeni bir şaheser ekledim. George Orwell - 1984. Kesinlikle okunmalı!", R.drawable.beyaz_logo, "2 saat önce", 120, 24),
            Post("3", "@film_elestirmeni", R.drawable.ic_person, "Dün akşam izlediğim bilim kurgu filminin sonu inanılmazdı. Sizin en sevdiğiniz film hangisi?", null, "5 saat önce", 89, 41)
        )

        val rvFeed = findViewById<RecyclerView>(R.id.rv_feed_posts)
        rvFeed.adapter = PostAdapter(dummyPosts) { tiklananGonderi ->
            // Yorum butonuna tıklandığında Bottom Sheet'i aç
            showCommentsBottomSheet(tiklananGonderi)
        }
    }

    private fun showCommentsBottomSheet(post: Post) {
        val bottomSheetDialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_comments, null)
        bottomSheetDialog.setContentView(view)

        val etComment = view.findViewById<android.widget.EditText>(R.id.et_new_comment)
        val rvComments = view.findViewById<RecyclerView>(R.id.rv_comments_list)

        val dummyComments = mutableListOf(
            Comment("@ahmet_y", "Kesinlikle katılıyorum!", "5dk"),
            Comment("@ayse_k", "Ben de geçen hafta okudum, harikaydı.", "12dk"),
            Comment("@mehmet123", "Fiyatı ne kadar acaba?", "1s"),
            Comment("@kitap_delisi", "Bunu okuduktan sonra Dune serisine de bakmalısın.", "2s")
        )

        // Hangi yoruma yanıt verdiğimizi tutacağımız hafıza değişkeni (-1 ise kimseye yanıt vermiyoruz demektir)
        var replyingToCommentIndex = -1

        val commentAdapter = CommentAdapter(dummyComments) { tiklananYorum ->
            // Yanıtla butonuna basıldığında o yorumun listedeki sırasını kaydet
            replyingToCommentIndex = dummyComments.indexOf(tiklananYorum)

            etComment.setText("${tiklananYorum.username} ")
            etComment.setSelection(etComment.text.length)
            etComment.requestFocus()
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(etComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        rvComments.adapter = commentAdapter

        val ivSend = view.findViewById<ImageView>(R.id.iv_send_comment)
        ivSend.setOnClickListener {
            val yeniYorumMetni = etComment.text.toString().trim()

            if (yeniYorumMetni.isNotEmpty()) {
                if (replyingToCommentIndex != -1) {
                    // 1. DURUM: BİRİNE YANIT VERİYORUZ
                    val yeniYorum = Comment("@benim_hesabim", yeniYorumMetni, "Şimdi", isReply = true)

                    // Yorumu en alta değil, tıkladığımız kişinin hemen bir altına (index + 1) ekliyoruz
                    val insertIndex = replyingToCommentIndex + 1
                    dummyComments.add(insertIndex, yeniYorum)
                    commentAdapter.notifyItemInserted(insertIndex)
                    rvComments.scrollToPosition(insertIndex)

                    // Hafızayı sıfırla ki sonraki yazdıklarımız da yanıt sanılmasın
                    replyingToCommentIndex = -1
                } else {
                    // 2. DURUM: NORMAL YORUM YAPIYORUZ
                    val yeniYorum = Comment("@benim_hesabim", yeniYorumMetni, "Şimdi", isReply = false)
                    dummyComments.add(yeniYorum)
                    commentAdapter.notifyItemInserted(dummyComments.size - 1)
                    rvComments.scrollToPosition(dummyComments.size - 1)
                }

                etComment.text.clear()
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(etComment.windowToken, 0)
            } else {
                android.widget.Toast.makeText(this, "Yorum boş olamaz!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        bottomSheetDialog.show()
    }

    private fun setupAddPostButton() {
        val ivAddPost = findViewById<ImageView>(R.id.iv_add_post)
        ivAddPost.setOnClickListener {
            // "+" butonuna basılınca AddPostActivity sayfasını aç
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_feed)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_feed -> true // Zaten bu sayfadayız
                R.id.nav_library -> {
                    val intent = Intent(this, LibraryActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}