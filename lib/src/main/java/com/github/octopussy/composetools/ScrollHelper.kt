package com.github.octopussy.composetools

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusEventModifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.OnGloballyPositionedModifier
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

private const val TAG = "ScrollHelper"

@Stable
class ScrollContainer(val scrollState: ScrollState, val parentRect: Rect)

class ScrollRequester {

    internal var bringIntoViewDelegate: () -> Unit = {}

    fun bringIntoView() {
        bringIntoViewDelegate()
    }
}

@Composable
fun rememberScrollRequester(): ScrollRequester {
    return remember { ScrollRequester() }
}

val LocalScrollContainer =
    compositionLocalOf<ScrollContainer> { error("Scroll container is not provided!") }

@Composable
fun ProvideScrollHelper(
    scrollState: ScrollState,
    parentRect: Rect,
    content: @Composable () -> Unit
) {

    val view = LocalView.current
    //  val windowInsets = remember { RootWindowInsets() }

    DisposableEffect(view) {
        Log.d(TAG, "Init $view")
        // val observer = ViewWindowInsetObserver(view)
        /*   observer.observeInto(
               windowInsets = windowInsets,
               consumeWindowInsets = consumeWindowInsets,
               windowInsetsAnimationsEnabled = windowInsetsAnimationsEnabled
           )*/
        onDispose {
            //observer.stop()
            Log.d(TAG, "Dispose $view")
        }
    }

    CompositionLocalProvider(
        LocalScrollContainer provides ScrollContainer(
            scrollState,
            parentRect
        )
    ) {
        content()
    }
}

fun Modifier.scrollHelper(
    autoScrollIfFocused: Boolean = true,
    requester: ScrollRequester = ScrollRequester()
): Modifier = composed {
    var lastRequester: ScrollRequester

    var focused by remember { mutableStateOf(false) }
    var verticalPos by remember { mutableStateOf(0) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val coroutineScope = rememberCoroutineScope()

    val scrollState = LocalScrollContainer.current.scrollState
    val parentRect = LocalScrollContainer.current.parentRect

    Log.d(TAG, " ---- " + parentRect.toString())

    fun scrollTo() {
        Log.d(TAG, parentRect.toString())
        coroutineScope.launch {
            scrollState.animateScrollTo((scrollState.value + verticalPos - parentRect.height / 2).toInt())
        }
    }

    fun scrollIfNeeded() {
        val viewportTop = scrollState.value
        val viewportBottom = scrollState.value + parentRect.height

        val top = verticalPos + viewportTop
        val bottom = top + size.height
        val fullyVisible = top > viewportTop && bottom < viewportBottom

        if (focused && !fullyVisible) {
            scrollTo()
        }
    }

    DisposableEffect(requester) {
        requester.bringIntoViewDelegate = {
            scrollTo()
        }
        lastRequester = requester
        onDispose {
            lastRequester.bringIntoViewDelegate = {}
        }
    }


    LaunchedEffect(focused, parentRect) {
        // Log.d(TAG, "testModifier focused '${focused}'")
        //   Log.d(TAG, "$parentRect $size $focused ${parentRect.bottom}")

        scrollIfNeeded()
    }

    /*val modifier = remember { RelocationRequesterModifier() }
    DisposableEffect(relocationRequester) {
        relocationRequester.modifiers += modifier
        onDispose { relocationRequester.modifiers -= modifier }
    }
    modifier*/

    return@composed object : FocusEventModifier, OnGloballyPositionedModifier {
        override fun onFocusEvent(focusState: FocusState) {
            focused = focusState.isFocused
        }

        override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
            verticalPos = coordinates.positionInRoot().y.toInt()
            size = coordinates.size
            //Log.d(TAG, "onGloballyPositioned '$verticalPos' $size")
        }
    }
}