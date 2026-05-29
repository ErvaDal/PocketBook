package com.example.papirus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentAdapter(
    private val commentList: List<Comment>,
    private val onReplyClick: (Comment) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tv_comment_username)
        val tvTime: TextView = view.findViewById(R.id.tv_comment_time)
        val tvText: TextView = view.findViewById(R.id.tv_comment_text)
        val tvReply: TextView = view.findViewById(R.id.tv_comment_reply)
        val llLike: LinearLayout = view.findViewById(R.id.ll_comment_like)
        val ivLikeIcon: ImageView = view.findViewById(R.id.iv_comment_like)
        val tvLikeCount: TextView = view.findViewById(R.id.tv_comment_like_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.tvUsername.text = comment.username
        holder.tvTime.text = comment.timeAgo
        holder.tvText.text = comment.text

        // --- GÖRÜNÜMÜ YANIT İSE SAĞA KAYDIRMA İŞLEMİ ---
        val dpToPx = { dp: Int -> (dp * holder.itemView.context.resources.displayMetrics.density).toInt() }
        if (comment.isReply) {
            // Eğer yanıtsa sol taraftan 56dp boşluk bırak (İçeri it)
            holder.itemView.setPadding(dpToPx(56), dpToPx(12), dpToPx(16), dpToPx(12))
        } else {
            // Normal yorumsa normal boşluk (16dp)
            holder.itemView.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
        }

        val updateLikeUI = {
            holder.tvLikeCount.text = if (comment.likesCount > 0) comment.likesCount.toString() else ""
            if (comment.isLiked) {
                holder.ivLikeIcon.setColorFilter(Color.parseColor("#E91E63"))
            } else {
                holder.ivLikeIcon.setColorFilter(Color.parseColor("#888888"))
            }
        }
        updateLikeUI()

        holder.llLike.setOnClickListener {
            comment.isLiked = !comment.isLiked
            if (comment.isLiked) comment.likesCount++ else comment.likesCount--
            updateLikeUI()
        }

        holder.tvReply.setOnClickListener {
            onReplyClick(comment)
        }
    }

    override fun getItemCount(): Int = commentList.size
}