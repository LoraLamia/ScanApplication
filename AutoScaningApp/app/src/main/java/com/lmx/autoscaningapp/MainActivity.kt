package com.lmx.autoscaningapp


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
    private lateinit var dataWedgeReceiver: DataWedgeReciever

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editTextText)

        dataWedgeReceiver = DataWedgeReciever(editText)

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
            //jfujfjfjhshshs
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dataWedgeReceiver)
    }
}
