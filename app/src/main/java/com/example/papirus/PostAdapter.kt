package com.example.papirus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class PostAdapter(
    private var postList: List<Post>,
    private val onCommentClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tv_post_username)
        val tvTime: TextView = view.findViewById(R.id.tv_post_time)
        val tvContent: TextView = view.findViewById(R.id.tv_post_content)
        val cvMedia: MaterialCardView = view.findViewById(R.id.cv_post_media)
        val ivMedia: ImageView = view.findViewById(R.id.iv_post_media)
        val tvLikes: TextView = view.findViewById(R.id.tv_likes_count)
        val tvComments: TextView = view.findViewById(R.id.tv_comments_count)
        val llLikeBtn: LinearLayout = view.findViewById(R.id.ll_like_btn)
        val ivLikeIcon: ImageView = view.findViewById(R.id.iv_like_icon) // Yeni eklendi
        val llCommentBtn: LinearLayout = view.findViewById(R.id.ll_comment_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        holder.tvUsername.text = post.username
        holder.tvTime.text = post.timeAgo
        holder.tvContent.text = post.contentText
        holder.tvComments.text = "${post.commentsCount} Yorum"

        if (post.contentMediaResId != null) {
            holder.cvMedia.visibility = View.VISIBLE
            holder.ivMedia.setImageResource(post.contentMediaResId)
        } else {
            holder.cvMedia.visibility = View.GONE
        }

        // --- BEĞENİ GÜNCELLEME FONKSİYONU ---
        val updateLikeUI = {
            holder.tvLikes.text = post.likesCount.toString()
            if (post.isLiked) {
                holder.ivLikeIcon.setColorFilter(Color.parseColor("#E91E63")) // Kırmızı yap
            } else {
                holder.ivLikeIcon.setColorFilter(Color.parseColor("#888888")) // Gri yap
            }
        }
        updateLikeUI() // İlk açılışta rengi ayarla

        // --- BEĞEN BUTONUNA TIKLANINCA ---
        holder.llLikeBtn.setOnClickListener {
            post.isLiked = !post.isLiked // Durumu tersine çevir
            if (post.isLiked) post.likesCount++ else post.likesCount-- // Sayıyı artır/azalt
            updateLikeUI() // Ekranı güncelle
        }

        // Yorum Butonu
        holder.llCommentBtn.setOnClickListener {
            onCommentClick(post)
        }
    }

    override fun getItemCount(): Int = postList.size
}