package com.stitchfix.recomposition

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.stitchfix.domain.DomainFullName
import kotlinx.coroutines.launch

private val LOG_TAG = "*******RECOMPOSED"

@Composable
fun RecompositionTests() {

    Column {

        ViewModelLambdaTest()
        MethodReferenceTest()
        RememberedLambdaTest()
        StaticFunctionTest()
        LambdaUsingMutableStateTest()

        DomainClassTest()
        UiClassTest()

        LazyListRecomposition()
    }
}

@Composable
private fun ViewModelLambdaTest() {
    val viewModel = remember { NameListViewModel() }
    val state by viewModel.state.collectAsState()

    NameColumnWithButton(
        strings = state.names,
        onButtonClick = viewModel::addName,
        buttonName = "ViewModel Lambda Test",
        onTextClick = { viewModel.handleNameClick() }
    )
}

@Composable
private fun MethodReferenceTest() {
    val viewModel = remember { NameListViewModel() }
    val state by viewModel.state.collectAsState()

    NameColumnWithButton(
        strings = state.names,
        onButtonClick = viewModel::addName,
        buttonName = "Method Reference Test",
        onTextClick = viewModel::handleNameClick
    )
}

@Composable
private fun RememberedLambdaTest() {
    val viewModel = remember { NameListViewModel() }
    val state by viewModel.state.collectAsState()

    NameColumnWithButton(
        strings = state.names,
        onButtonClick = viewModel::addName,
        buttonName = "Remembered Lambda Test",
        onTextClick = remember { { viewModel.handleNameClick() } }
    )
}


@Composable
private fun StaticFunctionTest() {

    val viewModel = remember { NameListViewModel() }
    val state by viewModel.state.collectAsState()

    NameColumnWithButton(
        strings = state.names,
        onButtonClick = viewModel::addName,
        buttonName = "Static Function Test",
        onTextClick = { print() }
    )
}

fun print() {
    Log.e("****", "Hi")
}

@Composable
fun LambdaUsingMutableStateTest() {
    var state by remember { mutableStateOf(listOf("Aaron", "Bob", "Claire")) }
    NameColumnWithButton(
        strings = state,
        buttonName = "Recompose Lambda Capturing @Stable",
        onButtonClick = { state = state + "Daisy" },
        onTextClick = { state = state + "Daisy" },
    )
}

@Composable
private fun NameColumnWithButton(
    strings: List<String>,
    buttonName: String,
    onButtonClick: () -> Unit,
    onTextClick: () -> Unit,
) {
    Column {
        Row {
            strings.forEach { CompositionTrackingName(name = "$it ", onClick = onTextClick) }
        }
        Button(onClick = onButtonClick) { Text(buttonName) }
    }
}


@Composable
private fun CompositionTrackingName(name: String, onClick: () -> Unit) {
    Log.e(LOG_TAG, name)
    Text(name, modifier = Modifier.clickable(onClick = onClick))
}

@Composable
fun DomainClassTest() {
    val name = remember { DomainFullName("John", "Doe") }
    var count by remember { mutableStateOf(1) }

    Column {
        //This shouldn't recompose when count changes since [name] isn't changing
        //but it does since DomainFullName is not @Stable.
        NameFromDomainClass(name)
        Text("Click Count: $count")
        Button(onClick = { count++ }) {
            Text("Recompose domain module class test")
        }
    }
}

@Composable
private fun NameFromDomainClass(domainObject: DomainFullName) {
    Log.e(LOG_TAG, "DomainFullName recomposed")
    Text(domainObject.first + " " + domainObject.last)
}

//Compose level state
data class ComposeFullName(val first: String, val last: String)

fun DomainFullName.toComposeFullName() = ComposeFullName(first = first, last = last)

@Composable
fun UiClassTest() {
    val name = remember { DomainFullName("John", "Doe") }
    val uiName = remember { name.toComposeFullName() }
    var count by remember { mutableStateOf(1) }

    Column {
        //This shouldn't recompose when count changes since [uiName] isn't changing
        NameFromComposeModule(uiName)
        Text("Click Count: $count")
        Button(onClick = { count++ }) {
            Text("Recompose UI module class test")
        }
    }
}

@Composable
private fun NameFromComposeModule(uiObject: ComposeFullName) {
    Log.e(LOG_TAG, "ComposeFullName recomposed")
    Text(uiObject.first + " " + uiObject.last)
}

@Composable
fun LazyListRecomposition() {
    Text(
        text = "Lazy List Tests",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline,
    )

    Row(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1.0f)) {
            Text("No Derived State", fontWeight = FontWeight.Bold)
            LazyListTest { listState ->
                listState.firstVisibleItemIndex > 0
            }
        }
        Column(modifier = Modifier.weight(1.0f)) {
            Text("Derived State", fontWeight = FontWeight.Bold)
            LazyListTest { listState ->
                remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }.value
            }
        }
    }
}


@Composable
fun LazyListTest(showButtonCallback: @Composable (LazyListState) -> Boolean) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val textItems = List(100) { it.toString() }

    val showButton = showButtonCallback(listState)
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            Log.e(LOG_TAG, "Recomposing entire list")

            items(textItems) {
                Text(it)
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomStart),
            visible = showButton
        ) {
            Button(onClick = { scope.launch { listState.scrollToItem(0) } }) {
                Text("Scroll to top")
            }
        }
    }
}
