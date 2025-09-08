import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex

object Board {
    @Composable
    fun rows(
        columns: SnapshotStateList<SnapshotStateList<Pin>>,
        columnSize: MutableState<Int>,
        currentColumn: MutableState<Int>,
        gamePhase: MutableState<GamePhases>,
        solution: MutableList<Pin>,
        won: MutableState<Boolean>,
        neededTries: MutableState<Int>,
        colorAmount: MutableState<Int>,
        pinSize: MutableState<Float>
    ) {
        Column (
            modifier = Modifier
                .fillMaxHeight()
        ) {
            for (column in columns) {
                // Evaluation Data
                val perfectPins = remember { mutableStateOf(0) } // right position and color
                val rightColorPin = remember { mutableStateOf(0) } // only right color

                Box {
                    if(currentColumn.value != columns.indexOf(column)) {
                        Box(
                            Modifier
                                .clickable(enabled = false){}
                                .zIndex(2f)
                                .size((((45 * columnSize.value) + (30 * columnSize.value)) * pinSize.value).dp, (45 * pinSize.value).dp)
                        )
                    }

                    Row(Modifier.zIndex(1f)) {
                        evaluationRow(perfectPins, rightColorPin, columnSize, pinSize)

                        row(column, columnSize, colorAmount, pinSize)

                        if(currentColumn.value == columns.indexOf(column)) {
                            Button(
                                modifier = Modifier
                                    .size((46 * pinSize.value).dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .pointerHoverIcon(if(column.none {it.color == Color.Black}) PointerIcon.Hand else PointerIcon.Default),
                                onClick = {
                                    if(currentColumn.value < columns.size - 1) {
                                        val evaluation = evaluate(column, solution)
                                        perfectPins.value = evaluation.first
                                        rightColorPin.value = evaluation.second

                                        if(evaluation.first == columnSize.value) {
                                            won.value = true
                                            neededTries.value = currentColumn.value + 1

                                            gamePhase.value = GamePhases.FINISHED
                                            println("game won")
                                        }
                                        else {
                                            currentColumn.value++
                                            println("Column ${currentColumn.value}")
                                        }
                                    }
                                    else {
                                        neededTries.value = currentColumn.value + 1

                                        gamePhase.value = GamePhases.FINISHED
                                        println("game lost")
                                    }
                                },
                                content = {
                                    Text("✓")
                                },
                                enabled = column.none {it.color == Color.Black},
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = DefaultColors.HIGHLIGHT.color,
                                    contentColor = DefaultColors.TEXT_ON_HIGHLIGHT.color,
                                    disabledBackgroundColor = DefaultColors.PRIMARY.color,
                                    disabledContentColor = DefaultColors.TEXT_ON_PRIMARY.color
                                )
                            )
                        }
                    }

                        AnimatedContent(
                            targetState = currentColumn.value,
                            transitionSpec = {
                                // Jetzt kannst du den alten mit dem neuen Index vergleichen!
                                if (targetState < initialState) {
                                    // Index wird größer (nach unten gescrollt)
                                    slideInVertically { height -> height } + fadeIn() togetherWith
                                            slideOutVertically { height -> -height } + fadeOut()
                                } else {
                                    // Index wird kleiner (nach oben gescrollt)
                                    slideInVertically { height -> -height } + fadeIn() togetherWith
                                            slideOutVertically { height -> height } + fadeOut()
                                }.using(SizeTransform(clip = false))
                            },
                            label = "animatedSelection"
                        ) { targetIndex ->
                            if(targetIndex == columns.indexOf(column)) {
                                Box(
                                    Modifier
                                        .size((((45 * columnSize.value) + (30 * columnSize.value)) * pinSize.value).dp, (45 * pinSize.value).dp)
                                        .shadow(
                                            5.dp,
                                            RoundedCornerShape(5.dp)
                                        )
                                        .background(
                                            DefaultColors.SECONDARY.color,
                                            RoundedCornerShape(5.dp)
                                        )
                                        .zIndex(0f)
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

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun row(
        pins: List<Pin>,
        columnSize: MutableState<Int>,
        colorAmount: MutableState<Int>,
        pinSize: MutableState<Float>
    ) {
        Row(
            modifier = Modifier
                .size(((45 * columnSize.value) * pinSize.value).dp, (45 * pinSize.value).dp)
                .background(Color.Transparent)
        ) {
            for (pin in pins) {
                var showPopup by remember { mutableStateOf(false) }
                val boxPosition by remember { mutableStateOf(Offset.Zero) }
                var isHovered by remember { mutableStateOf(false) }

                val scale by animateFloatAsState(
                    targetValue = if(isHovered) 1.15f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                val shadow by animateDpAsState(
                    targetValue = if(isHovered) 5.dp else 0.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .padding((5 * pinSize.value).dp)
                        .shadow(shadow, CircleShape)
                        .size((35 * pinSize.value).dp)
                        .background(pin.color, CircleShape)
                        .clip(CircleShape)
                        .clickable{ showPopup = true }
                        .onPointerEvent(PointerEventType.Enter) {
                            isHovered = true
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            isHovered = false
                        }
                        .pointerHoverIcon(PointerIcon.Hand)
                )

                if (showPopup) {
                    Popup(
                        offset = IntOffset(boxPosition.x.toInt() + 100, boxPosition.y.toInt()),
                        onDismissRequest = { showPopup = false }
                    ) {
                        Column(
                            modifier = Modifier
                                .shadow(30.dp, RoundedCornerShape((8 * pinSize.value).dp))
                                .background(DefaultColors.SECONDARY.color, shape = RoundedCornerShape((8 * pinSize.value).dp))
                                .padding((16 * pinSize.value).dp)
                        ) {
                            Text("Wähle eine Farbe:", color = DefaultColors.TEXT_ON_SECONDARY.color)
                            Spacer(modifier = Modifier.height((8 * pinSize.value).dp))
                            Row {
                                for(color in PinColors.entries) {
                                    var isHovered by remember { mutableStateOf(false) }

                                    val scale by animateFloatAsState(
                                        targetValue = if(isHovered) 1.15f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )

                                    val shadow by animateDpAsState(
                                        targetValue = if(isHovered) 5.dp else 0.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )

                                    if(PinColors.entries.indexOf(color) >= colorAmount.value) continue

                                    val colorPickerPin = Pin(color.color)

                                    Box (Modifier
                                        .scale(scale)
                                        .padding((5 * pinSize.value).dp)
                                        .shadow(shadow, CircleShape)
                                        .size((35 * pinSize.value).dp)
                                        .background(colorPickerPin.initialColor, CircleShape)
                                        .clip(CircleShape)
                                        .clickable {
                                            pin.color = color.color
                                            showPopup = false
                                        }
                                        .onPointerEvent(PointerEventType.Enter) {
                                            isHovered = true
                                        }
                                        .onPointerEvent(PointerEventType.Exit) {
                                            isHovered = false
                                        }
                                        .pointerHoverIcon(PointerIcon.Hand)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .padding((5 * pinSize.value).dp)
                                        .background(DefaultColors.HIGHLIGHT.color, RoundedCornerShape((15 * pinSize.value).dp))
                                        .size((3 * pinSize.value).dp, (35 * pinSize.value).dp)
                                )

                                val colorPickerPin = Pin(Color.Black)
                                Box (Modifier
                                    .padding((5 * pinSize.value).dp)
                                    .size((35 * pinSize.value).dp)
                                    .background(colorPickerPin.initialColor, CircleShape)
                                    .clip(CircleShape)
                                    .clickable {
                                        pin.color = Color.Black
                                        showPopup = false
                                    }
                                    .pointerHoverIcon(PointerIcon.Hand)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun immutableRow(
        pins: List<Pin>,
        columnSize: MutableState<Int>,
        pinSize: MutableState<Float>
    ) {
        Row(
            modifier = Modifier
                .size(((45 * columnSize.value) * pinSize.value).dp, (45 * pinSize.value).dp)
                .background(Color.Transparent)
        ) {
            for (pin in pins) {
                Box(
                    modifier = Modifier
                        .padding((5 * pinSize.value).dp)
                        .size((35 * pinSize.value).dp)
                        .background(pin.color, CircleShape)
                        .clip(CircleShape)
                )
            }
        }
    }

    @Composable
    fun evaluationRow(
        perfectPins: MutableState<Int>,
        rightColorPin: MutableState<Int>,
        columnSize: MutableState<Int>,
        pinSize: MutableState<Float>
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

        Row(
            modifier = Modifier
                .size(((30 * columnSize.value) * pinSize.value).dp, (45 * pinSize.value).dp)
                .background(Color.Transparent)
        ) {
            for (color in colors) {
                Box(
                    modifier = Modifier
                        .padding((5 * pinSize.value).dp)
                        .size((20 * pinSize.value).dp)
                        .background(color, CircleShape)
                        .clip(CircleShape)
                )
            }
        }
    }
}