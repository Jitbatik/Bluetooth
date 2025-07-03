package com.example.bluetooth.presentation.view.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.transfer.filePick.domain.FilesMetadata
import java.time.format.DateTimeFormatter

@Composable
fun VersionFilesCard(
    files: List<FilesMetadata>,
    isUploadEnabled: Boolean,
    selectedFileName: String?,
    onSelectFile: (String) -> Unit,
    onAdd: () -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
    colors: CardColors = CardDefaults.cardColors(),
    dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
) {
    Card(
        colors = colors,
        modifier = modifier,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Загруженные файлы версий",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(12.dp))

            FilledTonalButton(
                onClick = onAdd,
                enabled = isUploadEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить файл")
            }

            Spacer(Modifier.height(12.dp))

            if (files.isEmpty()) {
                val message =
                    if (isUploadEnabled) "Ни один файл версии не загружен" else "Сначала загрузите общий файл"
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                files.forEach { file ->
                    FileVersionItem(
                        fileName = file.name,
                        fileAdded = file.addedDate.toString(), // todo
                        isSelected = file.name == selectedFileName,
                        onSelect = { onSelectFile(file.name) },
                        onDelete = { onDelete(file.name) },
                        dateFormatter = dateFormatter
                    )
                }
            }
        }
    }
}