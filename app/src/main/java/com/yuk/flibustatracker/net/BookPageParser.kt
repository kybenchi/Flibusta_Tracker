package com.yuk.flibustatracker.net

/** Вытаскивает ссылки на скачивание со страницы книги (аналог get_download_links из app.py) */
object BookPageParser {
    data class Links(val fb2: String?, val pdf: String?, val epub: String?, val mobi: String?)

    fun parse(html: String): Links {
        return Links(
            fb2 = firstMatch(html, listOf(
                Regex("href=\"([^\"]*?/b/\\d+/fb2)\"", RegexOption.IGNORE_CASE),
                Regex("href=\"([^\"]*?\\.fb2\\.zip)\"", RegexOption.IGNORE_CASE),
                Regex("href=\"([^\"]*?\\.fb2)\"", RegexOption.IGNORE_CASE)
            )),
            pdf = firstMatch(html, listOf(
                Regex("href=\"([^\"]*?/b/\\d+/pdf)\"", RegexOption.IGNORE_CASE),
                Regex("href=\"([^\"]*?/b/\\d+/download)\"", RegexOption.IGNORE_CASE),
                Regex("href=\"([^\"]*?\\.pdf)\"", RegexOption.IGNORE_CASE)
            )),
            epub = firstMatch(html, listOf(
                Regex("href=\"([^\"]*?/b/\\d+/epub)\"", RegexOption.IGNORE_CASE),
                Regex("href=\"([^\"]*?\\.epub)\"", RegexOption.IGNORE_CASE)
            )),
            mobi = firstMatch(html, listOf(
                Regex("href=\"([^\"]*?/b/\\d+/mobi)\"", RegexOption.IGNORE_CASE),
                Regex("href=\"([^\"]*?\\.mobi)\"", RegexOption.IGNORE_CASE)
            ))
        )
    }

    private fun firstMatch(html: String, patterns: List<Regex>): String? {
        for (p in patterns) {
            val m = p.find(html)
            if (m != null) {
                var link = m.groupValues[1]
                if (link.startsWith("/")) link = "http://flibusta.is$link"
                return link
            }
        }
        return null
    }
}