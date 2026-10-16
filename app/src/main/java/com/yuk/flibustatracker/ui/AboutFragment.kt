package com.yuk.flibustatracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yuk.flibustatracker.BuildConfig
import com.yuk.flibustatracker.R

class AboutFragment : Fragment() {

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_about, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val variant = if (BuildConfig.PRELOAD_AUTHORS) "Full (с авторами)" else "Empty (чистый)"
        view.findViewById<TextView>(R.id.aboutVersion).text =
            "Версия ${BuildConfig.VERSION_NAME}  •  Сборка: $variant"
        view.findViewById<TextView>(R.id.aboutCredit).text = "© Yu_K 2026"
    }
}