package com.example.bluetooth.presentation.view.connectcontainer.scanner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.bluetooth.ui.theme.BluetoothTheme

@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
    ),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shadowColor: Color,
    shadowBottomOffset: Float,
    buttonHeight: Float = 0f,
    toggleMode: Boolean = false,
    isPressed: Boolean? = null, // Добавлен новый параметр
    content: @Composable () -> Unit,
) {
    var isToggled by remember { mutableStateOf(false) }
    val isLocalPressed = interactionSource.collectIsPressedAsState().value
    val buttonPressedState = when {
        isPressed != null -> isPressed // Используем внешнее значение isPressed, если оно передано
        toggleMode -> isToggled // Используем состояние переключателя, если включен toggleMode
        else -> isLocalPressed // Иначе используем локальное состояние нажатия
    }

    val density = LocalDensity.current
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight.dp + shadowBottomOffset.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .width(with(density) { buttonSize.width.toDp() })
                .align(Alignment.BottomCenter)
                .height(
                    buttonHeight.dp + if (!buttonPressedState)
                        shadowBottomOffset.dp
                    else
                        (shadowBottomOffset * 0.1).dp
                ),
            color = shadowColor,
            shape = shape,
        ) {}

        Button(
            onClick = {
                if (toggleMode) isToggled = !isToggled
                onClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .onSizeChanged { size -> buttonSize = size }
                .padding(
                    top = if (!buttonPressedState) 0.dp else (shadowBottomOffset * 0.7).dp
                )
                .height(buttonHeight.dp),
            enabled = enabled,
            shape = shape,
            colors = colors,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
        ) {
            content()
        }
    }
}



@PreviewLightDark
@Composable
fun AnimatedButtonPreview(
) = BluetoothTheme {
    Surface {
        AnimatedButton(
            modifier = Modifier.padding(all = 16.dp),
            shadowColor = Color.DarkGray,
            shadowBottomOffset = 12f,
            buttonHeight = 50f,
            shape = RoundedCornerShape(18.dp),
            onClick = { },
            toggleMode = true
        ) {
            Text(text = "test")
        }
    }
}