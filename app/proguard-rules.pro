# Сохраняем подписи для Retrofit и Gson
-keepattributes Signature, *Annotation*

# Сохраняем классы для работы с сетью и парсинга JSON
-keep class com.vacansense.data.network.** { *; }
-keep interface com.vacansense.data.network.** { *; }

# Сохраняем модели данных
-keep class com.vacansense.domain.models.** { *; }

# Сохраняем БД Room
-keep class com.vacansense.data.db.** { *; }

# Защищаем C++ библиотеку LlamaCpp (чтобы бот не падал при анализе)
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.arm.aichat.** { *; }
