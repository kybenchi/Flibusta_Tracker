package com.yuk.flibustatracker.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.yuk.flibustatracker.net.FlibustaClient

object DownloadHelper {

    fun download(context: Context, url: String, referer: String, title: String) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val req = DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription("Скачивание с Флибусты")
                .setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
                .setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
            req.addRequestHeader("Referer", referer)
            req.addRequestHeader("User-Agent", FlibustaClient.UA)
            req.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, safeFileName(title, url)
            )
            dm.enqueue(req)
            Toast.makeText(context, "Скачивание началось → папка Downloads", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка скачивания: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun safeFileName(title: String, url: String): String {
        val ext = when {
            url.contains("/fb2") || url.endsWith(".fb2.zip") || url.endsWith(".fb2") -> ".fb2.zip"
            url.contains("/pdf") || url.endsWith(".pdf") -> ".pdf"
            url.contains("/epub") || url.endsWith(".epub") -> ".epub"
            url.contains("/mobi") || url.endsWith(".mobi") -> ".mobi"
            else -> ""
        }
        val clean = title.replace(Regex("[<>:\"/\\\\|?*]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(100)
        return (if (clean.isBlank()) "book" else clean) + ext
    }
}