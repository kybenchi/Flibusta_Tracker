package com.yuk.flibustatracker.data

import com.google.gson.annotations.SerializedName

data class Author(
    @SerializedName("url") var url: String = "",
    @SerializedName("name") var name: String? = null,
    @SerializedName("custom_name") var customName: String? = null,
    @SerializedName("added") var added: String = "",
    @SerializedName("cutoff_days") var cutoffDays: Int = 300,
    @SerializedName("last_check") var lastCheck: String? = null
) {
    fun displayName(): String =
        customName?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: "Неизвестный автор"
}

data class Book(
    @SerializedName("title") var title: String = "",
    @SerializedName("author") var author: String = "",
    @SerializedName("date") var date: String = "",
    @SerializedName("date_iso") var dateIso: String = "",
    @SerializedName("url") var url: String = "",
    @SerializedName("fb2_link") var fb2Link: String? = null,
    @SerializedName("pdf_link") var pdfLink: String? = null,
    @SerializedName("epub_link") var epubLink: String? = null,
    @SerializedName("mobi_link") var mobiLink: String? = null,
    @SerializedName("description") var description: String = "",
    @SerializedName("author_url") var authorUrl: String = "",
    @SerializedName("found_date") var foundDate: String = ""
)

data class Settings(
    @SerializedName("default_cutoff_days") var defaultCutoffDays: Int = 300,
    @SerializedName("export_folder") var exportFolder: String = ""
)

data class AppData(
    @SerializedName("authors") var authors: MutableList<Author> = mutableListOf(),
    @SerializedName("books") var books: MutableList<Book> = mutableListOf(),
    @SerializedName("last_check") var lastCheck: String? = null,
    @SerializedName("settings") var settings: Settings = Settings()
)