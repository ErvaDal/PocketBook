package com.example.papirus

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChapterAdapter(
    private val chapterList: List<AuthorChapter>,
    private val onChapterClick: (AuthorChapter) -> Unit,
    private val onOptionsClick: (AuthorChapter) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    class ChapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_chapter_title)
        val tvStatus: TextView = view.findViewById(R.id.tv_chapter_status)
        val tvStats: TextView = view.findViewById(R.id.tv_chapter_stats)
        val ivOptions: ImageView = view.findViewById(R.id.iv_chapter_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapterList[position]
        holder.tvTitle.text = chapter.title

        // Okunma, beğeni ve yorum sayılarını tek satırda birleştirip yazıyoruz
        holder.tvStats.text = "👁 ${chapter.reads}  |  ❤️ ${chapter.likes}  |  💬 ${chapter.comments}"

        // Duruma göre renk ve yazı ayarı
        if (chapter.isPublished) {
            holder.tvStatus.text = "Yayınlandı"
            holder.tvStatus.setTextColor(Color.parseColor("#00C853")) // Yeşil renk
        } else {
            holder.tvStatus.text = "Taslak"
            holder.tvStatus.setTextColor(Color.GRAY)
        }

        holder.itemView.setOnClickListener { onChapterClick(chapter) }
        holder.ivOptions.setOnClickListener { onOptionsClick(chapter) }
    }

    override fun getItemCount(): Int = chapterList.size
}