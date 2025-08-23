import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class Pin(
    val initialColor: Color
) {
    var color: Color by mutableStateOf(initialColor)
}
