package com.yuk.flibustatracker.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yuk.flibustatracker.R
import com.yuk.flibustatracker.TrackerViewModel
import com.yuk.flibustatracker.data.Author
import com.yuk.flibustatracker.ui.adapters.AuthorsAdapter
import kotlinx.coroutines.launch

class AuthorsFragment : Fragment() {

    private val vm: TrackerViewModel by activityViewModels()
    private lateinit var adapter: AuthorsAdapter
    private lateinit var progress: ProgressBar
    private lateinit var countText: TextView

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_authors, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rv = view.findViewById<RecyclerView>(R.id.authorsRecycler)
        progress = view.findViewById(R.id.authorsProgress)
        countText = view.findViewById(R.id.authorsCount)

        adapter = AuthorsAdapter(
            onCheckNew = { vm.checkOne(it, false) },
            onCheckFull = { vm.checkOne(it, true) },
            onSaveCutoff = { a, d -> vm.updateAuthorCutoff(a.url, d) },
            onEditName = { showEditNameDialog(it) },
            onOpenUrl = { openUrl(it.url) },
            onDelete = { confirmDelete(it) }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<Button>(R.id.btnAdd).setOnClickListener { showAddDialog() }
        view.findViewById<Button>(R.id.btnCheckAll).setOnClickListener { vm.checkAll() }
        view.findViewById<Button>(R.id.btnRefreshNames).setOnClickListener { vm.refreshNames() }
        view.findViewById<Button>(R.id.btnResetCheck).setOnClickListener { vm.resetLastCheck() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.data.collect { d ->
                        adapter.submit(d.authors)
                        countText.text = "Список авторов (${d.authors.size})"
                    }
                }
                launch {
                    vm.working.collect { progress.visibility = if (it) View.VISIBLE else View.GONE }
                }
                launch {
                    vm.toast.collect { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAddDialog() {
        val input = EditText(requireContext()).apply {
            hint = "http://flibusta.is/a/XXXX"
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Добавить автора")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ -> vm.addAuthor(input.text.toString()) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showEditNameDialog(author: Author) {
        val input = EditText(requireContext()).apply {
            setText(author.customName ?: author.name ?: "")
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Отображаемое имя")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                vm.updateAuthorName(author.url, input.text.toString())
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun confirmDelete(author: Author) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить автора?")
            .setMessage("${author.displayName()}\nБудут удалены и его найденные книги.")
            .setPositiveButton("Удалить") { _, _ -> vm.removeAuthor(author.url) }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
        }
    }
}