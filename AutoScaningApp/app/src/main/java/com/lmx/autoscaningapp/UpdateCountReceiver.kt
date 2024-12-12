package com.lmx.autoscaningapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpdateCountReceiver(private val fragment: MainFragment) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        fragment.updateRecordCount()
    }
}
