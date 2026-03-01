package com.vacansense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun FiltersScreen(viewModel: MainViewModel, navController: NavController) {
    val filters by viewModel.hhFilters.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HH Фильтры") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Region
            OutlinedTextField(
                value = filters.area,
                onValueChange = { s -> viewModel.updateFilters { it.copy(area = s) } },
                label = { Text("ID Региона (Москва=1, Россия=113)") },
                modifier = Modifier.fillMaxWidth()
            )
            // Salary
            OutlinedTextField(
                value = filters.salary.toString(),
                onValueChange = { s ->
                    viewModel.updateFilters {
                        it.copy(
                            salary = s.toIntOrNull() ?: 0
                        )
                    }
                },
                label = { Text("Зарплата от (руб)") },
                modifier = Modifier.fillMaxWidth()
            )
            // Period
            OutlinedTextField(
                value = filters.period.toString(),
                onValueChange = { s ->
                    viewModel.updateFilters {
                        it.copy(
                            period = s.toIntOrNull() ?: 30
                        )
                    }
                },
                label = { Text("Период (дней)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Пример простых текстовых для Enum значений HH,
            // В продакшне это должны быть ExposedDropdownMenu. Здесь оставляю простыми полями для экономии строк.
            OutlinedTextField(
                value = filters.experience,
                onValueChange = { s -> viewModel.updateFilters { it.copy(experience = s) } },
                label = { Text("Опыт (напр. noExperience, between1And3)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("between1And3") }
            )
            OutlinedTextField(
                value = filters.employment,
                onValueChange = { s -> viewModel.updateFilters { it.copy(employment = s) } },
                label = { Text("Тип занятости (full, part, project)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = filters.schedule,
                onValueChange = { s -> viewModel.updateFilters { it.copy(schedule = s) } },
                label = { Text("График (remote, fullDay, flexible)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Оставьте поля (кроме региона) пустыми/нулевыми, чтобы не фильтровать.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
