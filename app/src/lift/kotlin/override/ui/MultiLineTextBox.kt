package override.ui

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
import com.psis.elimlift.presentation.view.home.DataUI
import com.psis.elimlift.ui.theme.psisFontFamily


@NonRestartableComposable
@Composable
fun MultiLineTextBox(
    dataUIList: () -> List<DataUI>,
    lines: Int,
    modifier: Modifier = Modifier,
) {
    val charList = dataUIList()
    val chunkSize = if (charList.isEmpty()) 20 else maxOf(1, charList.size / lines)
    val rows = charList.chunked(chunkSize)
    val rowTexts = rows.map { row -> row.joinToString("") { it.data } }

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
