package com.example.papirus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.papirus.data.DBBook

class BookAdapter(
    private var bookList: List<DBBook>,
    // 🚀 KESİN ÇÖZÜM: Lambda fonksiyonunun beklediği modeli 'DBBook' olarak harfi harfine eşitledik!
    private val onItemClick: ((DBBook) -> Unit)? = null
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

        // 🚀 Statik veya dinamik görsel ataması projedeki gibi kalabilir
        holder.ivCover.setImageResource(android.R.drawable.ic_menu_gallery)

        // Bütün öğeye tıklandığında artık pürüzsüzce tetiklenecek!
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(book)
        }
    }

    override fun getItemCount(): Int = bookList.size

    // Tip uyuşmazlığı uyarısı vermemesi için updateData parametresini de DBBook yapıyoruz
    fun updateData(newBookList: List<DBBook>) {
        this.bookList = newBookList
        notifyDataSetChanged()
    }
}