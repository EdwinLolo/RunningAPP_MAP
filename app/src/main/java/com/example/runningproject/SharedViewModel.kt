package com.example.runningproject

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _totalDistance = MutableLiveData<String>()
    val totalDistance: LiveData<String> get() = _totalDistance

    private val _totalPace = MutableLiveData<String>()
    val totalPace: LiveData<String> get() = _totalPace

    private val _totalCalories = MutableLiveData<String>()
    val totalCalories: LiveData<String> get() = _totalCalories

    fun setUsername(name: String) {
        _username.value = name
    }

    fun setTotalDistance(distance: String) {
        _totalDistance.value = distance
    }

    fun setTotalPace(pace: String) {
        _totalPace.value = pace
    }

    fun setTotalCalories(calories: String) {
        _totalCalories.value = calories
    }
}