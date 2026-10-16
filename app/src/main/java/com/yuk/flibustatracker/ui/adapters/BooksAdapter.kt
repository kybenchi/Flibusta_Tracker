package com.yuk.flibustatracker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yuk.flibustatracker.R
import com.yuk.flibustatracker.data.Book

class BooksAdapter(
    private val onOpenPage: (Book) -> Unit,
    private val onDownload: (Book, String) -> Unit  // (book, формат)
) : RecyclerView.Adapter<BooksAdapter.VH>() {

    private val items = mutableListOf<Book>()

    fun submit(list: List<Book>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.bookTitle)
        val date: TextView = v.findViewById(R.id.bookDate)
        val author: TextView = v.findViewById(R.id.bookAuthor)
        val desc: TextView = v.findViewById(R.id.bookDescription)
        val found: TextView = v.findViewById(R.id.bookFound)
        val btnPage: Button = v.findViewById(R.id.btnPage)
        val btnFb2: Button = v.findViewById(R.id.btnFb2)
        val btnPdf: Button = v.findViewById(R.id.btnPdf)
        val btnEpub: Button = v.findViewById(R.id.btnEpub)
        val btnMobi: Button = v.findViewById(R.id.btnMobi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = items[position]
        holder.title.text = b.title
        holder.date.text = b.date
        holder.author.text = b.author
        holder.desc.text = b.description.ifBlank { "Описание отсутствует" }
        holder.found.text = "Найдено: ${b.foundDate}"

        holder.btnPage.setOnClickListener { onOpenPage(b) }

        holder.btnFb2.visibility = if (b.fb2Link != null) View.VISIBLE else View.GONE
        holder.btnPdf.visibility = if (b.pdfLink != null) View.VISIBLE else View.GONE
        holder.btnEpub.visibility = if (b.epubLink != null) View.VISIBLE else View.GONE
        holder.btnMobi.visibility = if (b.mobiLink != null) View.VISIBLE else View.GONE

        holder.btnFb2.setOnClickListener { onDownload(b, "fb2") }
        holder.btnPdf.setOnClickListener { onDownload(b, "pdf") }
        holder.btnEpub.setOnClickListener { onDownload(b, "epub") }
        holder.btnMobi.setOnClickListener { onDownload(b, "mobi") }
    }
}