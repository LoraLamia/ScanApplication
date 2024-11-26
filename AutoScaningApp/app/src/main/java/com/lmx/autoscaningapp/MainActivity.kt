package com.lmx.autoscaningapp


import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var textViewSifra: TextView
    private lateinit var editTextKod1: EditText
    private lateinit var editTextKod2: EditText
    private lateinit var editTextKod3: EditText
    private lateinit var saveButton: Button
    private val targetKeyCode = 103
    private lateinit var dataWedgeReceiver: DataWedgeReciever

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewSifra = findViewById(R.id.labelSkeniranaSifra)
        editTextKod1 = findViewById(R.id.editTextKod1)
        editTextKod2 = findViewById(R.id.editTextKod2)
        editTextKod3 = findViewById(R.id.editTextKod3)
        saveButton = findViewById(R.id.buttonClearAndSave)

        updateFieldsState(false)

        saveButton.setOnClickListener {
            saveDataToFile()
            if(textViewSifra.text.toString().isEmpty()) {
                updateFieldsState(false)
            }
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

        dataWedgeReceiver = DataWedgeReciever(textViewSifra)

        // Registracija BroadcastReceiver-a za DataWedge
        val filter = IntentFilter()
        filter.addAction("com.lmx.autoscaningapp.SCAN")
        registerReceiver(dataWedgeReceiver, filter, RECEIVER_EXPORTED)
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

    fun saveDataToFile() {
        val sifra = textViewSifra.text.toString()
        val kod1 = editTextKod1.text.toString()
        val kod2 = editTextKod2.text.toString()
        val kod3 = editTextKod3.text.toString()
        val fileName = "autoscan_data.txt"
        val data = "Šifra: $sifra\nKod 1: $kod1\nKod 2: $kod2\nKod 3: $kod3\n\n"

        if (sifra.isEmpty() || kod1.isEmpty() || kod2.isEmpty() || kod3.isEmpty()) {
            Toast.makeText(this, "Sva polja moraju biti popunjena prije spremanja!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Otvori datoteku u APPEND modu - kreira ako ne postoji, dodaje ako postoji
            openFileOutput(fileName, Context.MODE_APPEND).use {
                it.write(data.toByteArray())
            }
            Toast.makeText(this, "Podaci spremljeni u $fileName", Toast.LENGTH_SHORT).show()

            textViewSifra.text = ""
            textViewSifra.hint = "Skeniraj kod"
            hideKeyboard()
        } catch (e: Exception) {
            Toast.makeText(this, "Greška pri spremanju podataka: ${e.message}", Toast.LENGTH_LONG).show()
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
}
