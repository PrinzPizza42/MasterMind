import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

object FinishedBoard {
    var pinRowList = ArrayList<List<Pin>>().toList()

    @Composable
    fun paint(pinSize: MutableState<Float>) {
        for (row in pinRowList) {
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
                    row(row, pinSize)
                }
            }
        }
    }

    @Composable
    private fun row(row: List<Pin>, pinSize: MutableState<Float>) {
        Row {
            for (pin in row) {
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
}