import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
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
import androidx.compose.ui.zIndex

object Board {
    @Composable
    fun columns(
        columns: SnapshotStateList<SnapshotStateList<Pin>>,
        columnSize: MutableState<Int>,
        currentColumn: MutableState<Int>,
        gamePhase: MutableState<GamePhases>,
        solution: MutableList<Pin>
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            for (column in columns) {
                val ready = remember { mutableStateOf(false) }

                //Evalution
                val perfectPins = remember { mutableStateOf(0) } //right position and color
                val rightColorPin = remember { mutableStateOf(0) } // only right color

                Box {
                    if(currentColumn.value != columns.indexOf(column)) {
                        Box(
                            Modifier
                                .background(Color.LightGray.copy(alpha = 0.5f))
                                .clickable(enabled = false){}
                                .zIndex(2f)
                                .size(45.dp, (45 * columnSize.value).dp)
                        )
                    }

                    Column {
                        column(column, columnSize, ready)

                        if(currentColumn.value == columns.indexOf(column)) {
                            Button(
                                modifier = Modifier
                                    .size(45.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                onClick = {
                                    if(currentColumn.value < columns.size - 1) {
                                        val evaluation = evaluate(column, solution)
                                        perfectPins.value = evaluation.first
                                        rightColorPin.value = evaluation.second
                                        println("Evaluation: perfect pins: ${evaluation.first}, right color pins: ${evaluation.second}")

                                        currentColumn.value++
                                        println("Column ${currentColumn.value}")
                                    }
                                    else {
                                        gamePhase.value = GamePhases.FINISHED
                                        println("game finished")
                                    }
                                },
                                content = {
                                    Text("✓")
                                },
                                enabled = column.none {it.color == Color.Black}
                            )
                        }
                    }
                }
            }
        }
    }

    private fun evaluate(column: List<Pin>, solution: MutableList<Pin>): Pair<Int, Int> {
        println("Evaluating $column")

        var perfectPins = 0
        var rightColorPins = 0

        column.forEach { pin ->
            val index = column.indexOf(pin)

            val sameColor: Pin? = solution.firstOrNull {
                it.color == pin.color
            }

            if(sameColor != null) {
                if(solution.indexOf(sameColor) == index) {
                    perfectPins++
                    println("Found perfect pin at solution index ${solution.indexOf(sameColor)} color ${sameColor.color}")
                }
                else {
                    rightColorPins++
                    println("Found right color pin")
                }
            }
        }

        return Pair(perfectPins, rightColorPins)
    }

    @Composable
    fun column(pins: List<Pin>, columnSize: MutableState<Int>, ready: MutableState<Boolean>) {
        Column(
            modifier = Modifier
                .size(45.dp, (45 * columnSize.value).dp)
                .background(Color.White)
        ) {
            for (pin in pins) {
                var showPopup by remember { mutableStateOf(false) }
                val boxPosition by remember { mutableStateOf(Offset.Zero) }

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
    fun evaluationPinsColumn() {

    }
}