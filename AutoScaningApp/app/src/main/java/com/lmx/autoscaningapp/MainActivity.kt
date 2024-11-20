package com.lmx.autoscaningapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private val targetKeyCode = 103

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editTextText)

        // Registracija BroadcastReceiver-a za DataWedge
        val filter = IntentFilter()
        filter.addAction("com.lmx.autoscaningapp.ACTION")
        registerReceiver(dataWedgeReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == targetKeyCode) {
            Log.d("KeyPress", "Physical key with keyCode 103 pressed")
            // Ovdje možete dodati logiku za aktivaciju DataWedge-a ako je potrebno
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private val dataWedgeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val barcodeData = it.getStringExtra("com.symbol.datawedge.data_string")
                Log.d("DataWedge", "Received barcode: $barcodeData")

                // Postavljanje skeniranog koda u EditText
                runOnUiThread {
                    editText.setText(barcodeData ?: "")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dataWedgeReceiver)
    }
}
