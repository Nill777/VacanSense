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

    val predefinedModels = listOf(
        AiModel(
            fileName = "qwen2.5-0.5b-instruct-q8_0.gguf",
            name = "Qwen 2.5 (0.5B) - Ультра-быстрая",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q8_0.gguf?download=true",
            size = "~550 MB"
        ),
        AiModel(
            fileName = "llama-3.2-1b-instruct-q5_k_m.gguf",
            name = "Llama 3.2 (1B) - Лёгкая",
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q5_K_M.gguf?download=true",
            size = "~890 MB"
        ),
        AiModel(
            fileName = "qwen2.5-1.5b-instruct-q5_k_m.gguf",
            name = "Qwen 2.5 (1.5B) - Баланс (Рекомендуем)",
            downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q5_k_m.gguf?download=true",
            size = "~1.12 GB"
        ),
        AiModel(
            fileName = "llama-3.2-3b-instruct-q4_k_m.gguf",
            name = "Llama 3.2 (3B) - Мощная",
            downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf?download=true",
            size = "~1.95 GB"
        ),
        AiModel(
            fileName = "gemma-3-4b-it-q5_k_m.gguf",
            name = "Gemma 3 (4B) - Новинка Google",
            downloadUrl = "https://huggingface.co/unsloth/gemma-3-4b-it-GGUF/resolve/main/gemma-3-4b-it-Q5_K_M.gguf?download=true",
            size = "~2.8 GB"
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

    fun deleteModel(fileName: String) {
        try {
            val internalFile = File(context.filesDir, fileName)
            if (internalFile.exists()) {
                internalFile.delete()
            }

            val externalFile = File(context.getExternalFilesDir(null), fileName)
            if (externalFile.exists()) {
                externalFile.delete()
            }

            Log.d("DownloadManager", "Модель $fileName успешно удалена")

            updateModelState(fileName, DownloadState.NOT_DOWNLOADED, 0)
        } catch (e: Exception) {
            Log.e("DownloadManager", "Ошибка при удалении модели", e)
        }
    }
}
