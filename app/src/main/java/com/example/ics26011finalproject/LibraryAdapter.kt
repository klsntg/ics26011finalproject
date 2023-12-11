package com.example.ics26011finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class LibraryAdapter(
    private val onRemoveClick: (Int) -> Unit,
    private val onItemClick: (Details) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    private var books: List<Details> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_books, parent, false)
        return LibraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val currentBook = books[position]
        holder.bind(currentBook)

        holder.itemView.setOnClickListener {
            onItemClick(currentBook)
        }

        // Update the function to pass bookId instead of currentBook
        holder.removeButton.setOnClickListener {
            onRemoveClick(currentBook.id)

            // Your existing code
            books = books.filter { it != currentBook }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = books.size

    fun setBooks(books: List<Details>) {
        this.books = books
        notifyDataSetChanged()
    }

    class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvTitle)
        private val authorTextView: TextView = itemView.findViewById(R.id.tvAuthor)
        private val coverImageView: ImageView = itemView.findViewById(R.id.cover)
        val removeButton: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(details: Details) {
            titleTextView.text = details.title
            authorTextView.text = details.author

            Glide.with(itemView.context)
                .load(getImageResource(details.imageSource))
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(coverImageView)
        }

        private fun getImageResource(imageSource: String): Any {
            return itemView.context.resources.getIdentifier(
                imageSource,
                "drawable",
                itemView.context.packageName
            )
        }
    }
}
