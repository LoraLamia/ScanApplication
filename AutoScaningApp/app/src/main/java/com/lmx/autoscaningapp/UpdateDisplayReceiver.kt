package com.lmx.autoscaningapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpdateDisplayReceiver() : BroadcastReceiver() {

    constructor(fragment: ScanResultsFragment) : this() {
        this.fragment = fragment
    }

    private var fragment: ScanResultsFragment? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        fragment?.loadScanResults()
            ?: run {
                println("Fragment is not attached, nothing to update.")
            }
    }
}