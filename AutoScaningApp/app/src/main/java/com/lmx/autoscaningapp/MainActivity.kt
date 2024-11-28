package com.lmx.autoscaningapp


import android.content.ContentValues
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var textViewSifra: TextView
    private lateinit var editTextKod1: EditText
    private lateinit var editTextKod2: EditText
    private lateinit var editTextKod3: EditText
    private lateinit var saveButton: Button
    private val targetKeyCode = 103
    private lateinit var dataWedgeReceiver: DataWedgeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewSifra = findViewById(R.id.labelSkeniranaSifra)
        editTextKod1 = findViewById(R.id.editTextKod1)
        editTextKod2 = findViewById(R.id.editTextKod2)
        editTextKod3 = findViewById(R.id.editTextKod3)
        saveButton = findViewById(R.id.buttonClearAndSave)

        createDataWedgeProfile() // Kreiraj profil
        configureDataWedgeProfile()
        checkFieldsAndUpdateButton()

        updateFieldsState(false)

        saveButton.setOnClickListener {
            saveDataToFile()
        }

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

        dataWedgeReceiver = DataWedgeReceiver(textViewSifra)

        // Registracija BroadcastReceiver-a za DataWedge
        val filter = IntentFilter()
        filter.addAction("com.lmx.autoscaningapp.SCAN")
        registerReceiver(dataWedgeReceiver, filter, Context.RECEIVER_EXPORTED)


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
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == targetKeyCode) {
            Log.d("KeyPress", "Physical key with keyCode 103 pressed")
            // Ovdje možete dodati logiku za aktivaciju DataWedge-a ako je potrebno
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dataWedgeReceiver)
    }

    private fun showKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            it.clearFocus()
        }
    }

    private fun saveDataToFile() {
        val sifra = textViewSifra.text.toString()
        val kod1 = editTextKod1.text.toString()
        val kod2 = editTextKod2.text.toString()
        val kod3 = editTextKod3.text.toString()
        val data = "Šifra: $sifra\nKod 1: $kod1\nKod 2: $kod2\nKod 3: $kod3\n\n"

        val scanResultsDir = Environment.DIRECTORY_DOCUMENTS + "/ScanResults"
        val scanArchiveDir = Environment.DIRECTORY_DOWNLOADS + "/ScanArchive"

        ensureDirectoryExists(scanResultsDir)
        ensureDirectoryExists(scanArchiveDir)

        val scanResultsDirectory = File(Environment.getExternalStoragePublicDirectory(scanResultsDir).absolutePath)
        val existingFiles = scanResultsDirectory.listFiles { _, name -> name.endsWith(".txt") }

        if (existingFiles.isNullOrEmpty()) {
            // Ako nema postojeće datoteke, kreiraj novu
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val newFileName = "Baza_${timestamp}.txt"

            writeFile(scanResultsDir, newFileName, data)
            writeFile(scanArchiveDir, newFileName, data)
        } else {
            // Ako postoji datoteka, dodaj podatke u nju
            val existingFile = existingFiles.first() // Uzimamo jedinu datoteku
            appendToFile(scanResultsDir, existingFile.name, data)
            appendToFile(scanArchiveDir, existingFile.name, data)
        }

        textViewSifra.text = ""
        textViewSifra.hint = "Skeniraj kod"
        hideKeyboard()

        if(textViewSifra.text.toString().isEmpty()) {
            updateFieldsState(false)
        }
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
        sendBroadcast(createProfileIntent)
    }

    private fun configureDataWedgeProfile() {
        val configureProfileIntent = Intent()
        configureProfileIntent.action = "com.symbol.datawedge.api.ACTION"

        // APP_LIST configuration
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))

        // BARCODE plugin configuration
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true")
        val barcodeParams = Bundle()
        barcodeParams.putString("scanner_selection", "auto")
        barcodeParams.putString("scanner_input_enabled", "true")
        barcodeConfig.putBundle("PARAM_LIST", barcodeParams)

        // INTENT plugin configuration
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentParams = Bundle()
        intentParams.putString("intent_output_enabled", "true")
        intentParams.putString("intent_action", "com.lmx.autoscaningapp.SCAN")
        intentParams.putString("intent_delivery", "2") // Broadcast
        intentConfig.putBundle("PARAM_LIST", intentParams)

        // KEYSTROKE plugin configuration
        val keystrokeConfig = Bundle()
        keystrokeConfig.putString("PLUGIN_NAME", "KEYSTROKE")
        keystrokeConfig.putString("RESET_CONFIG", "true")
        val keystrokeParams = Bundle()
        keystrokeParams.putString("keystroke_output_enabled", "false")
        keystrokeConfig.putBundle("PARAM_LIST", keystrokeParams)

        // Combine all plugin configurations
        val pluginConfigList = ArrayList<Bundle>()
        pluginConfigList.add(barcodeConfig)
        pluginConfigList.add(intentConfig)
        pluginConfigList.add(keystrokeConfig)

        // Final configuration
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", "AutoScanningAppProfile")
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString("CONFIG_MODE", "UPDATE")
        profileConfig.putParcelableArrayList("PLUGIN_CONFIG", pluginConfigList)
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))

        configureProfileIntent.putExtra("com.symbol.datawedge.api.SET_CONFIG", profileConfig)

        Log.d("DataWedgeConfig", "Configuring profile: AutoScanningAppProfile")
        sendBroadcast(configureProfileIntent)
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
        Toast.makeText(this, "Datoteka $fileName spremljena u $directoryPath", Toast.LENGTH_SHORT).show()
    }

    private fun appendToFile(directoryPath: String, fileName: String, data: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(directoryPath), fileName)
        file.appendText(data)
        Toast.makeText(this, "Dodani podaci u datoteku $fileName", Toast.LENGTH_SHORT).show()
    }
}
