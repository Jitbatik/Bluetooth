import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.home.state.HomeState
import components.ButtonFBox
import components.ButtonHelpBox

@NonRestartableComposable
@Composable
fun Home(state: HomeState) {
    Box(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ButtonFBox(
                buttonType = true,
                onEvent = state.onEvents
            )
            SpacerDivider()
            MultiLineTextBox(
                charUIList = { state.data },
                lines = 4,
                modifier = Modifier
                    .background(Color(0xFF61D7A4))
                    .padding(4.dp)
                    .fillMaxWidth(),
            )
            SpacerDivider()
            ButtonFBox(
                buttonType = false,
                onEvent = state.onEvents
            )
            ButtonHelpBox( onEvent = state.onEvents)
        }
    }
}

@Composable
private fun SpacerDivider() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(Color.Black)
    )
}