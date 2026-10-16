package com.yuk.flibustatracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuk.flibustatracker.R
import com.yuk.flibustatracker.TrackerViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SettingsFragment : Fragment() {

    private val vm: TrackerViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View? =
        i.inflate(R.layout.fragment_settings, c, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val cutoffInput = view.findViewById<EditText>(R.id.defaultCutoffInput)
        val lastCheckText = view.findViewById<android.widget.TextView>(R.id.lastCheckText)

        view.findViewById<Button>(R.id.btnSaveSettings).setOnClickListener {
            val days = cutoffInput.text.toString().toIntOrNull() ?: 300
            vm.setDefaultCutoff(days)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.data.collect { d ->
                        cutoffInput.setText(d.settings.defaultCutoffDays.toString())
                        lastCheckText.text = d.lastCheck?.let { "Последняя проверка: $it" }
                            ?: "Проверок ещё не было"
                    }
                }
                launch {
                    vm.toast.collect { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}