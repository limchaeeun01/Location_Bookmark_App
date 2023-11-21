package com.chaeni__beam.a409_lab_project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel : ViewModel() {
    private val repo = Repo()
    fun fetchData(): LiveData<MutableList<MyPlaceData>> {
        val mutableData = MutableLiveData<MutableList<MyPlaceData>>()
        repo.getData().observeForever{
            mutableData.value = it
        }
        return mutableData
    }
}