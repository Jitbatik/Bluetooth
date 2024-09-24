package com.example.bluetooth.presentation.view.connectcontainer.scanner

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AnimatedButton(
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
    ),
    shadowColor: Color,
    shadowBottomOffset: Float,
    buttonHeight: Float = 0f,
    shape: Shape = ButtonDefaults.shape,
    border: BorderStroke? = null,
    onClick: () -> Unit,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }
    val animatedShadowOffset by animateDpAsState(
        targetValue = if (isPressed) (shadowBottomOffset * 0.5).dp else shadowBottomOffset.dp,
        label = ""
    )
    val animatedButtonPadding by animateDpAsState(
        targetValue = if (isPressed) (shadowBottomOffset * 0.5).dp else 0.dp,
        label = ""
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100L)
            isPressed = false
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(buttonHeight.dp + animatedShadowOffset),
            color = shadowColor,
            shape = shape,
        ) {}
        Button(
            onClick = {
                isPressed = true
                onClick()
            },
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 0.dp,
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.containerColor,
                contentColor = colors.contentColor,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = animatedButtonPadding)
                .height(buttonHeight.dp),
            shape = shape,
            border = border,
            contentPadding = contentPadding
        ) {
            content()
        }
    }
}