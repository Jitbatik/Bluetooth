package com.example.transfer.filePick.domain.usecase

import com.example.transfer.filePick.ChartXmlFileType
import com.example.transfer.filePick.FilesMetadataRepository
import com.example.transfer.filePick.domain.FilesMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveSingleCommonXmlFileUseCase @Inject constructor(
    private val filesMetadataRepository: FilesMetadataRepository
) {
    operator fun invoke(): Flow<FilesMetadata?> {
        return filesMetadataRepository.observe()
            .map { list ->
                list.filter { it.type == ChartXmlFileType.CommonXml }
                    .maxByOrNull { it.addedDate }
            }
    }
}