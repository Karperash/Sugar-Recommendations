# DiaGuide CGM — Android prototype for a master's thesis

DiaGuide CGM is a Jetpack Compose Android prototype of a rule-based recommendation system for people living with diabetes.
The application is intended for research and educational use in a master's thesis. It ingests CGM-like data, visualizes glucose trends,
detects simple explainable patterns, and generates informational recommendations without performing diagnosis or insulin dose calculation.

## Key capabilities

- onboarding with editable glucose target range and alert thresholds
- manual glucose entry
- CGM import from CSV and JSON
- synthetic demo scenarios for testing
- local persistence with Room
- settings persistence with Preferences DataStore
- transparent rule-based CGM analysis
- recommendation history and event log
- chart visualization via Compose Canvas
- unit tests for parsers, analysis rules, recommendation rules, and a ViewModel

## Medical safety boundary

This prototype is **not** a certified medical device.
It does **not** diagnose, prescribe treatment, or calculate insulin doses.
Recommendations are cautious informational prompts only.
If the user experiences severe symptoms or dangerous glucose states, they must contact a clinician or emergency services.

## Tech stack

- Kotlin
- Android Studio / Gradle
- Jetpack Compose
- MVVM + Clean Architecture + Repository pattern
- Hilt
- Room
- DataStore
- Coroutines + Flow
- Navigation Compose

## Open in Android Studio

1. Install Android Studio with JDK 17.
2. Open the project folder.
3. Let Gradle sync the project.
4. Run the `app` configuration on an emulator or a device with Android 8.0+.

## Build assumptions

- minSdk = 26
- compileSdk = 36
- targetSdk = 36
- AGP = 8.10.1
- Kotlin = 2.2.20
- Compose BOM = 2026.03.00

## Demo data

The project includes:
- in-app synthetic scenarios:
  - stable day
  - repeated high glucose
  - repeated low glucose
  - sharp fluctuations
  - night episodes
- sample import files in `app/src/main/assets/sample_imports/`

## Main architecture

```
presentation/   UI, screens, navigation, view models
domain/         models, repository contracts, engines, use cases
data/           Room, DataStore, parsers, repositories, mock data
core/           reusable UI and helper utilities
di/             Hilt modules
```

## Important simplifications

- The app uses a single Gradle module (`app`) with strict package-based layering to keep the prototype easy to open and demonstrate.
- Mixed units are not automatically normalized across imported files. The prototype assumes the imported data is aligned with the selected unit.
- CSV parsing is lightweight and intended for well-formed research/demo input files.
- Future device integration is represented by interfaces and placeholders only.

## Suggested thesis chapter 3 description

In chapter 3, this implementation can be described as:
1. design of a mobile prototype architecture for CGM data processing;
2. implementation of the data layer with local persistence and import subsystem;
3. implementation of a rule-based analysis engine for trend and deviation detection;
4. implementation of a recommendation engine with explainable informational prompts;
5. implementation of a Compose-based UI for visualization, history, recommendations, and settings;
6. testing on synthetic scenarios that represent stable behavior, recurrent hyperglycemia, recurrent hypoglycemia, rapid oscillation, and night episodes.

## Deliverable map

See `DELIVERABLE_A_TO_T.md` for a structured A–T response matching the requested thesis-oriented output format.
