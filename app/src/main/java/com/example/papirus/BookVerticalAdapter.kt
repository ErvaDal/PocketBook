package com.example.papirus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookVerticalAdapter(
    private var bookList: List<Book>,
    private val onItemClick: ((Book) -> Unit)? = null
) : RecyclerView.Adapter<BookVerticalAdapter.VerticalViewHolder>() {

    class VerticalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_book_cover_vertical)
        val tvTitle: TextView = view.findViewById(R.id.tv_book_title_vertical)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_vertical, parent, false)
        return VerticalViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerticalViewHolder, position: Int) {
        val book = bookList[position]
        holder.tvTitle.text = book.title
        holder.ivCover.setImageResource(book.coverImageResId)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(book)
        }
    }

    override fun getItemCount(): Int = bookList.size
}