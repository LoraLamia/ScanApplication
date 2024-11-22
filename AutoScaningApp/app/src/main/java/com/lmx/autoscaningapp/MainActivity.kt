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
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editTextSifra1: EditText
    private lateinit var editTextKod1: EditText
    private val targetKeyCode = 103
    private lateinit var dataWedgeReceiver: DataWedgeReciever

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextSifra1 = findViewById(R.id.editTextSifra1)
        editTextKod1 = findViewById<EditText>(R.id.editTextKod1)
        val editTextKod2 = findViewById<EditText>(R.id.editTextKod2)
        val editTextKod3 = findViewById<EditText>(R.id.editTextKod3)

        editTextSifra1.addTextChangedListener(object : TextWatcher {
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
                if (!s.isNullOrEmpty()) {
                    if(editTextKod1.text.toString() == editTextSifra1.text.toString()) {
                        editTextKod1.text.clear()
                    }

                }
            }
        })


        dataWedgeReceiver = DataWedgeReciever(editTextSifra1)

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
}
