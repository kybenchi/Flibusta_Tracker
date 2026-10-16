package com.yuk.flibustatracker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yuk.flibustatracker.R
import com.yuk.flibustatracker.data.Author

class AuthorsAdapter(
    private val onCheckNew: (Author) -> Unit,
    private val onCheckFull: (Author) -> Unit,
    private val onSaveCutoff: (Author, Int) -> Unit,
    private val onEditName: (Author) -> Unit,
    private val onOpenUrl: (Author) -> Unit,
    private val onDelete: (Author) -> Unit
) : RecyclerView.Adapter<AuthorsAdapter.VH>() {

    private val items = mutableListOf<Author>()

    fun submit(list: List<Author>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.authorName)
        val badge: TextView = v.findViewById(R.id.customBadge)
        val url: TextView = v.findViewById(R.id.authorUrl)
        val meta: TextView = v.findViewById(R.id.authorMeta)
        val cutoff: EditText = v.findViewById(R.id.cutoffInput)
        val btnEdit: Button = v.findViewById(R.id.btnEditName)
        val btnNew: Button = v.findViewById(R.id.btnCheckNew)
        val btnFull: Button = v.findViewById(R.id.btnCheckFull)
        val btnSave: Button = v.findViewById(R.id.btnSaveCutoff)
        val btnDel: Button = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_author, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]
        holder.name.text = a.displayName()
        holder.badge.visibility = if (a.customName.isNullOrBlank()) View.GONE else View.VISIBLE
        holder.url.text = a.url

        val last = a.lastCheck ?: "Ещё не проверялся"
        holder.meta.text = "Добавлен: ${a.added}\nПоследняя проверка: $last"
        holder.cutoff.setText(a.cutoffDays.toString())

        holder.btnEdit.setOnClickListener { onEditName(a) }
        holder.btnNew.setOnClickListener { onCheckNew(a) }
        holder.btnFull.setOnClickListener { onCheckFull(a) }
        holder.btnSave.setOnClickListener {
            val days = holder.cutoff.text.toString().toIntOrNull() ?: a.cutoffDays
            onSaveCutoff(a, days)
        }
        holder.btnDel.setOnClickListener { onDelete(a) }
        holder.url.setOnClickListener { onOpenUrl(a) }
    }
}