package com.example.transfer.filePick.domain.usecase

import com.example.transfer.filePick.FilesMetadataRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSelectedFileVersionUseCase @Inject constructor(
    private val fileRepository: FilesMetadataRepository
) {
    operator fun invoke(): Flow<String?> {
        return fileRepository.observeSelectedVersionFile()
    }

}