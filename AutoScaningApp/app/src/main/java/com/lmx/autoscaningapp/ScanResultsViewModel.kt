package com.lmx.autoscaningapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScanResultsViewModel : ViewModel() {
    private val _refreshTrigger = MutableLiveData<Unit>()

    fun notifyRefresh() {
        _refreshTrigger.value = Unit
    }
}
