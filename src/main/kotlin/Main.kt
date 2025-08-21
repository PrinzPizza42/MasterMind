import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    val columns = remember { mutableStateListOf(List<Pin?>(4) {null}) }

    Column {
        Board.columns(columns)

        Board.placeBalePins()

        TextField(
            value = columns.size.toString(),
            onValueChange = { text ->
                val newSize = text.toIntOrNull() ?: return@TextField
                val currentSize = columns.size

                if (newSize > currentSize) {
                    repeat(newSize - currentSize) {
                        columns.add(List<Pin?>(4) { null })
                    }
                } else if (newSize < currentSize && newSize >= 0) {
                    repeat(currentSize - newSize) {
                        columns.removeLast()
                    }
                }
            },
            label = { Text("Anzahl der Reihen") }
        )
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
