package com.lmx.autoscaningapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpdateCountReceiver() : BroadcastReceiver() {

    constructor(fragment: MainFragment) : this() {
        this.fragment = fragment
    }

    private var fragment: MainFragment? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        fragment?.updateRecordCount()
            ?: run {
                println("Fragment is not attached, nothing to update.")
            }
    }
}
