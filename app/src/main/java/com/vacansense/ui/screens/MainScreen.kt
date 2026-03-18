package com.vacansense.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vacansense.domain.models.AiModel
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
    var expandedModels by remember { mutableStateOf(false) }
    var modelToDelete by remember { mutableStateOf<AiModel?>(null) }

    var localTgToken by remember { mutableStateOf<String?>(null) }
    var localTgChatId by remember { mutableStateOf<String?>(null) }
    var localQuery by remember { mutableStateOf<String?>(null) }
    var localPosPrompt by remember { mutableStateOf<String?>(null) }
    var localNegPrompt by remember { mutableStateOf<String?>(null) }
    var localThreshold by remember { mutableStateOf<String?>(null) }

    if (modelToDelete != null) {
        AlertDialog(
            onDismissRequest = { modelToDelete = null },
            text = { Text("Вы точно хотите удалить модель ${modelToDelete?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    modelToDelete?.let { viewModel.deleteModel(it.fileName) }
                    modelToDelete = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.onSecondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { modelToDelete = null }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VacanSense AI") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = localTgToken ?: settings.tgToken,
                onValueChange = { s ->
                    localTgToken = s
                    viewModel.updateSetting { it.copy(tgToken = s) }
                },
                label = { Text("Telegram Bot Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            OutlinedTextField(
                value = localTgChatId ?: settings.tgChatId,
                onValueChange = { s ->
                    localTgChatId = s
                    viewModel.updateSetting { it.copy(tgChatId = s) }
                },
                label = { Text("Telegram Chat ID / User ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            OutlinedTextField(
                value = localQuery ?: settings.query,
                onValueChange = { s ->
                    localQuery = s
                    viewModel.updateSetting { it.copy(query = s) }
                },
                label = { Text("Поисковый запрос (HH)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            Button(
                onClick = { navController.navigate("filters") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Настроить фильтры HH", style = MaterialTheme.typography.bodyLarge)
            }

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
                    singleLine = true,
                    label = { Text("AI Модель") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedModels) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                        focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
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
                                    when (model.state) {
                                        DownloadState.DOWNLOADED -> {
                                            if (settings.selectedModelFileName == model.fileName) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    "Готово",
                                                    tint = Color.Green
                                                )
                                                Spacer(Modifier.width(12.dp))
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Удалить",
                                                tint = Color(0xFFE91E1E),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clickable {
                                                        expandedModels = false
                                                        modelToDelete = model
                                                    }
                                            )
                                        }

                                        DownloadState.DOWNLOADING -> {
                                            Text(
                                                "${model.downloadProgress}%",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onSecondary
                                            )
                                        }

                                        else -> {
                                            Icon(
                                                Icons.Default.Add,
                                                "Скачать",
                                                tint = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.clickable {
                                                    viewModel.downloadModel(model)
                                                })
                                        }
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

            var posFocused by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = localPosPrompt ?: settings.positivePrompt,
                onValueChange = { s ->
                    localPosPrompt = s
                    viewModel.updateSetting { it.copy(positivePrompt = s) }
                },
                label = { Text("Позитивный промт (как обрабатывать)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { posFocused = it.isFocused }
                    .animateContentSize(),
                minLines = if (posFocused) 5 else 2,
                maxLines = if (posFocused) 15 else 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            var negFocused by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = localNegPrompt ?: settings.negativePrompt,
                onValueChange = { s ->
                    localNegPrompt = s
                    viewModel.updateSetting { it.copy(negativePrompt = s) }
                },
                label = { Text("Негативный промт (чего не должно быть)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { negFocused = it.isFocused }
                    .animateContentSize(),
                minLines = if (negFocused) 3 else 1,
                maxLines = if (negFocused) 8 else 1,
                placeholder = { Text("Оставьте пустым для отключения") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )

            if (settings.negativePrompt.isNotBlank()) {
                OutlinedTextField(
                    value = localThreshold ?: settings.negativeThreshold.toString(),
                    onValueChange = { s ->
                        localThreshold = s
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
                    label = { Text("Порог фильтрации [0-100], 0 - не содержит") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                        focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }

            Button(
                onClick = { navController.navigate("vacancies") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Список собранных вакансий", style = MaterialTheme.typography.bodyLarge)
            }

            Button(
                onClick = { onToggleBot(!isRunning) },
                enabled = settings.selectedModelFileName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFFE91E1E) else Color(0xFF3FC043)
                )
            ) {
                Text(
                    if (isRunning) "ОСТАНОВИТЬ БОТ" else "ЗАПУСТИТЬ БОТ",
                    style = MaterialTheme.typography.bodyLarge
                )
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
