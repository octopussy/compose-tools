package com.github.octopussy.composetools

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.github.octopussy.composetools.ui.theme.ComposeToolsTheme
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val scrollEventFlow = MutableSharedFlow<ScrollToEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ComposeToolsTheme {
                // ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                val fm = LocalFocusManager.current
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    //CustomModifierTest()
                    Column {
                        Button(onClick = {
                            lifecycleScope.launch {
                                scrollEventFlow.emit(ScrollToEvent.First)
                            }
                        }) {
                            Text(text = "First")
                        }
                        Button(onClick = {
                            lifecycleScope.launch {
                                scrollEventFlow.emit(ScrollToEvent.Second)
                            }
                        }) {
                            Text(text = "Second")
                        }

                        ValidationTest(scrollEventFlow)
                    }

                    //Greeting("Android")
                }
                //   }
            }
        }
    }
}

enum class ScrollToEvent {
    First, Second
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Input(scrollRequester: ScrollRequester = ScrollRequester()) {
    var value by remember { mutableStateOf("") }

    TextField(
        value = value,
        singleLine = true,
        onValueChange = { value = it },
        modifier = Modifier
            .scrollHelper(
                requester = scrollRequester
            )
    )
}


@OptIn(ExperimentalAnimatedInsets::class)
@Composable
fun ValidationTest(scrollToEvent: Flow<ScrollToEvent>) {

    //var container

    val firstRequester = rememberScrollRequester()
    val secondRequester = rememberScrollRequester()

    val scope = rememberCoroutineScope()

    LaunchedEffect(scrollToEvent) {
        scope.launch {
            scrollToEvent.collect { event ->
                Log.d("MainActivity", "Collected: $event")
                when (event) {
                    ScrollToEvent.First -> firstRequester.bringIntoView()
                    ScrollToEvent.Second -> secondRequester.bringIntoView()
                }
            }
        }
    }

    val state = rememberScrollState()
    var myRect by remember { mutableStateOf(Rect.Zero) }



    ProvideScrollHelper(state, myRect) {
        Column(
            Modifier
                .verticalScroll(state)
                .navigationBarsWithImePadding()
                .statusBarsPadding()
                .onGloballyPositioned {
                    myRect = it.boundsInWindow()
                    //it.size
                }
        ) {
            Spacer(modifier = Modifier.size(200.dp))
            Text("-Outer", color = Color.Black)
            Spacer(modifier = Modifier.size(200.dp))
            Column {

                Text("-----------Inner", color = Color.Black)

                Input(scrollRequester = firstRequester)

                Spacer(modifier = Modifier.size(200.dp))

                Input()

                Spacer(modifier = Modifier.size(200.dp))

                Input()

                Spacer(modifier = Modifier.size(200.dp))

                Input()

                Spacer(modifier = Modifier.size(200.dp))

                Input(scrollRequester = secondRequester)

                Spacer(modifier = Modifier.size(200.dp))

                Button(onClick = { firstRequester.bringIntoView() }) {

                }

            }
        }
    }
}


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeToolsTheme {
        Greeting("Android")
    }
}