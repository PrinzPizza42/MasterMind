import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

object Board {
    @Composable
    fun columns(columns: SnapshotStateList<SnapshotStateList<Pin>>, columnSize: MutableState<Int>) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Green)
            ) {
                for (column in columns) {
                    column(column, columnSize)
                }
                println(columns.size)
            }
    }

    @Composable
    fun column(pins: List<Pin>, columnSize: MutableState<Int>) {
        Column(
            modifier = Modifier
                .size(45.dp, (45 * columnSize.value).dp)
                .background(Color.White)
        ) {
            for (pin in pins) {
                var showPopup by remember { mutableStateOf(false) }
                var boxPosition by remember { mutableStateOf(Offset.Zero) }

                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(35.dp)
                        .background(pin.color, CircleShape)
                        .clip(CircleShape)
                        .clickable{ showPopup = true }
                )

                if (showPopup) {
                    Popup(
                        offset = IntOffset(boxPosition.x.toInt() + 100, boxPosition.y.toInt()),
                        onDismissRequest = { showPopup = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.White, shape = RoundedCornerShape(8.dp))
                                .padding(16.dp)
                        ) {
                            Text("Wähle eine Farbe:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                for(color in PinColors.entries) {
                                    val colorPickerPin = Pin(color.color)
                                    Box (Modifier
                                        .padding(5.dp)
                                        .size(35.dp)
                                        .background(colorPickerPin.initialColor, CircleShape)
                                        .clip(CircleShape)
                                        .clickable {
                                            pin.color = color.color
                                            showPopup = false
                                            println("Selected color ${color.color}")
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .background(Color.Gray, RoundedCornerShape(15.dp))
                                        .size(3.dp, 35.dp)
                                )

                                val colorPickerPin = Pin(Color.Black)
                                Box (Modifier
                                    .padding(5.dp)
                                    .size(35.dp)
                                    .background(colorPickerPin.initialColor, CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        pin.color = Color.Black
                                        showPopup = false
                                        println("Reset pin color")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun placeablePins() {
        Row(
            Modifier
                .size((45 * 4).dp, 45.dp)
        ) {
            for(color in PinColors.entries) {
                val pin = Pin(color.color)
                Box (Modifier
                    .padding(5.dp)
                    .size(35.dp)
                    .background(pin.initialColor, CircleShape)
                    .clip(CircleShape)) {

                }
            }
        }
    }
}