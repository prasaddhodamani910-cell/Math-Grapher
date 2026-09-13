package com.prasad.mathgrapher.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T> rememberDebouncedState(
    initialValue: T,
    delayMs: Long = 150L
): Pair<MutableState<T>, State<T>> {
    val rawState = remember { mutableStateOf(initialValue) }
    val debouncedState = remember { mutableStateOf(initialValue) }

    LaunchedEffect(rawState.value) {
        delay(delayMs)
        debouncedState.value = rawState.value
    }

    return Pair(rawState, debouncedState)
}

class DebouncedValue<T>(initialValue: T, private val delayMs: Long = 150L) {
    private var job: Job? = null
    var rawValue by mutableStateOf(initialValue)
        private set
    var debouncedValue by mutableStateOf(initialValue)
        private set
    
    fun update(value: T, scope: CoroutineScope) {
        rawValue = value
        job?.cancel()
        job = scope.launch {
            delay(delayMs)
            debouncedValue = value
        }
    }
}
