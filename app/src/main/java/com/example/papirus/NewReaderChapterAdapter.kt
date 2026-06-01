package com.example.papirus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.papirus.data.DBChapter

class NewReaderChapterAdapter(
    private val chapters: List<DBChapter>,
    private val onChapterClick: (DBChapter) -> Unit
) : RecyclerView.Adapter<NewReaderChapterAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.findViewById(R.id.tv_reader_chapter_title)
        val tvStats: TextView = v.findViewById(R.id.tv_reader_chapter_stats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reader_chapter, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = chapters[position]
        holder.tvTitle.text = "${ch.chapterNumber}. Bölüm"
        holder.tvStats.text = "👁 Canlı  |  ⭐ ${ch.starCount}"

        holder.itemView.setOnClickListener { onChapterClick(ch) }
    }

    override fun getItemCount(): Int = chapters.size
}