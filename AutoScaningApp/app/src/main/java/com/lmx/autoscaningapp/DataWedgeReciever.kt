package com.lmx.autoscaningapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.EditText

class DataWedgeReciever(private val editText: EditText) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == "com.lmx.autoscaningapp.SCAN") {

            val barcodeData = intent.getStringExtra("com.symbol.datawedge.data_string")
            Log.d("DataWedge", "Received barcode: $barcodeData")



            editText.post {
                editText.setText(barcodeData ?: "")
            }
        } else {
            Log.w("DataWedge", "Received null Intent")
        }
    }
}