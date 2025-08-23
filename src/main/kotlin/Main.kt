import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    val columns = remember { mutableStateListOf(List<Pin>(5) {Pin(Color.Black)}) }

    Column {
        Board.columns(columns)

        Board.placeablePins()

        var value by remember { mutableStateOf(columns.size.toString()) }

        val focusManager = LocalFocusManager.current
        TextField(
            value = value,
            onValueChange = { text ->
                value = text
            },
            modifier = Modifier.onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if(value.toIntOrNull() == null) return@onPreviewKeyEvent true

                    when (event.key) {
                        Key.Enter -> {
                            val newSize = value.toInt()
                            val currentSize = columns.size

                            if (newSize > currentSize) {
                                repeat(newSize - currentSize) {
                                    columns.add(List<Pin>(5) { Pin(Color.Black) })
                                }
                            } else if (newSize in 0..<currentSize) {
                                repeat(currentSize - newSize) {
                                    columns.removeLast()
                                }
                            }
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
            isError = value.toIntOrNull() == null
        )
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
