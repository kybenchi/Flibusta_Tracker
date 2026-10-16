package com.yuk.flibustatracker.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yuk.flibustatracker.R
import com.yuk.flibustatracker.TrackerViewModel
import com.yuk.flibustatracker.data.Book
import com.yuk.flibustatracker.ui.adapters.BooksAdapter
import com.yuk.flibustatracker.util.DownloadHelper

class BooksFragment : Fragment() {

    private val vm: TrackerViewModel by activityViewModels()
    private lateinit var adapter: BooksAdapter
    private lateinit var countText: TextView

    private var currentSort = "found"
    private var books: List<Book> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_books, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.booksRecycler)
        countText = view.findViewById(R.id.booksCount)

        val btnAuthor = view.findViewById<Button>(R.id.sortAuthor)
        val btnDate = view.findViewById<Button>(R.id.sortDate)
        val btnFound = view.findViewById<Button>(R.id.sortFound)

        adapter = BooksAdapter(
            onOpenPage = { openUrl(it.url) },
            onDownload = { book, format -> download(book, format) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        fun selectSort(mode: String) {
            currentSort = mode
            val active = requireContext().getColor(R.color.purple_primary)
            val inactive = requireContext().getColor(R.color.gray_btn)
            btnAuthor.setBackgroundColor(if (mode == "author") active else inactive)
            btnDate.setBackgroundColor(if (mode == "date") active else inactive)
            btnFound.setBackgroundColor(if (mode == "found") active else inactive)
            render()
        }
        btnAuthor.setOnClickListener { selectSort("author") }
        btnDate.setOnClickListener { selectSort("date") }
        btnFound.setOnClickListener { selectSort("found") }

        vm.data.observe(viewLifecycleOwner) { d ->
            books = d.books
            countText.text = "Найденные книги (${d.books.size})"
            render()
        }
        vm.toast.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        selectSort(currentSort)
    }

    private fun render() {
        val sorted = when (currentSort) {
            "author" -> books.sortedBy { it.author.lowercase() }
            "date" -> books.sortedByDescending { it.dateIso }
            else -> books.sortedByDescending { it.foundDate }
        }
        adapter.submit(sorted)
    }

    private fun download(book: Book, format: String) {
        val url = when (format) {
            "fb2" -> book.fb2Link
            "pdf" -> book.pdfLink
            "epub" -> book.epubLink
            else -> book.mobiLink
        }
        if (url == null) {
            Toast.makeText(requireContext(), "Ссылка недоступна", Toast.LENGTH_SHORT).show()
            return
        }
        DownloadHelper.download(requireContext(), url, book.url, book.title)
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }
}