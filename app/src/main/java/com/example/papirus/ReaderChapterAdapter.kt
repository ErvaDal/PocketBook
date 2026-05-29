package com.example.papirus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

// --- YORUM VERİ MODELİ ---
data class ChapterComment(val username: String, val text: String, val isReply: Boolean)

class ReadChapterActivity : AppCompatActivity() {

    private var isLiked = false
    private var likeCount = 245

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_chapter)

        val chapterTitle = intent.getStringExtra("CHAPTER_TITLE") ?: "Bölüm"
        findViewById<TextView>(R.id.tv_read_chapter_title).text = chapterTitle

        findViewById<ImageView>(R.id.iv_back_read).setOnClickListener { finish() }

        // BEĞENİ MANTIĞI
        val llLike = findViewById<LinearLayout>(R.id.ll_like_button)
        val tvLikeIcon = findViewById<TextView>(R.id.tv_like_icon)
        val tvLikeCount = findViewById<TextView>(R.id.tv_like_count)
        tvLikeCount.text = likeCount.toString()

        llLike.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                likeCount++
                tvLikeIcon.text = "❤️"
            } else {
                likeCount--
                tvLikeIcon.text = "🤍"
            }
            tvLikeCount.text = likeCount.toString()
        }

        // İLERİ VE GERİ BUTONLARI (Üstte)
        findViewById<TextView>(R.id.tv_prev_chapter).setOnClickListener {
            Toast.makeText(this, "Önceki bölüme geçiliyor...", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.tv_next_chapter).setOnClickListener {
            Toast.makeText(this, "Sonraki bölüme geçiliyor...", Toast.LENGTH_SHORT).show()
        }

        // --- ALTTAN AÇILAN MESAJLAŞMA (YORUM) PANELİ ---
        val llComment = findViewById<LinearLayout>(R.id.ll_comment_button)
        llComment.setOnClickListener {
            showCommentsBottomSheet()
        }
    }

    private fun showCommentsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_comments, null)
        bottomSheetDialog.setContentView(view)

        val rvComments = view.findViewById<RecyclerView>(R.id.rv_bottom_comments)
        val etNewComment = view.findViewById<EditText>(R.id.et_new_comment)
        val ivSend = view.findViewById<ImageView>(R.id.iv_send_comment)

        // Örnek Yorumlar (isReply = true olanlar içeriden başlar)
        val commentList = mutableListOf(
            ChapterComment("Erva", "Bu bölüm gerçekten nefes kesiciydi! Sonraki bölümü sabırsızlıkla bekliyorum.", false),
            ChapterComment("Okur2026", "@Erva kesinlikle katılıyorum, özellikle son sahne çok iyiydi.", true)
        )

        val adapter = ChapterCommentAdapter(commentList) { clickedComment ->
            // Biri "Cevapla" butonuna bastığında, yazma kutusuna @KullanıcıAdı ekler
            val replyText = "@${clickedComment.username} "
            etNewComment.setText(replyText)
            etNewComment.setSelection(replyText.length) // İmleci en sona alır
            etNewComment.requestFocus()
        }

        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = adapter

        // Gönder Butonu
        ivSend.setOnClickListener {
            val text = etNewComment.text.toString().trim()
            if (text.isNotEmpty()) {
                // Eğer mesaj "@" ile başlıyorsa bunu bir "Cevap" olarak algıla
                val isReply = text.startsWith("@")

                commentList.add(ChapterComment("Sen", text, isReply))
                adapter.notifyItemInserted(commentList.size - 1)
                rvComments.scrollToPosition(commentList.size - 1) // Ekranı en alta kaydır

                etNewComment.text.clear() // Kutuyu temizle
            }
        }

        bottomSheetDialog.show()
    }
}

// --- YORUM ADAPTÖRÜ ---
class ChapterCommentAdapter(
    private val comments: List<ChapterComment>,
    private val onReplyClick: (ChapterComment) -> Unit
) : RecyclerView.Adapter<ChapterCommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUser: TextView = view.findViewById(R.id.tv_comment_user)
        val tvText: TextView = view.findViewById(R.id.tv_comment_text)
        val tvReply: TextView = view.findViewById(R.id.tv_reply_button)
        val llRoot: LinearLayout = view.findViewById(R.id.ll_comment_root)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chapter_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.tvUser.text = comment.username
        holder.tvText.text = comment.text

        // Eğer bu bir cevapsa (isReply = true), yorumu biraz sağa kaydır (Girinti yap)
        val params = holder.llRoot.layoutParams as ViewGroup.MarginLayoutParams
        if (comment.isReply) {
            params.marginStart = 120 // Sağdan girinti (WhatsApp gibi)
        } else {
            params.marginStart = 0
        }
        holder.llRoot.layoutParams = params

        holder.tvReply.setOnClickListener {
            onReplyClick(comment)
        }
    }

    override fun getItemCount(): Int = comments.size
}