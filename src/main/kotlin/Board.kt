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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
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

                //Evaluation
                val perfectPins = remember { mutableStateOf(0) } //right position and color
                val rightColorPin = remember { mutableStateOf(0) } // only right color

                Box {
                    if(currentColumn.value != columns.indexOf(column)) {
                        Box(
                            Modifier
                                .background(Color.LightGray.copy(alpha = 0.5f))
                                .clickable(enabled = false){}
                                .zIndex(2f)
                                .size(45.dp, ((45 * columnSize.value) + (30 * columnSize.value)).dp)
                                .width(45.dp)
                        )
                    }

                    Column {
                        evaluationColumn(perfectPins, rightColorPin, columnSize)

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

                                        if(evaluation.first == columnSize.value) {
                                            gamePhase.value = GamePhases.FINISHED
                                            println("game won")
                                        }

                                        currentColumn.value++
                                        println("Column ${currentColumn.value}")
                                    }
                                    else {
                                        gamePhase.value = GamePhases.FINISHED
                                        println("game lost")
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

        val solutionCopy: MutableList<Pin?> = solution.toMutableList()

        //Perfect Pins
        column.forEachIndexed { i, pin ->
            if (solutionCopy[i]?.color == pin.color) {
                perfectPins++
                solutionCopy[i] = null
            }
        }

        //Right color Pins
        column.forEachIndexed { i, pin ->
            if (solution[i].color != pin.color) {
                val j = solutionCopy.indexOfFirst { it?.color == pin.color }
                if (j >= 0) {
                    rightColorPins++
                    solutionCopy[j] = null
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
    fun evaluationColumn(
        perfectPins: MutableState<Int>,
        rightColorPin: MutableState<Int>,
        columnSize: MutableState<Int>
    ) {
        val colors = remember { mutableStateListOf<Color>() }

        colors.clear()
        repeat(perfectPins.value) {
            colors.add(Color.Red)
        }
        repeat(rightColorPin.value) {
            colors.add(Color.Gray)
        }
        if(perfectPins.value + rightColorPin.value <= columnSize.value) {
            repeat(columnSize.value- (perfectPins.value + rightColorPin.value)) {
                colors.add(Color.Black)
            }
        }

        Column(
            modifier = Modifier
                .size(45.dp, (30 * columnSize.value).dp)
                .background(Color.White)
        ) {
            for (color in colors) {
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .size(20.dp)
                        .background(color, CircleShape)
                        .clip(CircleShape)
                )
            }
        }
    }
}