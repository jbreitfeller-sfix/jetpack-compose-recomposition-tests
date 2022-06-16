package com.stitchfix.recomposition

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StringListViewModel : ViewModel() {

    private val _state = MutableStateFlow(StringListState(listOf("1", "2", "3")))

    val state: StateFlow<StringListState>
        get() = _state.asStateFlow()

    fun updateStrings() {
        _state.value = StringListState(strings = _state.value.strings + "new")
    }

    fun printHi() {
        Log.e("*****", "Hi from a text click")
    }
}

data class StringListState(
    val strings: List<String>
)
