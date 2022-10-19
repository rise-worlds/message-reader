package com.example.messagereader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData


class SmsViewModel(application: Application?) : AndroidViewModel(application!!) {

    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private val mAllWords: LiveData<List<SmsItem>> = SmsRepository.getInstance().getAll()

    val allWords: LiveData<List<SmsItem>>
        get() = mAllWords

    fun insert(item: SmsItem?) {
        if (item != null) {
            SmsRepository.getInstance().insert(item)
        }
    }
}
