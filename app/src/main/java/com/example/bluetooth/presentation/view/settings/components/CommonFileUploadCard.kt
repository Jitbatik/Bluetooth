package com.example.bluetooth.presentation.view.settings.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.transfer.filePick.domain.FilesMetadata

@Composable
fun CommonFileUploadCard(
    title: String,
    commonFile: FilesMetadata?,
    onUploadClick: () -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    colors: CardColors = CardDefaults.cardColors(),
    transitionSpec: ContentTransform = fadeIn() togetherWith fadeOut()
) {
    Card(
        colors = colors,
        modifier = modifier,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = commonFile,
                    transitionSpec = { transitionSpec }
                ) { commonFile ->
                    Text(
                        text = commonFile?.name ?: "Файл не загружен",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                val (icon, handler) = if (commonFile != null) {
                    Icons.Default.Delete to { onDelete(commonFile.name) }
                } else {
                    Icons.Default.Add to onUploadClick
                }
                IconButton(onClick = handler) {
                    Icon(imageVector = icon, contentDescription = null)
                }

            }
        }
    }
}