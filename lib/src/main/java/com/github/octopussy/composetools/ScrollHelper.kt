package com.github.octopussy.composetools

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusEventModifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.OnGloballyPositionedModifier
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
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

    var parentRect by remember { mutableStateOf(Rect.Zero) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = LocalScrollContainer.current.scrollState

    val density = LocalDensity.current

    fun scrollTo() {
        Log.d(TAG, parentRect.toString())
        coroutineScope.launch {
            scrollState.animateScrollTo((scrollState.value + verticalPos - parentRect.height / 2).toInt())
        }
    }

    fun scrollIfNeeded() {
        val viewportTop = 0//scrollState.value
        val viewportBottom = viewportTop + parentRect.height

        val top = verticalPos// + viewportTop
        val bottom = top + viewSize.height
        val fullyVisible = top > 0 && bottom < parentRect.height

        Log.d(TAG, "$verticalPos ($top-$bottom) ($viewportTop-$viewportBottom)")

        if (!fullyVisible) {
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
        //Log.d(TAG, "1) focused '${focused}'")
        //Log.d(TAG, "2) $parentRect $viewSize $focused")

        if (focused) {
            scrollIfNeeded()
        }
    }

    /*val modifier = remember { RelocationRequesterModifier() }
    DisposableEffect(relocationRequester) {
        relocationRequester.modifiers += modifier
        onDispose { relocationRequester.modifiers -= modifier }
    }
    modifier*/

    return@composed object : FocusEventModifier, OnGloballyPositionedModifier {

        lateinit var coordinates: LayoutCoordinates

        override fun onFocusEvent(focusState: FocusState) {
            focused = focusState.isFocused

        }

        override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
            this.coordinates = coordinates

            val root = coordinates.findRoot()

            val dens = density.density
            parentRect = Rect(
                Offset.Zero, Offset(root.size.width.toFloat(), root.size.height.toFloat()))

            verticalPos = coordinates.positionInWindow().y.toInt()
            viewSize = coordinates.size
           // Log.d(TAG, "onGloballyPositioned '$verticalPos' $viewSize")
          //  Log.d(TAG, "root: $parentRect")
        }

        private fun LayoutCoordinates.findRoot(): LayoutCoordinates {
            var root = this
            var parent = root.parentLayoutCoordinates
            while (parent != null) {
                root = parent
                parent = root.parentLayoutCoordinates
            }

            return root
        }

    }
}