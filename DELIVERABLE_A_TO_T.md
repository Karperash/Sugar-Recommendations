# A. Краткий обзор системы

DiaGuide CGM — это Android-прототип рекомендательной системы для анализа данных непрерывного мониторинга глюкозы.
Приложение принимает CGM-подобные записи, визуализирует динамику глюкозы, вычисляет дневную сводку, выявляет отклонения и объяснимые паттерны, после чего формирует осторожные информационные рекомендации.

# B. Список функциональных требований

- онбординг профиля и медицинских порогов
- сохранение пользовательских настроек
- ручной ввод показателя глюкозы
- импорт CSV
- импорт JSON
- загрузка демо-сценариев
- локальное хранение CGM-записей
- локальное хранение рекомендаций
- локальное хранение журнала событий
- визуализация истории глюкозы
- расчет дневной сводки
- детекция низких/высоких значений
- детекция быстрого роста/снижения
- детекция длительного выхода из диапазона
- детекция повторяющихся отклонений
- детекция утренних повышений
- детекция повторяющихся скачков после еды
- детекция ночных снижений
- вывод рекомендаций по уровням serious/informational
- экран дисклеймера

# C. Список нефункциональных требований

- Kotlin
- Android Studio project
- MVVM
- Clean Architecture
- Repository pattern
- Hilt
- Room
- DataStore
- Coroutines + Flow
- Navigation Compose
- Compose-based UI
- production-style code
- explainable rule-based logic
- высокий уровень читаемости интерфейса
- медицинский дисклеймер и отказ от диагностических/терапевтических выводов

# D. Объяснение архитектуры приложения

Проект реализован как один Gradle-модуль `app`, внутри которого соблюдается логическое разделение на слои:
- `presentation` — экраны, composable-функции, ViewModel;
- `domain` — модели, контракты репозиториев, движок анализа, движок рекомендаций, use cases;
- `data` — Room, DataStore, импорт CSV/JSON, mock-данные и реализации репозиториев;
- `core` — повторно используемые UI-компоненты и утилиты;
- `di` — Hilt-модули.

# E. Полная структура проекта Android Studio

См. дерево каталогов внутри `app/src/main/java/com/gk/diaguide/`.
Ключевые узлы:
- `presentation/splash`
- `presentation/onboarding`
- `presentation/dashboard`
- `presentation/chart`
- `presentation/history`
- `presentation/recommendations`
- `presentation/events`
- `presentation/manual`
- `presentation/imports`
- `presentation/settings`
- `presentation/about`
- `domain/model`
- `domain/repository`
- `domain/engine`
- `domain/usecase`
- `data/local`
- `data/repository`
- `data/imports`
- `data/mock`
- `navigation`
- `di`

# F. Конфигурация Gradle

См.:
- `gradle/libs.versions.toml`
- `build.gradle.kts`
- `app/build.gradle.kts`

# G. Модели данных

См.:
- `domain/model/CgmRecord.kt`
- `domain/model/UserSettings.kt`
- `domain/model/DailySummary.kt`
- `domain/model/DetectedPattern.kt`
- `domain/model/Recommendation.kt`
- `domain/model/AppEvent.kt`

# H. Интерфейсы и реализации репозиториев

См.:
- `domain/repository/CgmRepository.kt`
- `domain/repository/SettingsRepository.kt`
- `data/repository/CgmRepositoryImpl.kt`
- `data/repository/SettingsRepositoryImpl.kt`

# I. Domain use cases

См.:
- `domain/usecase/GlucoseUseCases.kt`

# J. ViewModels

См.:
- `presentation/splash/SplashViewModel.kt`
- `presentation/onboarding/OnboardingViewModel.kt`
- `presentation/dashboard/DashboardViewModel.kt`
- `presentation/chart/ChartViewModel.kt`
- `presentation/history/HistoryViewModel.kt`
- `presentation/recommendations/RecommendationsViewModel.kt`
- `presentation/events/EventLogViewModel.kt`
- `presentation/manual/ManualEntryViewModel.kt`
- `presentation/imports/ImportViewModel.kt`
- `presentation/settings/SettingsViewModel.kt`

# K. Экраны на Jetpack Compose

См. папки `presentation/*/*Screen.kt`.

# L. Настройка навигации

См.:
- `navigation/AppDestination.kt`
- `navigation/AppNavHost.kt`

# M. Настройка Room database

См.:
- `data/local/AppDatabase.kt`
- `data/local/Entities.kt`
- `data/local/AppDao.kt`
- `data/local/Converters.kt`

# N. Парсеры импорта CSV и JSON

См.:
- `data/imports/CgmCsvParser.kt`
- `data/imports/CgmJsonParser.kt`

# O. Движок анализа CGM

См.:
- `domain/engine/CgmAnalysisEngine.kt`

# P. Движок рекомендаций

См.:
- `domain/engine/RecommendationEngine.kt`

# Q. Пример mock-данных

См.:
- `data/mock/SyntheticCgmDataFactory.kt`
- `app/src/main/assets/sample_imports/`

# R. Unit-тесты

См.:
- `app/src/test/java/com/gk/diaguide/domain/engine/CgmAnalysisEngineTest.kt`
- `app/src/test/java/com/gk/diaguide/domain/engine/RecommendationEngineTest.kt`
- `app/src/test/java/com/gk/diaguide/data/imports/CgmParsersTest.kt`
- `app/src/test/java/com/gk/diaguide/presentation/dashboard/DashboardViewModelTest.kt`

# S. README с инструкцией по сборке и запуску

См. `README.md`.

# T. Краткое пояснение, как эту реализацию можно описать в 3 главе ВКР

Для главы 3 можно описать:
- обоснование выбора Android/Kotlin/Compose;
- проектирование слоистой архитектуры;
- реализацию локального хранилища и подсистемы импорта;
- реализацию алгоритма анализа CGM на основе набора правил;
- реализацию движка рекомендаций с градацией серьезности;
- реализацию интерфейса мобильного прототипа;
- тестирование на синтетических сценариях и формирование ограничений прототипа.
