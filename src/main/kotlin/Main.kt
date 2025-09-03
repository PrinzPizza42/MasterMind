import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.zIndex

@Composable
@Preview
fun App() {
    // Settings
    val columnSize = remember { mutableStateOf(4) }
    val columnCount = remember { mutableStateOf(8) }
    val columns = remember { mutableStateListOf<SnapshotStateList<Pin>>() }
    val gamePhase = remember { mutableStateOf(GamePhases.BEFORE_GAME) }
    val solution = remember { mutableListOf<Pin>() }
    val colorAmount = remember { mutableStateOf(8) }
    val generateInitialPins = remember { mutableStateOf(true) }
    val duplicateColors = remember { mutableStateOf(false) }

    val gameResults = remember { mutableStateOf(mutableListOf<GameResult>()) }

    // End of game statistics
    val won = remember { mutableStateOf(false) }
    val neededTries = remember { mutableStateOf(0) }

//    LaunchedEffect(Unit) {
//        val newGameResults = mutableListOf<GameResult>()
//        repeat(10) {
//            newGameResults.addLast(GameResult(Random.nextBoolean(), Random.nextInt(10), Random.nextInt(10), Random.nextInt(10), Random.nextInt(8)))
//        }
//        gameResults.value = newGameResults
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
            GamePhases.BEFORE_GAME -> beforeGame(gamePhase, columnSize, columnCount, colorAmount, gameResults, generateInitialPins, duplicateColors)
            GamePhases.SET_INITIAL_PINS -> setInitialPins(gamePhase, solution, columnSize, colorAmount, generateInitialPins, duplicateColors)
            GamePhases.PLAYING -> playing(columns, columnSize, columnCount, gamePhase, solution, won, neededTries, colorAmount)
            GamePhases.FINISHED -> finished(gamePhase, won, neededTries, columns, solution, columnCount, columnSize, gameResults, colorAmount, generateInitialPins, duplicateColors)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun beforeGame(
    gamePhase: MutableState<GamePhases>,
    columnSize: MutableState<Int>,
    columnCount: MutableState<Int>,
    colorAmount: MutableState<Int>,
    gameResults: MutableState<MutableList<GameResult>>,
    generateInitialPins: MutableState<Boolean>,
    duplicateColors: MutableState<Boolean>
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
            settings(columnCount, columnSize, colorAmount, generateInitialPins, duplicateColors)
        }

        // Game Results
        Column(
            Modifier
                .background(Color.LightGray)
        ) {
            val shape = remember { RoundedCornerShape(5.dp) }
            Row(Modifier.align(Alignment.CenterHorizontally)) {
                Box(
                    Modifier
                        .padding(0.dp, 0.dp, 0.dp, 15.dp)
                        .shadow(5.dp, shape)
                        .background(Color.LightGray, shape)
                        .clip(shape)
                ) {
                    Text("Spiele in dieser Session (${gameResults.value.size}):", Modifier.padding(5.dp))
                }
                Button(
                    modifier = Modifier,
                    onClick = {
                        gameResults.value = mutableListOf()
                        println("Reset game results: $gameResults")
                    },
                    content = {
                        Text("Reset")
                    },
                    enabled = gameResults.value.isNotEmpty()
                )
            }

            val cursorPosition = remember { mutableStateOf(Offset.Zero) }

            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .onPointerEvent(PointerEventType.Move) {
                        val position = it.changes.first().position
                        cursorPosition.value = position
                    }
            ) {
                if(gameResults.value.isNotEmpty()) {
                    for(result in gameResults.value) {
                        var showPopup by remember { mutableStateOf(false) }
                        val wonString = if(result.won) "gewonnen" else "verloren"
                        val index = gameResults.value.indexOf(result)

                        Column(
                            Modifier
                                .shadow(5.dp, shape)
                                .padding(0.dp, 7.dp)
                                .width(200.dp)
                                .background(Color.White, shape)
                                .clip(shape)
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    showPopup = true
                                }
                        ) {
                            Text("Spiel ${index + 1}", Modifier.padding(5.dp))
                            // name of both players
                            Text("Spieler hat nach ${result.tries} Versuchen $wonString")
                        }

                        if(showPopup) {
                            Popup(
                                offset = IntOffset.Zero.copy(x = 210, y = cursorPosition.value.y.toInt()),
                                onDismissRequest = { showPopup = false }
                            ) {
                                Column(
                                    Modifier
                                        .background(Color.White, RoundedCornerShape(5.dp))
                                ) {
                                    Text("Spiel: ${gameResults.value.indexOf(result) + 1}")
//                                    Text("Spieler: ${result.player}")
                                    Text("Versuche: ${result.tries}")
                                    Text("Ergebnis: $wonString")
                                    Text("mit Einstellungen", Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp))
                                    Text("  Reihengröße: ${result.columnSize}")
                                    Text("  Reihenmenge: ${result.columnCount}")
                                    Text("  Farbenmenge: ${result.colorAmount}")
                                    Text("  Lösung generiert: ${result.generateInitialPins}")
                                    Text("  Farben mehrfach verwendbar: ${result.duplicateColors}")
                                }
                            }
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
    colorAmount: MutableState<Int>,
    generateInitialPins: MutableState<Boolean>,
    duplicateColors: MutableState<Boolean>
) {
    Text("Lösung setzen")

    solution.clear()

    if(generateInitialPins.value) {
        generateInitialPins(columnSize, colorAmount, solution, duplicateColors)

        gamePhase.value = GamePhases.PLAYING
    }
    else {
        repeat(columnSize.value) {
            solution.add(Pin(Color.Black))
        }

        Board.row(solution, columnSize, colorAmount)

        Button(
            onClick = {
                if(!solution.none { it.color == Color.Black }) return@Button

                gamePhase.value = GamePhases.PLAYING
            },
            content = { Text(if(solution.none { it.color == Color.Black }) "Fertig" else "Nicht alle gesetzt") }
        )
    }
}

@Composable
fun generateInitialPins(
    columnSize: MutableState<Int>,
    colorAmount: MutableState<Int>,
    solution: MutableList<Pin>,
    duplicateColors: MutableState<Boolean>
) {
    println("Generating initial pins")
    val pinColorsShortened = PinColors.entries.subList(0, colorAmount.value).toMutableList()
    repeat(columnSize.value) {
        val color = pinColorsShortened.random()

        if(!duplicateColors.value) {
            if(colorAmount.value >= columnSize.value) {
                pinColorsShortened.remove(color)
            }
            else println("Need to use colors several times because !colorAmount.value >= columnSize.value")
        }
        else println("did not remove because duplicateColors = ${duplicateColors.value}")

        solution.addLast(Pin(color.color))
        println("added $color to solution")
    }
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

    Board.rows(columns, columnSize, currentColumn, gamePhase, solution, won, neededTries, colorAmount)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun finished(
    gamePhase: MutableState<GamePhases>,
    won: MutableState<Boolean>,
    neededTries: MutableState<Int>,
    columns: SnapshotStateList<SnapshotStateList<Pin>>,
    solution: MutableList<Pin>,
    columnCount: MutableState<Int>,
    columnSize: MutableState<Int>,
    gameResults: MutableState<MutableList<GameResult>>,
    colorAmount: MutableState<Int>,
    generateInitialPins: MutableState<Boolean>,
    duplicateColors: MutableState<Boolean>
) {
    Row {
        Column(
            Modifier.weight(1f)
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
        }

        Column(
            Modifier
                .weight(1f)
        ) {
            Text("Lösung:")

            Box(
                Modifier
                    .clickable(enabled = false) {}
                    .align(Alignment.CenterHorizontally)
            ) {
                Board.immutableRow(solution, columnSize)
            }
        }
    }

    val result = GameResult(won.value, neededTries.value, columnSize.value, columnCount.value, colorAmount.value, generateInitialPins.value, duplicateColors.value)
    println("Result: $result")
    gameResults.value.addLast(result)

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
    colorAmount: MutableState<Int>,
    generateInitialPins: MutableState<Boolean>,
    duplicateColors: MutableState<Boolean>
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

    // generate initial pins
    Row {
        Checkbox(
            checked = generateInitialPins.value,
            onCheckedChange = {
                generateInitialPins.value = it
                println("Changed generateInitialPins value to ${generateInitialPins.value}")
            }
        )
        Text("Lösung generieren")
    }

    // duplicate colors
    Row {
        Checkbox(
            checked = duplicateColors.value,
            onCheckedChange = {
                duplicateColors.value = it
                println("Changed duplicateColors value to ${duplicateColors.value}")
            }
        )
        Text("Farben mehrfach verwenden")
    }

}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}