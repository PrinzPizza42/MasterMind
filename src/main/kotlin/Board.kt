import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Board {
    @Composable
    fun columns(columns: MutableList<List<Pin?>>) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Green)
            ) {
                for (column in columns) {
                    column(column)
                }
                println(columns.size)
            }
    }

    @Composable
    fun column(pins: List<Pin?>) {
        Column(
            modifier = Modifier
                .size(45.dp, (45 * 4).dp)
                .background(Color.White)
        ) {
            for (pin in pins) {
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(35.dp)
                        .background(pin?.color ?: Color.Black, CircleShape)
                        .clip(CircleShape)
                )
            }
        }
    }

    @Composable
    fun placeBalePins() {
        Row(
            Modifier
                .size((45 * 4).dp, 45.dp)
        ) {
            for(color in PinColors.entries) {
                val pin = Pin(color.color)
                Box (Modifier
                    .padding(5.dp)
                    .size(35.dp)
                    .background(pin.color, CircleShape)
                    .clip(CircleShape)) {

                }
            }
        }
    }
}