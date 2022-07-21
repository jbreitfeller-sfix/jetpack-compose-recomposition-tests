package com.stitchfix.recomposition

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NameListViewModel : ViewModel() {

    private val _state = MutableStateFlow(NameListState(listOf("Aaron", "Bob", "Claire")))

    val state: StateFlow<NameListState>
        get() = _state.asStateFlow()

    fun addName() {
        _state.value = NameListState(names = _state.value.names + "Daisy")
    }

    fun handleNameClick() {
        Log.e("*****", "Hi from a name click")
    }
}

data class NameListState(
    val names: List<String>
)
