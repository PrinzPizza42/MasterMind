import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

object FinishedBoard {
    var pinRowList = ArrayList<List<Pin>>().toList()

    @Composable
    fun paint(pinSize: MutableState<Float>) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(10.dp)
                .shadow(5.dp, RoundedCornerShape(5.dp))
                .background(DefaultColors.PRIMARY.color, RoundedCornerShape(5.dp)),
        ) {
            Text(
                text = "Spielverlauf:",
                color = DefaultColors.TEXT_ON_SECONDARY.color,
                modifier = Modifier
                    .shadow(5.dp, RoundedCornerShape(5.dp))
                    .background(DefaultColors.SECONDARY.color, RoundedCornerShape(5.dp))
                    .padding(5.dp)
            )
            var index = 0
            for (row in pinRowList) {
                index++
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
                        row(row, pinSize, index)
                    }
                }
            }
        }
    }

    @Composable
    private fun row(row: List<Pin>, pinSize: MutableState<Float>, index: Int) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = index.toString(),
                color = DefaultColors.TEXT_ON_PRIMARY.color,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(4.dp),
                fontSize = TextUnit(1.25f, TextUnitType.Em)
            )
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