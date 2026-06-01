package com.example.papirus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.papirus.data.DBBook

class NewBookAdapter(
    private val list: List<DBBook>,
    private val onClick: (DBBook) -> Unit
) : RecyclerView.Adapter<NewBookAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.iv_book_cover) // item_book içindeki id'niz
        val txt: TextView = v.findViewById(R.id.tv_book_title)  // item_book içindeki id'niz
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.txt.text = item.title

        holder.img.setImageResource(android.R.drawable.ic_menu_gallery)
        holder.img.setColorFilter(android.graphics.Color.GRAY)

        holder.itemView.setOnClickListener { onClick(item) }
    }
    override fun getItemCount() = list.size
}