package com.example.transfer.filePick.domain

import com.example.transfer.filePick.FileType

data class FilesMetadata(
    val name: String,
    val addedDate: Long,
    val type: FileType,
    val localPath: String
)