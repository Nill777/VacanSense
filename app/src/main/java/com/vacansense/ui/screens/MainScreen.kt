package com.vacansense.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vacansense.domain.models.DownloadState
import com.vacansense.presenter.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    navController: NavController,
    onToggleBot: (Boolean) -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()
    val models by viewModel.modelsState.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    // Раскрытие выпадающего списка моделей
    var expandedModels by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "VacanSense",
                    color = MaterialTheme.colorScheme.primary
                )
            })
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = settings.tgToken,
                onValueChange = { s -> viewModel.updateSetting { it.copy(tgToken = s) } },
                label = { Text("Telegram Bot Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = settings.tgChatId,
                onValueChange = { s -> viewModel.updateSetting { it.copy(tgChatId = s) } },
                label = { Text("Telegram Chat ID / User ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = settings.query,
                onValueChange = { s -> viewModel.updateSetting { it.copy(query = s) } },
                label = { Text("Поисковый запрос (HH)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = { navController.navigate("filters") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Настроить фильтры HH")
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // -- МОДЕЛИ --
            ExposedDropdownMenuBox(
                expanded = expandedModels,
                onExpandedChange = { expandedModels = !expandedModels }
            ) {
                val selText = models.find { it.fileName == settings.selectedModelFileName }?.name
                    ?: "Модель не выбрана"
                OutlinedTextField(
                    value = selText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("LLM Модель") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedModels) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedModels,
                    onDismissRequest = { expandedModels = false }) {
                    models.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            model.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            model.size,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    if (model.state == DownloadState.DOWNLOADED) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            "Готово",
                                            tint = Color.Green
                                        )
                                    } else if (model.state == DownloadState.DOWNLOADING) {
                                        Text(
                                            "${model.downloadProgress}%",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Add,
                                            "Скачать",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable {
                                                viewModel.downloadModel(model)
                                            })
                                    }
                                }
                            },
                            onClick = {
                                if (model.state == DownloadState.DOWNLOADED) {
                                    viewModel.selectModel(model.fileName)
                                    expandedModels = false
                                }
                            }
                        )
                    }
                }
            }

            // -- АНИМАЦИОННЫЕ ПОЛЯ ПРОМТОВ --
            var posFocused by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = settings.positivePrompt,
                onValueChange = { s -> viewModel.updateSetting { it.copy(positivePrompt = s) } },
                label = { Text("Инструкция AI (Положительный)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { posFocused = it.isFocused }
                    .animateContentSize(),
                minLines = if (posFocused) 5 else 2,
                maxLines = if (posFocused) 15 else 3
            )

            var negFocused by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = settings.negativePrompt,
                onValueChange = { s -> viewModel.updateSetting { it.copy(negativePrompt = s) } },
                label = { Text("Чего НЕ ДОЛЖНО быть (Негативный)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { negFocused = it.isFocused }
                    .animateContentSize(),
                minLines = if (negFocused) 3 else 1,
                maxLines = if (negFocused) 8 else 1,
                placeholder = { Text("Оставьте пустым для отключения") }
            )

            if (settings.negativePrompt.isNotBlank()) {
                OutlinedTextField(
                    value = settings.negativeThreshold.toString(),
                    onValueChange = { s ->
                        s.toIntOrNull()?.let { num ->
                            viewModel.updateSetting {
                                it.copy(
                                    negativeThreshold = num.coerceIn(
                                        0,
                                        100
                                    )
                                )
                            }
                        }
                    },
                    label = { Text("Порог отсева (0 - 100)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Button(
                onClick = { navController.navigate("vacancies") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Посмотреть обработанные вакансии")
            }

            Button(
                onClick = { onToggleBot(!isRunning) },
                enabled = settings.selectedModelFileName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (isRunning) "ОСТАНОВИТЬ БОТ" else "ЗАПУСТИТЬ БОТ")
            }

            if (settings.selectedModelFileName.isBlank()) {
                Text(
                    "⚠️ Для запуска бота скачайте и выберите модель",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
