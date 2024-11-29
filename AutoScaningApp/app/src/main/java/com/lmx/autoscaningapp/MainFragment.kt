package com.lmx.autoscaningapp

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainFragment : Fragment() {

    private lateinit var textViewSifra: TextView
    private lateinit var editTextKod1: EditText
    private lateinit var editTextKod2: EditText
    private lateinit var editTextKod3: EditText
    private lateinit var saveButton: Button
    private lateinit var dataWedgeReceiver: DataWedgeReceiver
    private lateinit var scanResultsViewModel: ScanResultsViewModel
    private lateinit var recordCountTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        textViewSifra = view.findViewById(R.id.labelSkeniranaSifra)
        editTextKod1 = view.findViewById(R.id.editTextKod1)
        editTextKod2 = view.findViewById(R.id.editTextKod2)
        editTextKod3 = view.findViewById(R.id.editTextKod3)
        saveButton = view.findViewById(R.id.buttonClearAndSave)
        recordCountTextView = view.findViewById(R.id.recordCountTextView)
        updateRecordCount()


        textViewSifra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    updateFieldsState(true)

                    editTextKod1.requestFocus()
                    showKeyboard(editTextKod1)
                }
            }
        })

        scanResultsViewModel = requireActivity().run {
            androidx.lifecycle.ViewModelProvider(this)[ScanResultsViewModel::class.java]
        }

        saveButton.setOnClickListener {
            saveDataToFile()
        }

        createDataWedgeProfile()
        configureDataWedgeProfile()
        checkFieldsAndUpdateButton()

        updateFieldsState(false)

        dataWedgeReceiver = DataWedgeReceiver(textViewSifra)

        val filter = IntentFilter()
        filter.addAction("com.lmx.autoscaningapp.SCAN")
        requireContext().registerReceiver(dataWedgeReceiver, filter, Context.RECEIVER_EXPORTED)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkFieldsAndUpdateButton()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        editTextKod1.addTextChangedListener(textWatcher)
        editTextKod2.addTextChangedListener(textWatcher)
        editTextKod3.addTextChangedListener(textWatcher)
        textViewSifra.addTextChangedListener(textWatcher)

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(dataWedgeReceiver)
    }

    private fun showKeyboard(editText: EditText) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    }

    private fun saveDataToFile() {
        val sifra = textViewSifra.text.toString()
        val kod1 = editTextKod1.text.toString()
        val kod2 = editTextKod2.text.toString()
        val kod3 = editTextKod3.text.toString()
        val data = "$sifra     $kod1 $kod2 $kod3\n" //5 razmaka izmedu sifre i koda 1, izmedu kodova po 1 razmak

        val scanResultsDir = Environment.DIRECTORY_DOCUMENTS + "/ScanResults"
        val scanArchiveDir = Environment.DIRECTORY_DOWNLOADS + "/ScanArchive"

        ensureDirectoryExists(scanResultsDir)
        ensureDirectoryExists(scanArchiveDir)

        val scanResultsDirectory = File(Environment.getExternalStoragePublicDirectory(scanResultsDir).absolutePath)
        val existingFiles = scanResultsDirectory.listFiles { _, name -> name.endsWith(".txt") }

        if (existingFiles.isNullOrEmpty()) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val newFileName = "Baza_${timestamp}.txt"

            writeFile(scanResultsDir, newFileName, data)
            writeFile(scanArchiveDir, newFileName, data)
        } else {
            val existingFile = existingFiles.first()
            appendToFile(scanResultsDir, existingFile.name, data)
            appendToFile(scanArchiveDir, existingFile.name, data)
        }

        textViewSifra.text = ""
        textViewSifra.hint = "Skeniraj kod"
        hideKeyboard()

        if(textViewSifra.text.toString().isEmpty()) {
            updateFieldsState(false)
        }

        scanResultsViewModel.notifyRefresh()
        updateRecordCount()
    }

    private fun updateFieldsState(enabled: Boolean) {
        val textColor = if (enabled) R.color.black else R.color.gray

        editTextKod1.apply {
            isEnabled = enabled
            setTextColor(resources.getColor(textColor, null))
        }
        editTextKod2.apply {
            isEnabled = enabled
            setTextColor(resources.getColor(textColor, null))
        }
        editTextKod3.apply {
            isEnabled = enabled
            setTextColor(resources.getColor(textColor, null))
        }
    }

    private fun checkFieldsAndUpdateButton() {
        val isSifraNotEmpty = textViewSifra.text.toString().isNotEmpty()
        val isKod1NotEmpty = editTextKod1.text.toString().isNotEmpty()
        val isKod2NotEmpty = editTextKod2.text.toString().isNotEmpty()
        val isKod3NotEmpty = editTextKod3.text.toString().isNotEmpty()

        saveButton.isEnabled = isSifraNotEmpty && isKod1NotEmpty && isKod2NotEmpty && isKod3NotEmpty
    }

    private fun createDataWedgeProfile() {
        val createProfileIntent = Intent()
        createProfileIntent.action = "com.symbol.datawedge.api.ACTION"
        createProfileIntent.putExtra("com.symbol.datawedge.api.CREATE_PROFILE", "AutoScanningAppProfile")
        Log.d("DataWedgeConfig", "Creating profile: AutoScanningAppProfile")
        requireContext().sendBroadcast(createProfileIntent)
    }

    private fun configureDataWedgeProfile() {
        val configureProfileIntent = Intent()
        configureProfileIntent.action = "com.symbol.datawedge.api.ACTION"

        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", requireContext().packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))

        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true")
        val barcodeParams = Bundle()
        barcodeParams.putString("scanner_selection", "auto")
        barcodeParams.putString("scanner_input_enabled", "true")
        barcodeConfig.putBundle("PARAM_LIST", barcodeParams)

        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentParams = Bundle()
        intentParams.putString("intent_output_enabled", "true")
        intentParams.putString("intent_action", "com.lmx.autoscaningapp.SCAN")
        intentParams.putString("intent_delivery", "2") // Broadcast
        intentConfig.putBundle("PARAM_LIST", intentParams)

        val keystrokeConfig = Bundle()
        keystrokeConfig.putString("PLUGIN_NAME", "KEYSTROKE")
        keystrokeConfig.putString("RESET_CONFIG", "true")
        val keystrokeParams = Bundle()
        keystrokeParams.putString("keystroke_output_enabled", "false")
        keystrokeConfig.putBundle("PARAM_LIST", keystrokeParams)

        val pluginConfigList = ArrayList<Bundle>()
        pluginConfigList.add(barcodeConfig)
        pluginConfigList.add(intentConfig)
        pluginConfigList.add(keystrokeConfig)

        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", "AutoScanningAppProfile")
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString("CONFIG_MODE", "UPDATE")
        profileConfig.putParcelableArrayList("PLUGIN_CONFIG", pluginConfigList)
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))

        configureProfileIntent.putExtra("com.symbol.datawedge.api.SET_CONFIG", profileConfig)

        Log.d("DataWedgeConfig", "Configuring profile: AutoScanningAppProfile")
        requireContext().sendBroadcast(configureProfileIntent)
    }

    private fun ensureDirectoryExists(directoryPath: String) {
        val directory = File(Environment.getExternalStoragePublicDirectory(directoryPath).absolutePath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    private fun writeFile(directoryPath: String, fileName: String, data: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(directoryPath), fileName)
        file.writeText(data)
        Toast.makeText(requireContext(), "Datoteka $fileName spremljena u $directoryPath", Toast.LENGTH_SHORT).show()
    }

    private fun appendToFile(directoryPath: String, fileName: String, data: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(directoryPath), fileName)
        file.appendText(data)
        Toast.makeText(requireContext(), "Dodani podaci u datoteku $fileName", Toast.LENGTH_SHORT).show()
    }

    private fun updateRecordCount() {
        val scanResultsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "ScanResults"
        )

        if (scanResultsDir.exists() && scanResultsDir.isDirectory) {
            val txtFiles = scanResultsDir.listFiles { _, name -> name.endsWith(".txt") }

            if (!txtFiles.isNullOrEmpty()) {
                val latestFile = txtFiles.maxByOrNull { it.lastModified() }
                if (latestFile != null) {
                    val lineCount = latestFile.readLines().size
                    recordCountTextView.text = "Broj zapisa: $lineCount"
                    return
                }
            }
        } else {
            recordCountTextView.text = "Broj zapisa: 0"
        }
    }

}
