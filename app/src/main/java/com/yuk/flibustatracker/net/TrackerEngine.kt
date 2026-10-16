package com.yuk.flibustatracker.net

import com.yuk.flibustatracker.data.Author
import com.yuk.flibustatracker.data.Book
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TrackerEngine {

    data class CheckResult(
        val status: String,         // success / no_updates / error
        val message: String,
        val authorName: String,
        val newBooks: List<Book>
    )

    private val foundFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", Locale.US)
    private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    private val lastCheckFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)

    private fun nameFromRssTitle(channelTitle: String): String? {
        val prefix = "Новости Флибусты - "
        if (channelTitle.startsWith(prefix)) {
            val name = channelTitle.substring(prefix.length).trim()
            if (name.isNotBlank()) return name
        }
        return null
    }

    suspend fun getAuthorName(authorUrl: String): String? {
        val rss = FlibustaClient.fetch(authorUrl.trimEnd('/') + "/rss")
        if (rss != null) {
            val feed = RssParser.parse(rss)
            val fromRss = feed?.channelTitle?.let { nameFromRssTitle(it) }
            if (fromRss != null) return fromRss
        }
        // fallback: страница автора -> <h1>
        val page = FlibustaClient.fetch(authorUrl) ?: return null
        val m = Regex("<h1[^>]*>(.*?)</h1>", RegexOption.DOT_MATCHES_ALL).find(page)
        if (m != null) {
            val clean = m.groupValues[1].replace(Regex("<[^>]+>"), "").trim()
            if (clean.length > 3) return clean
        }
        return null
    }

    suspend fun checkAuthor(author: Author, fullCheck: Boolean): CheckResult {
        val rssUrl = author.url.trimEnd('/') + "/rss"
        val xml = FlibustaClient.fetch(rssUrl)
            ?: return CheckResult("error", "Ошибка загрузки RSS", author.displayName(), emptyList())

        val feed = RssParser.parse(xml)
            ?: return CheckResult("error", "Ошибка разбора RSS", author.displayName(), emptyList())

        val authorName = nameFromRssTitle(feed.channelTitle) ?: author.displayName()

        // вычисляем дату отсечки
        val cutoff: Date = if (fullCheck) {
            minusDays(Date(), author.cutoffDays)
        } else {
            val lc = author.lastCheck?.let {
                try { lastCheckFmt.parse(it) } catch (e: Exception) { null }
            }
            lc ?: minusDays(Date(), author.cutoffDays)
        }

        val newBooks = mutableListOf<Book>()
        for (item in feed.items) {
            val pub = RssParser.parseDate(item.pubDate) ?: continue
            if (pub.after(cutoff)) {
                val links = item.link.takeIf { it.isNotBlank() }?.let { url ->
                    val page = FlibustaClient.fetch(url)
                    val l = if (page != null) BookPageParser.parse(page) else BookPageParser.Links(null, null, null, null)
                    delay(700)
                    l
                } ?: BookPageParser.Links(null, null, null, null)

                newBooks.add(
                    Book(
                        title = item.title,
                        author = authorName,
                        date = dateFmt.format(pub),
                        dateIso = isoFmt.format(pub),
                        url = item.link,
                        fb2Link = links.fb2,
                        pdfLink = links.pdf,
                        epubLink = links.epub,
                        mobiLink = links.mobi,
                        description = cleanDescription(item.description).take(200),
                        authorUrl = author.url,
                        foundDate = foundFmt.format(Date())
                    )
                )
            }
        }

        newBooks.sortByDescending { it.dateIso }

        return if (newBooks.isEmpty()) {
            CheckResult("no_updates", "Новых книг нет", authorName, emptyList())
        } else {
            CheckResult("success", "Найдено ${newBooks.size} новых книг", authorName, newBooks)
        }
    }

    private fun minusDays(from: Date, days: Int): Date {
        val cal = Calendar.getInstance()
        cal.time = from
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.time
    }

    private fun cleanDescription(html: String): String {
        return html.replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}