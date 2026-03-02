<p align="center">
    <img src="./img/icon.png" width="180"/>
</p>

# VacanSense AI

<p align="center">
    <img src="https://img.shields.io/badge/Kotlin-2.3.0-blue.svg?logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/Jetpack%20Compose-Modern%20UI-green.svg?logo=android" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Clean%20Architecture-Solid-orange.svg" alt="Clean Architecture">
    <img src="https://img.shields.io/badge/Local%20LLM-LlamaCpp-red.svg" alt="Local LLM">
    <img src="https://img.shields.io/badge/Room-Database-lightgrey.svg" alt="Room">
</p>

**VacanSense AI** — это умный Android-ассистент для автоматизированного поиска и анализа вакансий. Приложение самостоятельно ищет новые вакансии на HH.ru по заданным фильтрам, пропускает их через **локальную нейросеть (LLM)** прямо на вашем устройстве, делает краткую выжимку и отправляет идеальные совпадения вам в Telegram.


## Ключевые возможности

* **Автоматический мониторинг HH.ru:** Настройте параметры поиска (зарплата, регион, график, опыт работы), и бот будет фоном проверять наличие новых вакансий.
* **On-device AI (Локальная нейросеть):** Вся обработка текста происходит прямо на телефоне с использованием GGUF-моделей (Qwen, Llama, Gemma). Ваши данные **НЕ ОТПРАВЛЯЮТСЯ** на сторонние AI-серверы! Всё локально.
* **Гибкий промптинг:**
  - *Positive Prompt:* Укажите, как именно нейросеть должна сделать выжимку (например, "только стек технологий и обязанности").
  - *Negative Prompt:* Задайте критерии отсева (например, "отсеивать вакансии с легаси-кодом"), и нейросеть сама отбракует нерелевантные варианты по заданной шкале (0-100%).
* **Интеграция с Telegram:** Подключите своего бота, и VacanSense AI будет присылать готовую, отфильтрованную и отформатированную информацию прямо вам в личные сообщения.
* **Фоновая работа:** Работает как Foreground Service, методично собирая и анализируя данные, пока вы занимаетесь своими делами.

## Скриншоты

| Главный экран (Настройки) | Экран фильтров (HH) |
| :---: | :---: |
| <img src="./img/Screenshot_20260302-220030_VacanSense.jpg" width="200"/> | <img src="./img/Screenshot_20260302-215928_VacanSense.jpg" width="200"/> |

| Загрузка LLM моделей | База собранных вакансий |
| :---: | :---: |
| <img src="./img/Screenshot_20260302-220046_VacanSense.jpg" width="200"/> | <img src="./img/Screenshot_20260302-222649_VacanSense.jpg" width="200"/> |
## Архитектура и стек технологий

Проект разработан с соблюдением принципов **Clean Architecture**. Это обеспечивает высокую тестируемость и легкость масштабирования.

### Стек:
* **Язык:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Асинхронность:** Coroutines & Flow
* **Сеть:** Retrofit2 + Gson (HH API & Telegram API)
* **База данных:** Room
* **Локальное хранилище:** DataStore (Preferences)
* **AI Engine:** LlamaCpp (C++ wrapper для Android) для запуска GGUF-моделей.
* **Background Tasks:** Foreground Service + PowerManager WakeLocks + DownloadManager

### Настройка приложения:
1. Создайте Telegram бота через `@BotFather` и скопируйте **Token**.
2. Узнайте свой **Chat ID/User ID** (например, через бота `@userinfobot`).
3. В приложении введите Token и ID.
4. В разделе моделей выберите и скачайте подходящую LLM.
5. Настройте фильтры HH.ru и нажмите **ЗАПУСТИТЬ БОТ**.
