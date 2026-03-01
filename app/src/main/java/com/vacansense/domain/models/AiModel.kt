package com.vacansense.domain.models

enum class DownloadState { NOT_DOWNLOADED, DOWNLOADING, DOWNLOADED }

data class AiModel(
    val fileName: String,
    val name: String,
    val downloadUrl: String,
    val size: String,
    val downloadProgress: Int = 0,
    val state: DownloadState = DownloadState.NOT_DOWNLOADED
)
