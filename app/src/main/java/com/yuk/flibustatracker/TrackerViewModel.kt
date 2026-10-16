package com.yuk.flibustatracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.yuk.flibustatracker.data.AppData
import com.yuk.flibustatracker.data.Author
import com.yuk.flibustatracker.data.DataRepository
import com.yuk.flibustatracker.net.TrackerEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = DataRepository(app)
    private val engine = TrackerEngine()
    private val stamp = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)

    val data = MutableLiveData<AppData>()
    val working = MutableLiveData(false)
    val toast = MutableLiveData<String>()

    init { reload() }

    fun reload() {
        data.value = repo.load()
    }

    private fun persist() {
        data.value?.let { repo.save(it) }
    }

    private fun now(): String = stamp.format(Date())

    fun addAuthor(url: String) {
        val u = url.trim()
        if (u.isBlank()) { toast.value = "URL не может быть пустым"; return }
        if (!u.contains("flibusta") || !u.contains("/a/")) {
            toast.value = "Некорректная ссылка (нужна ссылка на автора)"; return
        }
        val d = data.value ?: return
        if (d.authors.any { it.url == u }) { toast.value = "Автор уже добавлен"; return }

        working.value = true
        viewModelScope.launch {
            val name = withContext(Dispatchers.IO) { engine.getAuthorName(u) } ?: "Неизвестный автор"
            d.authors.add(Author(url = u, name = name, added = now(), cutoffDays = 300))
            persist()
            data.value = d
            working.value = false
            toast.value = "Автор \"$name\" добавлен"
        }
    }

    fun removeAuthor(url: String) {
        val d = data.value ?: return
        d.authors.removeAll { it.url == url }
        d.books.removeAll { it.authorUrl == url }
        persist(); data.value = d
        toast.value = "Автор удалён"
    }

    fun updateAuthorName(url: String, newName: String) {
        val d = data.value ?: return
        d.authors.firstOrNull { it.url == url }?.let {
            it.customName = newName.takeIf { n -> n.isNotBlank() }
            persist(); data.value = d
            toast.value = "Имя обновлено"
        }
    }

    fun updateAuthorCutoff(url: String, days: Int) {
        if (days < 1 || days > 3650) { toast.value = "Дни должны быть от 1 до 3650"; return }
        val d = data.value ?: return
        d.authors.firstOrNull { it.url == url }?.let {
            it.cutoffDays = days
            persist(); data.value = d
            toast.value = "Установлено $days дней"
        }
    }

    fun checkOne(author: Author, fullCheck: Boolean) {
        working.value = true
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { engine.checkAuthor(author, fullCheck) }
            val d = data.value ?: return@launch
            val target = d.authors.firstOrNull { it.url == author.url }
            target?.let {
                it.lastCheck = now()
                if (result.authorName != "Неизвестный автор") it.name = result.authorName
            }
            for (book in result.newBooks) {
                if (d.books.none { it.url == book.url }) d.books.add(book)
            }
            d.lastCheck = now()
            persist(); data.value = d
            working.value = false
            toast.value = "${author.displayName()}: ${result.message}"
        }
    }

    fun checkAll() {
        val d = data.value ?: return
        if (d.authors.isEmpty()) { toast.value = "Список авторов пуст"; return }
        working.value = true
        viewModelScope.launch {
            var totalNew = 0
            for (author in d.authors) {
                val result = withContext(Dispatchers.IO) { engine.checkAuthor(author, false) }
                val target = d.authors.firstOrNull { it.url == author.url }
                target?.let {
                    it.lastCheck = now()
                    if (result.authorName != "Неизвестный автор") it.name = result.authorName
                }
                for (book in result.newBooks) {
                    if (d.books.none { it.url == book.url }) { d.books.add(book); totalNew++ }
                }
            }
            d.lastCheck = now()
            persist(); data.value = d
            working.value = false
            toast.value = "Проверено авторов: ${d.authors.size}, новых книг: $totalNew"
        }
    }

    fun refreshNames() {
        val d = data.value ?: return
        if (d.authors.isEmpty()) { toast.value = "Список авторов пуст"; return }
        working.value = true
        viewModelScope.launch {
            var updated = 0
            for (author in d.authors) {
                val name = withContext(Dispatchers.IO) { engine.getAuthorName(author.url) }
                if (!name.isNullOrBlank()) { author.name = name; updated++ }
            }
            persist(); data.value = d
            working.value = false
            toast.value = "Имена обновлены: $updated из ${d.authors.size}"
        }
    }

    fun resetLastCheck() {
        val d = data.value ?: return
        d.authors.forEach { it.lastCheck = null }
        persist(); data.value = d
        toast.value = "Дата последней проверки сброшена"
    }

    fun clearBooks() {
        val d = data.value ?: return
        d.books.clear()
        persist(); data.value = d
        toast.value = "Список книг очищен"
    }

    fun setDefaultCutoff(days: Int) {
        val d = data.value ?: return
        d.settings.defaultCutoffDays = days
        persist(); data.value = d
        toast.value = "Настройки сохранены"
    }
}