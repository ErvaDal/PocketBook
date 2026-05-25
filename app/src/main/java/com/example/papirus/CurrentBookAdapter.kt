package com.example.papirus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CurrentBookAdapter(
    private var currentBookList: List<CurrentBook>,
    private val onItemClick: ((CurrentBook) -> Unit)? = null
) : RecyclerView.Adapter<CurrentBookAdapter.CurrentViewHolder>() {

    class CurrentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_current_book_cover)
        val tvTitle: TextView = view.findViewById(R.id.tv_current_book_title)
        val pbProgress: ProgressBar = view.findViewById(R.id.pb_read_progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_current_book, parent, false)
        return CurrentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrentViewHolder, position: Int) {
        val book = currentBookList[position]
        holder.tvTitle.text = book.title
        holder.ivCover.setImageResource(book.coverImageResId)
        holder.pbProgress.progress = book.readProgress

        // Öğeye tıklanma olayını yakala
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(book)
        }
    }

    override fun getItemCount(): Int = currentBookList.size

    fun updateData(newCurrentBookList: List<CurrentBook>) {
        this.currentBookList = newCurrentBookList
        notifyDataSetChanged()
    }
}