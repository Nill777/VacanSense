package com.vacansense.data.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import com.vacansense.domain.models.AiModel
import com.vacansense.domain.models.DownloadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class ModelDownloadManager(private val context: Context) {

    // ДОБАВЛЕНО ?download=true чтобы HuggingFace отдавал файл, а не HTML
    val predefinedModels = listOf(
        AiModel(
            "qwen2.5-0.5b-instruct-q8_0.gguf",
            "Qwen 2.5 (0.5B)",
            "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q8_0.gguf?download=true",
            "~550 MB"
        ),
        AiModel(
            "tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            "TinyLlama (1.1B)",
            "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf?download=true",
            "~680 MB"
        )
    )

    private val _modelsState = MutableStateFlow<List<AiModel>>(emptyList())
    val modelsState: StateFlow<List<AiModel>> = _modelsState

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val activeDownloads = mutableMapOf<Long, String>()

    init {
        checkLocalFiles()
    }

    private fun checkLocalFiles() {
        val filesDir = context.filesDir
        val updated = predefinedModels.map { model ->
            val file = File(filesDir, model.fileName)
            // Жесткая проверка: GGUF модели весят сотни мегабайт.
            // Если файл меньше 10 МБ - это ошибка скачивания (HTML-заглушка). Мы его удаляем.
            if (file.exists()) {
                if (file.length() > 10 * 1024 * 1024) {
                    model.copy(state = DownloadState.DOWNLOADED, downloadProgress = 100)
                } else {
                    Log.e(
                        "DownloadManager",
                        "Файл ${model.fileName} поврежден (размер ${file.length()} байт). Удаляем."
                    )
                    file.delete()
                    model.copy(state = DownloadState.NOT_DOWNLOADED, downloadProgress = 0)
                }
            } else {
                model
            }
        }
        _modelsState.value = updated
    }

    fun getAbsolutePath(fileName: String): String {
        return File(context.filesDir, fileName).absolutePath
    }

    fun downloadModel(model: AiModel) {
        // Удаляем "битые" остатки перед новым скачиванием
        File(context.getExternalFilesDir(null), model.fileName).delete()
        File(context.filesDir, model.fileName).delete()

        val request = DownloadManager.Request(Uri.parse(model.downloadUrl))
            .setTitle("Загрузка AI Модели")
            .setDescription(model.name)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(context, null, model.fileName)

        val downloadId = downloadManager.enqueue(request)
        activeDownloads[downloadId] = model.fileName

        updateModelState(model.fileName, DownloadState.DOWNLOADING, 0)
    }

    suspend fun observeDownloads() {
        while (true) {
            val iterList = activeDownloads.toMap()
            if (iterList.isEmpty()) {
                delay(2000)
                continue
            }
            for ((id, fileName) in iterList) {
                val q = DownloadManager.Query().setFilterById(id)
                downloadManager.query(q)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val status =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val downloaded =
                            cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total =
                            cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val externalFile = File(context.getExternalFilesDir(null), fileName)
                            val internalFile = File(context.filesDir, fileName)

                            // Проверяем, не скачалась ли пустышка
                            if (externalFile.exists() && externalFile.length() < 10 * 1024 * 1024) {
                                externalFile.delete()
                                updateModelState(fileName, DownloadState.NOT_DOWNLOADED, 0)
                                activeDownloads.remove(id)
                                continue
                            }

                            if (externalFile.exists() && externalFile.absolutePath != internalFile.absolutePath) {
                                try {
                                    externalFile.copyTo(internalFile, overwrite = true)
                                    externalFile.delete()
                                } catch (e: Exception) {
                                    Log.e("DownloadManager", "Move file failed", e)
                                }
                            }

                            updateModelState(fileName, DownloadState.DOWNLOADED, 100)
                            activeDownloads.remove(id)

                        } else if (status == DownloadManager.STATUS_FAILED) {
                            updateModelState(fileName, DownloadState.NOT_DOWNLOADED, 0)
                            activeDownloads.remove(id)
                        } else {
                            val progress =
                                if (total > 0) ((downloaded * 100) / total).toInt() else 0
                            updateModelState(fileName, DownloadState.DOWNLOADING, progress)
                        }
                    }
                }
            }
            delay(1000)
        }
    }

    private fun updateModelState(fileName: String, state: DownloadState, progress: Int) {
        val current = _modelsState.value.toMutableList()
        val index = current.indexOfFirst { it.fileName == fileName }
        if (index != -1) {
            current[index] = current[index].copy(state = state, downloadProgress = progress)
            _modelsState.value = current
        }
    }
}
