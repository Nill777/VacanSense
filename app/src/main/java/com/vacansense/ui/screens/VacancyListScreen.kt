package com.vacansense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vacansense.presenter.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacancyListScreen(viewModel: MainViewModel, navController: NavController) {
    val vacancies by viewModel.currentVacancies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("База Вакансий") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { pad ->
        if (vacancies.isEmpty()) {
            Box(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { Text("Пока ничего не найдено по этому запросу.") }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vacancies) { vac ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(vac.title, style = MaterialTheme.typography.titleMedium)
                            Text(vac.employer, style = MaterialTheme.typography.bodyMedium)

                            val statusColor = when (vac.status) {
                                "DONE" -> Color(0xFF4CAF50)
                                "PROCESSING" -> Color(0xFFFF9800)
                                "REJECTED" -> Color.Red
                                "ERROR" -> Color.Red
                                else -> Color.Gray
                            }
                            Text(
                                "Статус: ${vac.status}",
                                color = statusColor,
                                style = MaterialTheme.typography.labelMedium
                            )
                            if (vac.status == "REJECTED") {
                                Text(
                                    vac.summary,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { viewModel.resetVacancy(vac.id) }) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        "Заново",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteVacancy(vac.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
