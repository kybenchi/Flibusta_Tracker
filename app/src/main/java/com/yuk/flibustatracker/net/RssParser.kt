package com.yuk.flibustatracker.net

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RssParser {
    data class RssItem(val title: String, val link: String, val pubDate: String, val description: String)
    data class RssFeed(val channelTitle: String, val items: List<RssItem>)

    fun parse(xml: String): RssFeed? {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var channelTitle = ""
            val items = mutableListOf<RssItem>()

            var inItem = false
            var inChannel = false
            var tag = ""
            var title = ""; var link = ""; var pub = ""; var desc = ""

            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        tag = parser.name.lowercase()
                        when (tag) {
                            "channel" -> inChannel = true
                            "item" -> {
                                inItem = true
                                title = ""; link = ""; pub = ""; desc = ""
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            if (inItem) {
                                when (tag) {
                                    "title" -> if (title.isEmpty()) title = text
                                    "link" -> if (link.isEmpty()) link = text
                                    "pubdate" -> if (pub.isEmpty()) pub = text
                                    "description" -> if (desc.isEmpty()) desc = text
                                }
                            } else if (inChannel && tag == "title" && channelTitle.isEmpty()) {
                                channelTitle = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (parser.name.lowercase()) {
                            "item" -> {
                                items.add(RssItem(title, link, pub, desc))
                                inItem = false
                            }
                            "channel" -> inChannel = false
                        }
                        tag = ""
                    }
                }
                event = parser.next()
            }
            RssFeed(channelTitle, items)
        } catch (e: Exception) {
            null
        }
    }

    private val formats = arrayOf(
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss zzz",
        "dd MMM yyyy HH:mm:ss Z",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    fun parseDate(raw: String): Date? {
        for (pattern in formats) {
            try {
                val fmt = SimpleDateFormat(pattern, Locale.US)
                return fmt.parse(raw)
            } catch (_: Exception) {
            }
        }
        return null
    }
}