package com.stitchfix.recomposition

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.stitchfix.domain.DomainClass

private val LOG_TAG = "*******RECOMPOSED"

@Composable
fun RecompositionTests() {
    Column {
        VmTest("ViewModel Lambda Test") {
            { it.printHi() }
        }
        VmTest("Method reference test") {
            it::printHi
        }
        VmTest("Remembered lambda test") {
            remember { { it.printHi() } }
        }
        VmTest("Static function test") {
            { print() }
        }

        LambdaUsingMutableStateTest()
        DomainClassTest()
        UiClassTest()
    }
}

@Composable
private fun VmTest(
    buttonText: String,
    onTextClickFactory: @Composable (StringListViewModel) -> (() -> Unit),
) {
    val viewModel = remember { StringListViewModel() }
    val state by viewModel.state.collectAsState()
    StringListColumn(
        strings = state.strings,
        buttonName = buttonText,
        onButtonClick = viewModel::updateStrings,
        onTextClick = onTextClickFactory(viewModel),
    )
}

fun print() {
    Log.e("****", "Hi")
}

@Composable
fun LambdaUsingMutableStateTest() {
    var state by remember { mutableStateOf(listOf("1", "2", "3")) }
    StringListColumn(
        strings = state,
        buttonName = "Recompose lambda using mutable state",
        onButtonClick = { state = state + "new" },
        onTextClick = { state = state + "new" },
    )
}

@Composable
private fun StringListColumn(
    strings: List<String>,
    buttonName: String,
    onButtonClick: () -> Unit,
    onTextClick: () -> Unit,
) {
    Column {
        Row {
            strings.forEach { TextThatLogsComposition(text = "$it ", onClick = onTextClick) }
        }
        Button(onClick = onButtonClick) { Text(buttonName) }
    }
}



@Composable
private fun TextThatLogsComposition(text: String, onClick: () -> Unit) {
    Log.e(LOG_TAG, text)
    Text(text, modifier = Modifier.clickable(onClick = onClick))
}

@Composable
fun <T : Any> ClassArgComposable(value: T, content: @Composable (T) -> Unit) {
    var count by remember { mutableStateOf(1) }

    Column {
        //content() shouldn't recompose when count changes since value isn't changing
        content(value)
        Text("Click Count: $count")
        Button(onClick = { count++ }) {
            Text("Recompose ${value::class.simpleName} test")
        }
    }
}


@Composable
fun DomainClassTest() {
    ClassArgComposable(value = DomainClass("DomainClass")) {
        Log.e(LOG_TAG, "DomainComposable is recomposing")
        Text(it.value)
    }
}


//Exact copy of DomainClass
data class UiModuleClass(val value: String)

@Composable
fun UiClassTest() {
    ClassArgComposable(value = UiModuleClass("UiClass")) {
        Log.e(LOG_TAG, "UiComposable is recomposing")
        Text(it.value)
    }
}
