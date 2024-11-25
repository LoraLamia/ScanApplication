package com.lmx.autoscaningapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView

class DataWedgeReciever(private val textViewSifra: TextView) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == "com.lmx.autoscaningapp.SCAN") {
            val barcodeData = intent.getStringExtra("com.symbol.datawedge.data_string")
            Log.d("DataWedge", "Received barcode: $barcodeData")

            if (barcodeData != null) {
                textViewSifra.post {
                    val activity = textViewSifra.context as MainActivity
                    activity.clearKodFields()

                    textViewSifra.setText(barcodeData)
                }
            }
        } else {
            Log.w("DataWedge", "Received null Intent")
        }
    }

}