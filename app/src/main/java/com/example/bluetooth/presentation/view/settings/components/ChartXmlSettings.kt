package com.example.bluetooth.presentation.view.settings.components

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetooth.presentation.view.settings.model.ChartXmlPickerEvent
import com.example.bluetooth.presentation.view.settings.model.SettingsEvent
import com.example.transfer.filePick.ChartXmlFileType
import com.example.transfer.filePick.domain.FilesMetadata

@Composable
fun rememberFilePickerLauncher(
    onResult: (Uri) -> Unit
): ManagedActivityResultLauncher<String, Uri?> =
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onResult(it) }
    }


@Composable
fun rememberChartXmlFilePickerLauncher(
    fileType: ChartXmlFileType,
    onEvent: (SettingsEvent) -> Unit
): ManagedActivityResultLauncher<String, Uri?> = rememberFilePickerLauncher { uri ->
    onEvent(ChartXmlPickerEvent.UploadFile(uri = uri, fileType = fileType))
}

@Composable
fun ChartXmlSettings(
    commonFile: FilesMetadata?,
    versionFiles: List<FilesMetadata>,
    onEvent: (SettingsEvent) -> Unit,
    selectedFileName: String?
) {
    val commonPicker = rememberChartXmlFilePickerLauncher(ChartXmlFileType.CommonXml, onEvent)
    val versionPicker = rememberChartXmlFilePickerLauncher(ChartXmlFileType.VersionXml, onEvent)

    ExpandableItem(title = "Параметры сигналов") {
        CommonFileUploadCard(
            title = "Загруженный общий файл версий",
            commonFile = commonFile,
            onUploadClick = { commonPicker.launch("text/xml") },
            onDelete = {
                onEvent(
                    ChartXmlPickerEvent.DeleteFile(
                        name = it,
                        fileType = ChartXmlFileType.CommonXml
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )

        VersionFilesCard(
            files = versionFiles,
            isUploadEnabled = commonFile != null,
            selectedFileName = selectedFileName,
            onSelectFile = {
                onEvent(
                    ChartXmlPickerEvent.SelectFile(
                        name = it,
                        fileType = ChartXmlFileType.VersionXml
                    )
                )
            },
            onAdd = { versionPicker.launch("text/xml") },
            onDelete = {
                onEvent(
                    ChartXmlPickerEvent.DeleteFile(
                        name = it,
                        fileType = ChartXmlFileType.VersionXml
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}


//@Composable
//@Preview
//private fun ChartXmlPickerContentPreview() {
//    val previewState = remember { mutableStateOf(ChartXmlPicker()) }
//
//    val updateState: (ChartXmlPicker) -> Unit = { previewState.value = it }
//    val getState: () -> ChartXmlPicker = { previewState.value }
//
//    val previewLogic = remember {
//        ChartXmlPickerPreviewLogic(updateState, getState)
//    }
//
//    val handler: (SettingsEvent) -> Unit = { event ->
//        when (event) {
//            is ChartXmlPickerEvent.UploadFileFromUri -> previewLogic.uploadFile(event.isCommonFile)
//            ChartXmlPickerEvent.DeleteCommonFile -> previewLogic.deleteCommonFile()
//            is ChartXmlPickerEvent.DeleteVersionFile -> previewLogic.deleteVersionFile(event.name)
//            is ChartXmlPickerEvent.SelectVersionFile -> previewLogic.selectVersionFile(event.name)
//            else -> {}
//        }
//    }
//
//    MaterialTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                ChartXmlSettings(
//                    state = previewState.value,
//                    onEvent = handler
//                )
//            }
//        }
//    }
//}
//
//private class ChartXmlPickerPreviewLogic(
//    private val updateState: (ChartXmlPicker) -> Unit,
//    private val getState: () -> ChartXmlPicker
//) {
//    fun uploadFile(isCommonFile: Boolean) {
//        val fileName = if (isCommonFile) {
//            "common_preview.xml"
//        } else {
//            "version_preview_${System.currentTimeMillis()}.xml"
//        }
//
//        val newState = if (isCommonFile) {
//            getState().copy(
//                commonFile = fileName,
//                uploadedVersionFiles = emptyList(),
//                selectedVersionFileName = null
//            )
//        } else {
//            val newFile = FileVersion(fileName, LocalDateTime.now())
//            getState().copy(
//                uploadedVersionFiles = listOf(newFile) + getState().uploadedVersionFiles
//            )
//        }
//
//        updateState(newState)
//    }
//
//    fun deleteCommonFile() {
//        updateState(getState().copy(commonFile = null))
//    }
//
//    fun deleteVersionFile(fileName: String) {
//        val updatedFiles = getState().uploadedVersionFiles.filterNot { it.name == fileName }
//        val selected =
//            if (getState().selectedVersionFileName == fileName) null else getState().selectedVersionFileName
//        updateState(
//            getState().copy(
//                uploadedVersionFiles = updatedFiles,
//                selectedVersionFileName = selected
//            )
//        )
//    }
//
//    fun selectVersionFile(fileName: String) {
//        updateState(getState().copy(selectedVersionFileName = fileName))
//    }
//}
