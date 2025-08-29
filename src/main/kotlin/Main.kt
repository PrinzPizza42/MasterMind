import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

@Composable
@Preview
fun App() {
    val columnSize = remember { mutableStateOf(4) }
    val columnCount = remember { mutableStateOf(8) }
    val columns = remember { mutableStateListOf<SnapshotStateList<Pin>>() }
    val gamePhase = remember { mutableStateOf(GamePhases.BEFORE_GAME) }
    val solution = remember { mutableListOf<Pin>() }
    val colorAmount = remember { mutableStateOf(4) }

    val gameResults = remember { mutableListOf<GameResult>() }

    //End of game statistics
    val won = remember { mutableStateOf(false) }
    val neededTries = remember { mutableStateOf(0) }

//    LaunchedEffect(Unit) {
//        repeat(10) {
//            gameResults.addLast(GameResult(Random.nextBoolean(), Random.nextInt(10), Random.nextInt(10), Random.nextInt(10)))
//        }
//    }

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
        when (gamePhase.value) {
            GamePhases.BEFORE_GAME -> beforeGame(gamePhase, columnSize, columnCount, colorAmount, gameResults)
            GamePhases.SET_INITIAL_PINS -> setInitialPins(gamePhase, solution, columnSize, colorAmount)
            GamePhases.PLAYING -> playing(columns, columnSize, columnCount, gamePhase, solution, won, neededTries, colorAmount)
            GamePhases.FINISHED -> finished(gamePhase, won, neededTries, columns, solution, columnCount, columnSize, gameResults)
        }
    }
}

@Composable
fun beforeGame(
    gamePhase: MutableState<GamePhases>,
    columnSize: MutableState<Int>,
    columnCount: MutableState<Int>,
    colorAmount: MutableState<Int>,
    gameResults: MutableList<GameResult>
) {
    Row {
        Column {
            Text("Voreinstellungen")
            Button(
                onClick = {
                    gamePhase.value = GamePhases.SET_INITIAL_PINS
                },
                content = { Text("Start") }
            )
            settings(columnCount, columnSize, colorAmount)
        }

        Column(
            Modifier
                .background(Color.LightGray)
        ) {
            val shape = remember { RoundedCornerShape(5.dp) }
            Box(
                Modifier
                    .padding(0.dp, 0.dp, 0.dp, 15.dp)
                    .shadow(5.dp, shape)
                    .background(Color.LightGray, shape)
                    .clip(shape)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Spiele in dieser Session:", Modifier.padding(5.dp))
            }

            Column(
                Modifier.verticalScroll(rememberScrollState())
            ) {
                if(gameResults.size > 0) {
                    for(result in gameResults) {
                        val wonString = if(result.won) "gewonnen" else "verloren"
                        Column(
                            Modifier
                                .shadow(5.dp, shape)
                                .padding(0.dp, 7.dp)
                                .width(200.dp)
                                .background(Color.White, shape)
                                .clip(shape)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text("Spiel ${gameResults.indexOf(result) + 1}", Modifier.padding(5.dp))
                            // name of both players
                            Text("Spieler hat nach ${result.tries} Versuchen $wonString")
                        }
                    }
                }
                else Text("Noch keine Spiele gespielt")
            }
        }
    }
}

@Composable
fun setInitialPins(
    gamePhase: MutableState<GamePhases>,
    solution: MutableList<Pin>,
    columnSize: MutableState<Int>,
    colorAmount: MutableState<Int>
) {
    Text("Lösung setzen")

    solution.clear()
    repeat(columnSize.value) {
        solution.add(Pin(Color.Black))
    }

    Board.column(solution, columnSize, mutableStateOf(false), colorAmount)

    Button(
        onClick = {
            if(!solution.none { it.color == Color.Black }) return@Button

            solution.forEach { pin ->
                println(pin.color.toArgb())
            }

            gamePhase.value = GamePhases.PLAYING
        },
        content = { Text(if(solution.none { it.color == Color.Black }) "Fertig" else "Nicht alle gesetzt") }
    )
}

@Composable
fun playing(
    columns: SnapshotStateList<SnapshotStateList<Pin>>,
    columnSize: MutableState<Int>,
    columnCount: MutableState<Int>,
    gamePhase: MutableState<GamePhases>,
    solution: MutableList<Pin>,
    won: MutableState<Boolean>,
    neededTries: MutableState<Int>,
    colorAmount: MutableState<Int>,
) {
    Text("Lösen")

    val currentColumn = remember { mutableStateOf(0) }

    Board.columns(columns, columnSize, currentColumn, gamePhase, solution, won, neededTries, colorAmount)
}

@Composable
fun finished(
    gamePhase: MutableState<GamePhases>,
    won: MutableState<Boolean>,
    neededTries: MutableState<Int>,
    columns: SnapshotStateList<SnapshotStateList<Pin>>,
    solution: MutableList<Pin>,
    columnCount: MutableState<Int>,
    columnSize: MutableState<Int>,
    gameResults: MutableList<GameResult>
) {
    Text("Ende")

    if(won.value) Text("Du hast das Muster gefunden")
    else Text("Du hast das Muster nicht rechtzeitig gefunden")

    Text("Gebrauchte Versuche: ${neededTries.value}")

    Button(
        onClick = {
            gamePhase.value = GamePhases.BEFORE_GAME
        },
        content = { Text("Neustarten") }
    )

    val result = GameResult(won.value, neededTries.value, columnSize.value, columnCount.value)
    println("Result: $result")
    gameResults.addLast(result)

    resetValues(columns, solution, columnCount, columnSize, neededTries)
}

@Composable
private fun resetValues(
    columns: SnapshotStateList<SnapshotStateList<Pin>>,
    solution: MutableList<Pin>,
    columnCount: MutableState<Int>,
    columnSize: MutableState<Int>,
    neededTries: MutableState<Int>
) {
    columns.clear()
    repeat(columnCount.value) {
        columns.add(MutableList(columnSize.value) { Pin(Color.Black) }.toMutableStateList())
    }
}

@Composable
fun settings(
    columnCount: MutableState<Int>,
    columnSize: MutableState<Int>,
    colorAmount: MutableState<Int>
) {
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

    //color amount
    var colorAmountValue by remember { mutableStateOf(colorAmount.value.toString()) }
    TextField(
        value = colorAmountValue,
        onValueChange = { text ->
            colorAmountValue = text
        },
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                if(colorAmountValue.toIntOrNull() == null) return@onPreviewKeyEvent true

                when (event.key) {
                    Key.Enter -> {
                        colorAmount.value = colorAmountValue.toInt()
                        println("Set color amount: ${colorAmount.value}")

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
        label = { Text("Menge der auswählbaren Farben (maximal ${PinColors.entries.size})") },
        isError = colorAmountValue.toIntOrNull() == null
    )
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}