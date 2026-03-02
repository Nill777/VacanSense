package com.vacansense.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vacansense.domain.models.VacancyStatus
import com.vacansense.presenter.viewmodels.MainViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacancyListScreen(viewModel: MainViewModel, navController: NavController) {
    val vacancies by viewModel.currentVacancies.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("База Вакансий") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { pad ->
        if (vacancies.isEmpty()) {
            Box(Modifier
                .padding(pad)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Пока ничего не найдено по этому запросу.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vacancies) { vac ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = vac.title,
                                style = MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.Underline),
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.clickable {
                                    try {
                                        uriHandler.openUri(vac.url)
                                    } catch (e: Exception) {
                                    }
                                }
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(vac.employer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiary)
                            Text(vac.salary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiary)
                            Spacer(Modifier.height(4.dp))

                            val statusColor = when (vac.status) {
                                VacancyStatus.DONE -> Color(0xFF4CAF50)
                                VacancyStatus.PROCESSING -> Color(0xFFFF9800)
                                VacancyStatus.REJECTED -> Color.Red
                                VacancyStatus.ERROR -> Color.Red
                                VacancyStatus.NEW -> Color.Gray
                            }
                            Text(
                                "Статус: ${vac.status.name}",
                                color = statusColor,
                                style = MaterialTheme.typography.labelMedium
                            )

                            if (vac.status == VacancyStatus.REJECTED || vac.status == VacancyStatus.DONE) {
                                Text(
                                    text = vac.summary,
                                    color = if (vac.status == VacancyStatus.REJECTED) Color.Red else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatHhDate(vac.publishedAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )

                                Row {
                                    IconButton(onClick = { viewModel.resetVacancy(vac.id) }) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            "Заново",
                                            tint = MaterialTheme.colorScheme.onSecondary
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteVacancy(vac.id) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            "Удалить",
                                            tint = Color(0xFFE91E1E)
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
}

fun formatHhDate(isoDate: String): String {
    return try {
        val parsed = ZonedDateTime.parse(isoDate)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
        parsed.format(formatter)
    } catch (e: Exception) {
        isoDate
    }
}
