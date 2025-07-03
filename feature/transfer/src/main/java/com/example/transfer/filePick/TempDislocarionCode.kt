package com.example.transfer.filePick

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.transfer.filePick.domain.FilesMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed interface FileType

enum class ChartXmlFileKind(val raw: String) {
    COMMON("CommonXml"),
    VERSION("VersionXml");

    companion object {
        fun fromRaw(raw: String) = entries.firstOrNull { it.raw == raw } ?: COMMON
    }
}

sealed interface ChartXmlFileType : FileType {
    val kind: ChartXmlFileKind

    data object CommonXml : ChartXmlFileType {
        override val kind = ChartXmlFileKind.COMMON
    }

    data object VersionXml : ChartXmlFileType {
        override val kind = ChartXmlFileKind.VERSION
    }
}


class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun saveFile(uri: Uri, type: FileType): FilesMetadata {
        val originalName = extractFileName(uri)
        val file = File(context.filesDir, originalName)

        context.contentResolver.openInputStream(uri).use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output) ?: error("Failed to open URI input stream")
            }
        }

        return FilesMetadata(
            name = originalName,
            addedDate = System.currentTimeMillis(),
            type = type,
            localPath = file.absolutePath
        )
    }

    fun deleteFile(name: String) {
        val file = File(context.filesDir, name)
        if (file.exists()) file.delete()
    }

    private fun extractFileName(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index)
            }
        }
        return uri.path?.substringAfterLast('/') ?: "unknown_file.xml"
    }
}


@Singleton
class FilesMetadataRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _filesFlow = MutableStateFlow(loadMetadata())
    private val _selectedVersionFileFlow = MutableStateFlow(
        prefs.getString(SELECTED_VERSION_KEY, null)
    )

    fun observe(): StateFlow<List<FilesMetadata>> = _filesFlow.asStateFlow()
    fun observeSelectedVersionFile(): StateFlow<String?> = _selectedVersionFileFlow.asStateFlow()

    fun add(metadata: FilesMetadata) {
        _filesFlow.update { it + metadata }
        saveMetadata()
    }

    fun remove(name: String) {
        _filesFlow.update { it.filterNot { it.name == name } }
        if (_selectedVersionFileFlow.value == name) setSelectedFileName(null)
        saveMetadata()
    }

    fun setSelectedFileName(name: String?) {
        _selectedVersionFileFlow.value = name
        prefs.edit().putString(SELECTED_VERSION_KEY, name).apply()
    }

    private fun saveMetadata() {
        val jsonArray = JSONArray().apply {
            _filesFlow.value.forEach { put(it.toJson()) }
        }
        prefs.edit().putString(FILES_KEY, jsonArray.toString()).apply()
    }

    private fun loadMetadata(): List<FilesMetadata> {
        val jsonString = prefs.getString(FILES_KEY, null) ?: return emptyList()

        return runCatching {
            val jsonArray = JSONArray(jsonString)
            List(jsonArray.length()) { index ->
                jsonArray.getJSONObject(index).toFilesMetadata()
            }
        }.getOrElse {
            it.printStackTrace()
            emptyList()
        }
    }

    private fun FilesMetadata.toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("addedDate", addedDate)
        put(
            "type", when (type) {
                is ChartXmlFileType.CommonXml -> ChartXmlFileKind.COMMON.raw
                is ChartXmlFileType.VersionXml -> ChartXmlFileKind.VERSION.raw
            }
        )
        put("localPath", localPath)
    }

    private fun JSONObject.toFilesMetadata(): FilesMetadata {
        val kind = ChartXmlFileKind.fromRaw(getString("type"))
        return FilesMetadata(
            name = getString("name"),
            addedDate = getLong("addedDate"),
            type = when (kind) {
                ChartXmlFileKind.COMMON -> ChartXmlFileType.CommonXml
                ChartXmlFileKind.VERSION -> ChartXmlFileType.VersionXml
            },
            localPath = getString("localPath")
        )
    }

    companion object {
        private const val PREFS_NAME = "files_metadata_prefs"
        private const val FILES_KEY = "files_metadata_list"
        private const val SELECTED_VERSION_KEY = "selected_version_file"
    }
}


class ChartFilesInteractor @Inject constructor(
    private val fileStorageManager: FileStorageManager,
    private val filesMetadataRepository: FilesMetadataRepository
) {
    fun add(uri: Uri, type: ChartXmlFileType) {
        val metadata = fileStorageManager.saveFile(uri, type)
        filesMetadataRepository.add(metadata)
    }

    fun delete(name: String) {
        fileStorageManager.deleteFile(name)
        filesMetadataRepository.remove(name)
    }

    fun select(name: String) {
        filesMetadataRepository.setSelectedFileName(name)
    }
}

//todo если не загружен общий файл загрузить файл версии нельзя
//todo если загружен общий файл то можно загрузить сколько угодно файлов версий

//todo если удалить общий файл то файлы версий удаляються все сразу
//todo если удалить  файл версии то общий файлы это никак не затронет

//todo если загружен один файл версий то он становиться выбранным автоматически
//todo если несколько файлов версий и пользователь не быбрал никакой из них сам то тот что  был загружен позже будет выбран автоматически

//todo  если несколько файлов версий и пользователь выбрал то
// не зависомо когда был загружен файл выбор падает на тот что выбрал пользователь

//todo если у загружаемого и загруженного файла совпадает имя то старый удаляется новый остается1221