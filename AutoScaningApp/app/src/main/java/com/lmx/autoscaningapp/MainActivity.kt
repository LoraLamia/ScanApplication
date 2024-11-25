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

        editTextKod1.isEnabled = false
        editTextKod2.isEnabled = false
        editTextKod3.isEnabled = false


        saveButton.setOnClickListener {
            saveDataToFile()
        }

        textViewSifra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {

                    editTextKod1.isEnabled = true
                    editTextKod2.isEnabled = true
                    editTextKod3.isEnabled = true

                    editTextKod1.requestFocus()
                    showKeyboard(editTextKod1)
                }
            }
        })

        editTextKod1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty() && editTextKod1.text.toString() == textViewSifra.text.toString()) {
                    editTextKod1.text.clear()
                }
                //Bez ovoga text se prepisuje
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
        // Dohvati tekst iz svih polja
        val sifra = textViewSifra.text.toString()
        val kod1 = editTextKod1.text.toString()
        val kod2 = editTextKod2.text.toString()
        val kod3 = editTextKod3.text.toString()
        val fileName = "autoscan_data.txt"
        val data = "Šifra: $sifra\nKod 1: $kod1\nKod 2: $kod2\nKod 3: $kod3\n\n"

        // Formatiraj podatke za spremanje

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

            editTextKod1.text.clear()
            editTextKod2.text.clear()
            editTextKod3.text.clear()

            textViewSifra.text = "Skeniraj kod"

            editTextKod1.isEnabled = false
            editTextKod2.isEnabled = false
            editTextKod3.isEnabled = false

            hideKeyboard()
        } catch (e: Exception) {
            Toast.makeText(this, "Greška pri spremanju podataka: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}
