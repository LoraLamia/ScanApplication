package com.lmx.autoscaningapp

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.File

class ScanResultsFragment : Fragment() {

    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_scan_results, container, false)
        textView = view.findViewById(R.id.textFileContent)

        loadScanResults()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadScanResults()
    }

    private fun loadScanResults() {
        val scanResultsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "ScanResults"
        )

        if (scanResultsDir.exists() && scanResultsDir.isDirectory) {
            val txtFiles = scanResultsDir.listFiles { _, name -> name.endsWith(".txt") }

            if (!txtFiles.isNullOrEmpty()) {
                val latestFile = txtFiles.maxByOrNull { it.lastModified() }
                textView.text = latestFile?.readText() ?: "Nema dostupnih zapisa."
            } else {
                textView.text = "Nema dostupnih zapisa."
            }
        } else {
            textView.text = "Nema dostupnih zapisa."
        }
    }
}

