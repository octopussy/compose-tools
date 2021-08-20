package com.github.octopussy.composetools

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import com.github.octopussy.composetools.ui.theme.ComposeToolsTheme
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val scrollEventFlow = MutableSharedFlow<ScrollToEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ComposeToolsTheme {
                // ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                val fm = LocalFocusManager.current
                // A surface container using the 'background' color from the theme
              //  Surface(color = MaterialTheme.colors.background) {
                    //CustomModifierTest()

                    ValidationTest(scrollEventFlow)

                    //Greeting("Android")
             //   }
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

    Box {
        ProvideScrollHelper(state) {
            Column(
                Modifier
                    .verticalScroll(state)
                    .padding(32.dp)
                    //  .navigationBarsWithImePadding()
                    //    .statusBarsPadding()

            ) {


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