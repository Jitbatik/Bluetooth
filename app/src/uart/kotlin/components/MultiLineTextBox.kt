import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.bluetooth.presentation.view.home.CharUI
import com.example.bluetooth.ui.theme.psisFontFamily

@NonRestartableComposable
@Composable
fun MultiLineTextBox(
    charUIList: () -> List<CharUI>,
    lines: Int,
    modifier: Modifier = Modifier,
) {
    val charList = charUIList()
    val rows = charList.chunked(charList.size / lines)
    val rowTexts = rows.map { row -> row.joinToString("") { it.char.toString() } }

    Box(
        modifier = modifier
    ) {
        Column {
            rowTexts.forEach { rowText ->
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    text = rowText,
                    fontFamily = psisFontFamily,
                    fontSize = 25.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
