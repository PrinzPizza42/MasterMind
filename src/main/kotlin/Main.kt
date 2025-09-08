import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.round
import kotlin.uuid.ExperimentalUuidApi

@Composable
@Preview
fun App(windowTitle: MutableState<String>) {
    // Settings
    val columnSize = remember { mutableStateOf(4) }
    val columnCount = remember { mutableStateOf(8) }
    val columns = remember { mutableStateListOf<SnapshotStateList<Pin>>() }
    val gamePhase = remember { mutableStateOf(GamePhases.BEFORE_GAME) }
    val solution = remember { mutableListOf<Pin>() }
    val colorAmount = remember { mutableStateOf(8) }
    val generateInitialPins = remember { mutableStateOf(true) }
    val duplicateColors = remember { mutableStateOf(false) }
    val pinSize = remember { mutableStateOf(1f) }

    val gameResults = remember { mutableStateOf(mutableListOf<GameResult>()) }

    // End of game statistics
    val won = remember { mutableStateOf(false) }
    val neededTries = remember { mutableStateOf(0) }

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

    AnimatedContent(
        targetState = gamePhase.value,
        transitionSpec = {
            // Compare the incoming number with the previous number.
            if (targetState.ordinal > initialState.ordinal) {
                // If the target number is larger, it slides up and fades in
                // while the initial (smaller) number slides up and fades out.
                slideInHorizontally { height -> height } + fadeIn() togetherWith
                        slideOutHorizontally { height -> -height } + fadeOut()
            } else {
                // If the target number is smaller, it slides down and fades in
                // while the initial number slides down and fades out.
                slideInHorizontally { height -> -height } + fadeIn() togetherWith
                        slideOutHorizontally { height -> height } + fadeOut()
            }.using(
                // Disable clipping since the faded slide-in/out should
                // be displayed out of bounds.
                SizeTransform(clip = false)
            )
        },
        label = "animated content",
        modifier = Modifier.background(DefaultColors.BACKGROUND.color),
        ) { targetCount ->
        Column(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (targetCount) {
                GamePhases.BEFORE_GAME -> {
                    beforeGame(gamePhase, columnSize, columnCount, colorAmount, gameResults, generateInitialPins, duplicateColors, pinSize)
                    windowTitle.value = "Vor dem Spiel"
                }
                GamePhases.SET_INITIAL_PINS -> {
                    setInitialPins(gamePhase, solution, columnSize, colorAmount, generateInitialPins, duplicateColors, pinSize)
                    windowTitle.value = "Lösung setzen"
                }
                GamePhases.PLAYING -> {
                    playing(columns, columnSize, columnCount, gamePhase, solution, won, neededTries, colorAmount, pinSize)
                    windowTitle.value = "Spielend"
                }
                GamePhases.FINISHED -> {
                    finished(gamePhase, won, neededTries, columns, solution, columnCount, columnSize, gameResults, colorAmount, generateInitialPins, duplicateColors, pinSize)
                    windowTitle.value = if(won.value) "Gewonnen" else "Verloren"
                }
            }
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
    duplicateColors: MutableState<Boolean>,
    pinSize: MutableState<Float>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(
                    Modifier
                        .padding(10.dp)
                        .shadow(5.dp, RoundedCornerShape(5.dp))
                        .background(DefaultColors.PRIMARY.color, RoundedCornerShape(5.dp)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Spieleinstellungen",
                        color = DefaultColors.TEXT_ON_SECONDARY.color,
                        modifier = Modifier
                            .shadow(5.dp)
                            .background(DefaultColors.SECONDARY.color, RoundedCornerShape(5.dp))
                            .padding(5.dp),
                    )

                    settings(columnCount, columnSize, colorAmount, generateInitialPins, duplicateColors, pinSize)
                }

                Button(
                    onClick = {
                        gamePhase.value = GamePhases.SET_INITIAL_PINS
                    },
                    content = { Text("Start") },
                    modifier = Modifier
                        .padding(40.dp)
                        .size(200.dp, 100.dp)
                        .pointerHoverIcon(PointerIcon.Hand),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DefaultColors.HIGHLIGHT.color,
                        contentColor = DefaultColors.TEXT_ON_HIGHLIGHT.color
                    )
                )
            }

            // Game Results
            Column(
                Modifier
                    .padding(10.dp)
                    .shadow(5.dp, RoundedCornerShape(5.dp))
                    .background(DefaultColors.PRIMARY.color, RoundedCornerShape(5.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val shape = remember { RoundedCornerShape(5.dp) }
                Row(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .shadow(5.dp, shape)
                        .background(DefaultColors.SECONDARY.color, shape)
                        .height(40.dp)
                        .padding(5.dp)
                ) {
                    Box(
                        Modifier
                            .clip(shape)
                    ) {
                        Text(text = "Spiele in dieser Session (${gameResults.value.size}):",
                            modifier = Modifier.padding(5.dp),
                            color = DefaultColors.TEXT_ON_SECONDARY.color
                        )
                    }
                    Button(
                        modifier = Modifier
                            .height(30.dp)
                            .pointerHoverIcon(if(gameResults.value.isNotEmpty()) PointerIcon.Hand else PointerIcon.Default),
                        onClick = {
                            gameResults.value = mutableListOf()
                            println("Reset game results: $gameResults")
                        },
                        content = {
                            Text("Reset")
                        },
                        enabled = gameResults.value.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = DefaultColors.HIGHLIGHT.color,
                            contentColor = DefaultColors.TEXT_ON_HIGHLIGHT.color,
                            disabledBackgroundColor = DefaultColors.PRIMARY.color,
                            disabledContentColor = DefaultColors.TEXT_ON_PRIMARY.color
                        )
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
                        var index = remember { 0 }
                        for(result in gameResults.value) {
                            var showPopup by remember { mutableStateOf(false) }
                            val wonString = if(result.won) "gewonnen" else "verloren"

                            index++

                            Column(
                                Modifier
                                    .padding(0.dp, 7.dp)
                                    .shadow(5.dp, shape)
                                    .width(230.dp)
                                    .background(DefaultColors.SECONDARY.color, shape)
                                    .clip(shape)
                                    .align(Alignment.CenterHorizontally)
                                    .onPointerEvent(PointerEventType.Enter) {
                                        showPopup = true
                                    }
                                    .onPointerEvent(PointerEventType.Exit) {
                                        showPopup = false
                                    }
                                    .padding(5.dp)
                                    .pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Text("Spiel $index", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                // name of both players
                                Text("Spieler hat nach ${result.tries} ${if(result.tries >= 2) "Versuchen" else "Versuch"} $wonString",
                                    color = DefaultColors.TEXT_ON_SECONDARY.color
                                )
                            }

                            if(showPopup) {
                                Popup(
                                    offset = IntOffset.Zero.copy(x = 210, y = cursorPosition.value.y.toInt() + 10),
                                    onDismissRequest = { showPopup = false }
                                ) {
                                    Column(
                                        Modifier
                                            .background(DefaultColors.SECONDARY.color, RoundedCornerShape(5.dp))
                                            .padding(5.dp)
                                    ) {
                                        Text("Spiel: ${gameResults.value.indexOf(result) + 1}", color = DefaultColors.TEXT_ON_SECONDARY.color)
//                                    Text("Spieler: ${result.player}", color = DefaultColors.TEXT.color)
                                        Text("Versuche: ${result.tries}", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                        Text("Ergebnis: $wonString", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                        Text("mit Einstellungen", Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp), color = DefaultColors.TEXT_ON_SECONDARY.color)
                                        Text("  Reihengröße: ${result.columnSize}", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                        Text("  Reihenmenge: ${result.columnCount}", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                        Text("  Farbenmenge: ${result.colorAmount}", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                        Text("  Lösung generiert: ${result.generateInitialPins}", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                        Text("  Farben mehrfach verwendbar: ${result.duplicateColors}", color = DefaultColors.TEXT_ON_SECONDARY.color)
                                    }
                                }
                            }
                        }
                    }
                    else Text("Noch keine Spiele gespielt", color = DefaultColors.TEXT_ON_PRIMARY.color)
                }
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
    duplicateColors: MutableState<Boolean>,
    pinSize: MutableState<Float>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .shadow(5.dp, RoundedCornerShape(5.dp))
            .background(DefaultColors.PRIMARY.color, RoundedCornerShape(5.dp))
    ) {
        Text("Lösung setzen",
            color = DefaultColors.TEXT_ON_SECONDARY.color,
            modifier = Modifier
                .shadow(5.dp, RoundedCornerShape(5.dp))
                .background(DefaultColors.SECONDARY.color, RoundedCornerShape(5.dp))
                .padding(5.dp)
        )

        solution.clear()

        if(generateInitialPins.value) {
            generateInitialPins(columnSize, colorAmount, solution, duplicateColors)

            gamePhase.value = GamePhases.PLAYING
        }
        else {
            Column(
                Modifier.padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(columnSize.value) {
                    solution.add(Pin(Color.Black))
                }

                pinSizeSlider(pinSize)
                Board.row(solution, columnSize, colorAmount, pinSize)

                Button(
                    onClick = {
                        if(!solution.none { it.color == Color.Black }) return@Button

                        gamePhase.value = GamePhases.PLAYING
                    },
                    content = { Text(if(solution.none { it.color == Color.Black }) "Fertig" else "Nicht alle gesetzt")},
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DefaultColors.HIGHLIGHT.color,
                        contentColor = DefaultColors.TEXT_ON_HIGHLIGHT.color,
                        disabledBackgroundColor = DefaultColors.BACKGROUND.color,
                        disabledContentColor = DefaultColors.SECONDARY.color
                    ),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }
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
    pinSize: MutableState<Float>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(10.dp)
            .shadow(5.dp, RoundedCornerShape(5.dp))
            .background(DefaultColors.PRIMARY.color, RoundedCornerShape(5.dp))
    ) {
        Text("Lösen",
            color = DefaultColors.TEXT_ON_SECONDARY.color,
            modifier = Modifier
                .shadow(5.dp, RoundedCornerShape(5.dp))
                .background(DefaultColors.SECONDARY.color, RoundedCornerShape(5.dp))
                .padding(5.dp)
        )
        Column(
            Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val currentColumn = remember { mutableStateOf(0) }

            pinSizeSlider(pinSize)
            Board.rows(columns, columnSize, currentColumn, gamePhase, solution, won, neededTries, colorAmount, pinSize)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalUuidApi::class)
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
    duplicateColors: MutableState<Boolean>,
    pinSize: MutableState<Float>
) {
    Row {
        Column(
            Modifier
                .width(300.dp)
                .padding(10.dp)
                .shadow(5.dp, RoundedCornerShape(5.dp))
                .background(DefaultColors.PRIMARY.color, RoundedCornerShape(5.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ende",
                color = DefaultColors.TEXT_ON_SECONDARY.color,
                modifier = Modifier
                    .shadow(5.dp, RoundedCornerShape(5.dp))
                    .background(DefaultColors.SECONDARY.color, RoundedCornerShape(5.dp))
                    .padding(5.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(5.dp)
            ) {
                if(won.value) Text("Du hast das Muster gefunden", color = DefaultColors.TEXT_ON_PRIMARY.color)
                else Text("Du hast das Muster nicht rechtzeitig gefunden", color = DefaultColors.TEXT_ON_PRIMARY.color)

                Text("Gebrauchte Versuche: ${neededTries.value}", color = DefaultColors.TEXT_ON_PRIMARY.color)

                Button(
                    onClick = {
                        gamePhase.value = GamePhases.BEFORE_GAME
                    },
                    content = { Text("Neustarten") },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = DefaultColors.HIGHLIGHT.color,
                        contentColor = DefaultColors.TEXT_ON_HIGHLIGHT.color,
                        disabledBackgroundColor = DefaultColors.PRIMARY.color,
                        disabledContentColor = DefaultColors.SECONDARY.color
                    ),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(10.dp)
                .shadow(5.dp, RoundedCornerShape(5.dp))
                .background(DefaultColors.PRIMARY.color, RoundedCornerShape(5.dp)),
        ) {
            Text("Lösung:",
                color = DefaultColors.TEXT_ON_SECONDARY.color,
                modifier = Modifier
                    .shadow(5.dp, RoundedCornerShape(5.dp))
                    .background(DefaultColors.SECONDARY.color, RoundedCornerShape(5.dp))
                    .padding(5.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(5.dp)
            ) {
                Box(
                    Modifier
                        .clickable(enabled = false) {}
                        .align(Alignment.CenterHorizontally)
                ) {
                    Board.immutableRow(solution, columnSize, pinSize)
                }
            }
        }
    }

    val result = GameResult(won.value, neededTries.value, columnSize.value, columnCount.value, colorAmount.value, generateInitialPins.value, duplicateColors.value)
    gameResults.value.addLast(result)
    println("Result: $result")

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
    duplicateColors: MutableState<Boolean>,
    pinSize: MutableState<Float>
) {
    Column(
        Modifier.padding(5.dp)
    ) {

        // row amount
        textFieldInt(columnCount, "Reihenanzahl")

        // row size
        textFieldInt(columnSize, "Reihengröße")

        // color amount
        textFieldInt(colorAmount, "Farbenmenge (max ${PinColors.entries.size})")

        // generate initial pins
        Row {
            Checkbox(
                checked = generateInitialPins.value,
                onCheckedChange = {
                    generateInitialPins.value = it
                    println("Changed generateInitialPins value to ${generateInitialPins.value}")
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = DefaultColors.HIGHLIGHT.color,
                    uncheckedColor = DefaultColors.SECONDARY.color,
                    checkmarkColor = DefaultColors.TEXT_ON_PRIMARY.color,
                    disabledColor = DefaultColors.PRIMARY.color,
                    disabledIndeterminateColor = DefaultColors.SECONDARY.color
                ),
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            )
            Text("Lösung generieren", color = DefaultColors.TEXT_ON_PRIMARY.color)
        }

        // duplicate colors
        Row {
            Checkbox(
                checked = duplicateColors.value,
                onCheckedChange = {
                    duplicateColors.value = it
                    println("Changed duplicateColors value to ${duplicateColors.value}")
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = DefaultColors.HIGHLIGHT.color,
                    uncheckedColor = DefaultColors.SECONDARY.color,
                    checkmarkColor = DefaultColors.TEXT_ON_PRIMARY.color,
                    disabledColor = DefaultColors.PRIMARY.color,
                    disabledIndeterminateColor = DefaultColors.SECONDARY.color
                ),
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            )
            Text("Farben mehrfach verwenden", color = DefaultColors.TEXT_ON_PRIMARY.color)
        }

        // pin Size
        pinSizeSlider(pinSize)
    }
}

@Composable
fun textFieldInt(value: MutableState<Int>, label: String) {
    val focusManager = LocalFocusManager.current

    var columnAmountValue by remember { mutableStateOf(value.value.toString()) }

    OutlinedTextField(
        value = columnAmountValue,
        onValueChange = { text ->
            columnAmountValue = text
        },
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                if(columnAmountValue.toIntOrNull() == null) return@onPreviewKeyEvent false

                when (event.key) {
                    Key.Enter -> {
                        value.value = columnAmountValue.toInt()

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
        label = { Text(label, color = DefaultColors.TEXT_ON_PRIMARY.color) },
        isError = columnAmountValue.toIntOrNull() == null,
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = DefaultColors.TEXT_ON_PRIMARY.color,
            cursorColor = DefaultColors.TEXT_ON_PRIMARY.color,

            focusedBorderColor = DefaultColors.TEXT_ON_PRIMARY.color,
            unfocusedBorderColor = DefaultColors.SECONDARY.color,

            focusedLabelColor = DefaultColors.TEXT_ON_PRIMARY.color,
            unfocusedLabelColor = DefaultColors.SECONDARY.color
        )
    )
}

@Composable
fun pinSizeSlider(pinSize: MutableState<Float>) {
    Row {
        Text("Pin Größe (${round(pinSize.value * 10) / 10.0f})", color = DefaultColors.TEXT_ON_PRIMARY.color)
        Slider(
            value = pinSize.value,
            onValueChange = {pinSize.value = it},
            valueRange = 0.5f..2f,
            modifier = Modifier
                .width(300.dp)
                .pointerHoverIcon(PointerIcon.Hand),
            colors = SliderDefaults.colors(
                thumbColor = DefaultColors.HIGHLIGHT.color,
                activeTrackColor = DefaultColors.HIGHLIGHT.color,
                inactiveTrackColor = DefaultColors.SECONDARY.color
            )
        )
    }
}

fun main() = application {
    val icon = painterResource("icon.png")

    val windowTitle = remember { mutableStateOf("MasterMind") }

    Window(
        onCloseRequest = ::exitApplication,
        title = windowTitle.value,
        icon = icon
    ) {
        App(windowTitle)
    }
}