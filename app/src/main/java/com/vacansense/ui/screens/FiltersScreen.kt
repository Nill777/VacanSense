package com.vacansense.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vacansense.presenter.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(viewModel: MainViewModel, navController: NavController) {
    val filters by viewModel.hhFilters.collectAsState()

    val cityMap = mapOf(
        "11" to "Барнаул",
        "14" to "Белгород",
        "15" to "Бийск",
        "16" to "Благовещенск",
        "17" to "Брянск",
        "20" to "Великий Новгород",
        "21" to "Владивосток",
        "22" to "Владимир",
        "24" to "Волгоград",
        "26" to "Воронеж",
        "3" to "Екатеринбург",
        "32" to "Иваново",
        "33" to "Ижевск",
        "34" to "Иркутск",
        "88" to "Казань",
        "35" to "Калининград",
        "36" to "Калуга",
        "38" to "Кемерово",
        "41" to "Комсомольск-на-Амуре",
        "43" to "Кострома",
        "54" to "Краснодар",
        "53" to "Красноярск",
        "145" to "Ленинградская область",
        "47" to "Липецк",
        "49" to "Магадан",
        "1" to "Москва",
        "2019" to "Московская область",
        "66" to "Нижний Новгород",
        "4" to "Новосибирск",
        "1202" to "Новосибирская область",
        "68" to "Омск",
        "71" to "Пенза",
        "72" to "Пермь",
        "73" to "Петрозаводск",
        "75" to "Псков",
        "76" to "Ростов-на-Дону",
        "77" to "Рыбинск",
        "78" to "Самара",
        "2" to "Санкт-Петербург",
        "79" to "Саратов",
        "1261" to "Свердловская область",
        "84" to "Смоленск",
        "85" to "Сочи",
        "87" to "Ставрополь",
        "1620" to "Татарстан (Республика)",
        "90" to "Тверь",
        "92" to "Томск",
        "93" to "Тула",
        "95" to "Тюмень",
        "96" to "Улан-Удэ",
        "97" to "Ульяновск",
        "99" to "Уфа",
        "102" to "Хабаровск",
        "104" to "Челябинск",
        "105" to "Череповец",
        "112" to "Ярославль"
    ).toList().sortedBy { it.second }.toMap()

    val regionOptions = mapOf("113" to "Вся Россия") + cityMap

    val expOptions = mapOf(
        "" to "Не важно",
        "noExperience" to "Нет опыта",
        "between1And3" to "От 1 года до 3 лет",
        "between3And6" to "От 3 до 6 лет",
        "moreThan6" to "Более 6 лет"
    )
    val empOptions = mapOf(
        "" to "Не важно",
        "full" to "Полная занятость",
        "part" to "Частичная занятость",
        "project" to "Проектная работа",
        "volunteer" to "Волонтерство",
        "probation" to "Стажировка"
    )
    val schedOptions = mapOf(
        "" to "Не важно",
        "fullDay" to "Полный день",
        "shift" to "Сменный график",
        "flexible" to "Гибкий график",
        "remote" to "Удаленная работа",
        "flyInFlyOut" to "Вахтовый метод"
    )
    var localSalary by remember { mutableStateOf<String?>(null) }
    var localPeriod by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтры поиска (HH)") },
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
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DropdownFilter(
                "Регион поиска",
                regionOptions,
                filters.area
            ) { s -> viewModel.updateFilters { it.copy(area = s) } }
            DropdownFilter(
                "Опыт работы",
                expOptions,
                filters.experience
            ) { s -> viewModel.updateFilters { it.copy(experience = s) } }
            DropdownFilter(
                "Тип занятости",
                empOptions,
                filters.employment
            ) { s -> viewModel.updateFilters { it.copy(employment = s) } }
            DropdownFilter(
                "График работы",
                schedOptions,
                filters.schedule
            ) { s -> viewModel.updateFilters { it.copy(schedule = s) } }

            OutlinedTextField(
                value = localSalary ?: if (filters.salary == 0) "" else filters.salary.toString(),
                onValueChange = { s ->
                    localSalary = s
                    viewModel.updateFilters {
                        it.copy(
                            salary = s.toIntOrNull() ?: 0
                        )
                    }
                },
                label = { Text("Зарплата от (руб)") },
                placeholder = { Text("Не важно") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )
            OutlinedTextField(
                value = localPeriod ?: filters.period.toString(),
                onValueChange = { s ->
                    localPeriod = s
                    viewModel.updateFilters {
                        it.copy(
                            period = s.toIntOrNull() ?: 30
                        )
                    }
                },
                label = { Text("За какой период (в днях)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                    focusedLabelColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownFilter(
    label: String,
    options: Map<String, String>,
    selectedKey: String,
    onSelectionChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = options[selectedKey] ?: options[""] ?: options["113"] ?: "Выбрано"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
                focusedLabelColor = MaterialTheme.colorScheme.onSecondary
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, value) ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        onSelectionChanged(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
