package com.example.papirus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private var bookList: List<Book>,
    private val onItemClick: ((Book) -> Unit)? = null // Tıklama için lambda eklendi
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_book_cover)
        val tvTitle: TextView = view.findViewById(R.id.tv_book_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]
        holder.tvTitle.text = book.title
        holder.ivCover.setImageResource(book.coverImageResId)

        // Bütün öğeye tıklandığında ne olacağını Activity'ye gönderiyoruz
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(book)
        }
    }

    override fun getItemCount(): Int = bookList.size

    // API'den yeni veri geldiğinde listeyi güncellemek için gerekli fonksiyon
    fun updateData(newBookList: List<Book>) {
        this.bookList = newBookList
        notifyDataSetChanged()
    }
}