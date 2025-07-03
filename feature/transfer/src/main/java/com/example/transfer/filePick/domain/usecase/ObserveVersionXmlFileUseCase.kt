package com.example.transfer.filePick.domain.usecase

import com.example.transfer.filePick.ChartXmlFileType
import com.example.transfer.filePick.FilesMetadataRepository
import com.example.transfer.filePick.domain.FilesMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveVersionXmlFileUseCase @Inject constructor(
    private val fileRepository: FilesMetadataRepository
) {
    operator fun invoke(): Flow<List<FilesMetadata>> {
        return fileRepository.observe()
            .map { list ->
                list.filter { it.type === ChartXmlFileType.VersionXml }
                    .sortedByDescending { it.addedDate }
            }
    }
}