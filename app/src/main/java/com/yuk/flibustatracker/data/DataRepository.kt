package com.yuk.flibustatracker.data

import android.content.Context
import com.google.gson.Gson
import com.yuk.flibustatracker.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataRepository(private val context: Context) {
    private val file = File(context.filesDir, "data.json")
    private val gson = Gson()
    private val addedFmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)

    fun load(): AppData {
        if (!file.exists()) {
            val data = AppData()
            if (BuildConfig.PRELOAD_AUTHORS) preloadAuthors(data)
            save(data)
            return data
        }
        return try {
            val parsed = gson.fromJson(file.readText(), AppData::class.java)
            parsed ?: AppData()
        } catch (e: Exception) {
            AppData()
        }
    }

    fun save(data: AppData) {
        try {
            file.writeText(gson.toJson(data))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun preloadAuthors(data: AppData) {
        val now = addedFmt.format(Date())
        for ((name, url, custom) in PreloadData.authors) {
            data.authors.add(
                Author(
                    url = url,
                    name = name,
                    customName = custom,
                    added = now,
                    cutoffDays = 300,
                    lastCheck = null
                )
            )
        }
    }
}