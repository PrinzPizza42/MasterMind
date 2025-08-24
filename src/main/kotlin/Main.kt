import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    val columnSize = remember { mutableStateOf(5) }
    val columnCount = remember { mutableStateOf(8) }
    val columns = remember { mutableStateListOf<SnapshotStateList<Pin>>() }

    // initial filling of list
    LaunchedEffect(Unit) {
        repeat(columnCount.value) {
            columns.add(MutableList(columnSize.value) { Pin(Color.Black) }.toMutableStateList())
        }
    }

    // on change
    LaunchedEffect(columnSize.value) {
        columns.forEach { column ->
            val currentSize = column.size
            val newSize = columnSize.value
            if (newSize > currentSize) {
                repeat(newSize - currentSize) { column.add(Pin(Color.Black)) }
            } else if (newSize < currentSize) {
                repeat(currentSize - newSize) { column.removeLast() }
            }
        }
    }

    LaunchedEffect(columnCount.value) {
        val currentCount = columns.size
        val newCount = columnCount.value
        if (newCount > currentCount) {
            repeat(newCount - currentCount) {
                columns.add(MutableList(columnSize.value) { Pin(Color.Black) }.toMutableStateList())
            }
        } else if (newCount < currentCount) {
            repeat(currentCount - newCount) { columns.removeLast() }
        }
    }

    Column {
        Board.columns(columns, columnSize)

        Board.placeablePins()

        //Changeable Values
        val focusManager = LocalFocusManager.current
        
        //column amount
        var columnAmountValue by remember { mutableStateOf(columnCount.value.toString()) }
        TextField(
            value = columnAmountValue,
            onValueChange = { text ->
                columnAmountValue = text
            },
            modifier = Modifier.onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if(columnAmountValue.toIntOrNull() == null) return@onPreviewKeyEvent true

                    when (event.key) {
                        Key.Enter -> {
                            columnCount.value = columnAmountValue.toInt()

                            focusManager.clearFocus()
                            true
                        }
                        Key.Escape -> {
                            focusManager.clearFocus()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
            label = { Text("Anzahl der Reihen") },
            isError = columnAmountValue.toIntOrNull() == null
        )

        //column size
        var columnSizeValue by remember { mutableStateOf(columnSize.value.toString()) }
        TextField(
            value = columnSizeValue,
            onValueChange = { text ->
                columnSizeValue = text
            },
            modifier = Modifier.onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if(columnSizeValue.toIntOrNull() == null) return@onPreviewKeyEvent true

                    when (event.key) {
                        Key.Enter -> {
                            columnSize.value = columnSizeValue.toInt()
                            println("Set column size: ${columnSize.value}")

                            focusManager.clearFocus()
                            true
                        }
                        Key.Escape -> {
                            focusManager.clearFocus()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
            label = { Text("Größe der Reihen") },
            isError = columnSizeValue.toIntOrNull() == null
        )
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
