package com.example.papirus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserLibraryAdapter(
    private var libraryList: List<UserLibrary>,
    private val onItemClick: ((UserLibrary) -> Unit)? = null
) : RecyclerView.Adapter<UserLibraryAdapter.LibraryViewHolder>() {

    class LibraryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_library_name)
        val tvCount: TextView = view.findViewById(R.id.tv_library_count)
        val ivPrivacy: ImageView = view.findViewById(R.id.iv_privacy_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_library, parent, false)
        return LibraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val library = libraryList[position]
        holder.tvName.text = library.libraryName
        holder.tvCount.text = "${library.bookCount} Kitap"

        if (library.isPublic) {
            holder.ivPrivacy.setImageResource(android.R.drawable.ic_menu_mapmode) // Herkese Açık (Dünya)
            holder.tvCount.text = "${library.bookCount} Kitap • Herkese Açık"
        } else {
            holder.ivPrivacy.setImageResource(android.R.drawable.ic_secure) // Gizli (Kilit)
            holder.tvCount.text = "${library.bookCount} Kitap • Gizli"
        }

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(library)
        }
    }

    override fun getItemCount(): Int = libraryList.size

    fun updateData(newLibraryList: List<UserLibrary>) {
        this.libraryList = newLibraryList
        notifyDataSetChanged()
    }
}