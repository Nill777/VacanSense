package com.vacansense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vacansense.presenter.viewmodels.MainViewModel

@Composable
fun BotScreen(viewModel: MainViewModel, onToggleBot: (Boolean) -> Unit) {
    val query by viewModel.query.collectAsState()
    val token by viewModel.tgToken.collectAsState()
    val chatId by viewModel.tgChatId.collectAsState()
    val modelPath by viewModel.modelPath.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("VacanSense AI Bot", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.query.value = it },
            label = { Text("Запрос (HH)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = token,
            onValueChange = { viewModel.tgToken.value = it },
            label = { Text("Telegram Bot Token") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = chatId,
            onValueChange = { viewModel.tgChatId.value = it },
            label = { Text("Telegram Chat ID") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = modelPath,
            onValueChange = { viewModel.modelPath.value = it },
            label = { Text("Путь к модели (.gguf)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onToggleBot(!isRunning) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        ) {
            Text(if (isRunning) "Остановить бота" else "Запустить бота")
        }
    }
}
