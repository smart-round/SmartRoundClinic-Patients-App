# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# SmartRound Clinic — Patient App

Kotlin Multiplatform project targeting **Android** and **iOS** using Compose Multiplatform. Mirrors the architecture of the doctor app at `/Users/pasaka/Developer/SmartRoundClinic-doctor` — consult it for reference implementations of full feature layers.

## Commands

```bash
# Android
./gradlew :androidApp:assembleDebug         # Build debug APK
./gradlew :androidApp:assembleRelease       # Build release APK

# Tests
./gradlew :shared:testDebugUnitTest         # Run Android unit tests
./gradlew :shared:iosSimulatorArm64Test     # Run iOS tests

# KSP (re-run after changing Room entities/DAOs)
./gradlew :shared:kspDebugKotlinAndroid
./gradlew :shared:kspKotlinIosArm64
./gradlew :shared:kspKotlinIosSimulatorArm64

# iOS — open in Xcode after building the Kotlin framework
open iosApp/iosApp.xcodeproj
```

## Module structure

```
shared/                    — KMP module (equivalent to doctor's composeApp/)
  src/
    commonMain/            — shared business logic and platform-agnostic setup
    androidMain/           — Android actuals; Context resolved via Koin androidContext()
    iosMain/               — iOS actuals using NSFileManager / Darwin engine
  schemas/                 — Room auto-generated migration JSON (commit these)
androidApp/                — Android application wrapper (no business logic here)
iosApp/                    — Xcode project; imports the Kotlin framework as "Shared"
```

## Package root

`ke.co.smartroundclinic.patient`

Android app subpackage: `ke.co.smartroundclinic.patient.android`

## Tech stack & versions

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.3.21 | Language |
| Compose Multiplatform | 1.10.3 | UI |
| androidx.lifecycle | 2.10.0 | ViewModel (`org.jetbrains.androidx.lifecycle`) |
| Koin | 4.1.0 | Dependency injection |
| Navigation3 (`org.jetbrains.androidx.navigation3`) | 1.1.0 | Navigation — JetBrains KMP port, NOT `androidx.navigation3` |
| Room | 2.8.4 | Local database — KMP via `BundledSQLiteDriver` |
| SQLite Bundled | 2.6.2 | Cross-platform SQLite driver for Room |
| Ktor | 3.4.3 | HTTP client |
| kotlinx.serialization | 1.11.0 | JSON |
| kotlinx.coroutines | 1.10.2 | Async |
| Coil 3 | 3.4.0 | Image loading — uses `coil-network-ktor3` (NOT `coil-network-okhttp`) |
| DataStore (`datastore-preferences-core`) | 1.2.1 | Key-value preferences — KMP `-core` variant |
| KVault | 1.12.0 | Secure storage (Android Keystore / iOS Keychain) |
| FileKit | 0.14.0 | Cross-platform file/photo picking |
| Napier | 2.7.1 | Multiplatform logging |
| KSP | 2.3.7 | Annotation processing for Room |

## App entry points

- **Android:** `SmartRoundApp` (`Application` class) initializes Koin with `androidContext()`, then `MainActivity` renders `App()`
- **iOS:** `iOSApp.swift` (`@main`) calls `MainViewControllerKt.doInitKoin()` on init, then `ContentView` wraps `MainViewControllerKt.MainViewController()`
- `App()` is the root composable — currently a placeholder `Box`. Add a `NavigationRoot` and theme here when building the first screen.

## Clean architecture — feature layers

New features follow this three-layer structure inside `commonMain`:

```
data/
  remote/dto/request/     — @Serializable request DTOs
  remote/dto/response/    — @Serializable response DTOs
  repository/             — Repository implementations (Ktor or Room, never both)
domain/
  model/                  — Pure Kotlin domain models (no framework annotations)
  repository/             — Repository interfaces typed to domain models + Resource<T>
  usecase/                — Use case classes, one responsibility each
presentation/
  navigation/             — NavDisplay + back stack setup
  <feature>/              — ViewModel + Composable screens per feature
```

### Data flow

```
Network DTO  ──► toDomain()  ──► Domain Model  ──► ViewModel / UI
Room Entity  ──► toDomain()  ──► Domain Model  ──► ViewModel / UI

ViewModel    ──► Domain Model  ──► toRequest()  ──► Request DTO  (send)
ViewModel    ──► Domain Model  ──► toEntity()   ──► Room Entity  (persist)
```

**Layer rules:**
- `data/remote/dto/` — DTOs stay in the data layer
- `core/database/entity/` — Room entities stay in the data layer
- `domain/model/` — pure Kotlin; the only type that ViewModels and UI touch
- `domain/repository/` — interfaces only; implementations live in `data/repository/`
- `domain/usecase/` — orchestrate repositories and **own all mapping**

### ⚠️ Non-negotiable: Repository return types

**Remote repositories return raw response DTOs. Local repositories return raw Room entities. No exceptions.**

```kotlin
// ✅ CORRECT
interface FooRepository      { suspend fun getFoos(): Resource<GetFoosRes>    }
interface FooLocalRepository { suspend fun getFoos(): List<FooEntity>         }

// ❌ WRONG — repositories must never return domain models
interface FooRepository      { suspend fun getFoos(): Resource<List<Foo>>     }
interface FooLocalRepository { suspend fun getFoos(): List<Foo>               }
```

**All `toDomain()` / `toEntity()` calls happen inside use cases, not repositories.**

Extension functions live on the source type:
- `fun FooResponse.toDomain()` — in the DTO file
- `fun FooEntity.toDomain()` — in the entity file
- `fun Foo.toEntity()` — in the entity file

Use case cache-first pattern:
```kotlin
class GetFoosUseCase(private val remote: FooRepository, private val local: FooLocalRepository) {
    suspend operator fun invoke(): Resource<List<Foo>> {
        val cached = local.getFoos()
        if (cached.isNotEmpty()) return Resource.Success(cached.map { it.toDomain() })
        return when (val result = remote.getFoos()) {
            is Resource.Success -> {
                val entities = result.data?.items?.map { it.toDomain().toEntity() } ?: emptyList()
                local.saveFoos(entities)
                Resource.Success(entities.map { it.toDomain() })
            }
            is Resource.Error -> Resource.Error(result.message ?: "Error")
            is Resource.Loading -> Resource.Loading()
        }
    }
}
```

### Repository isolation rule

**A repository must be single-source — either remote (Ktor) or local (Room), never both.**

Cache-first orchestration belongs in a **use case**.

```
domain/repository/FooRepository.kt         — remote interface → Resource<GetFoosRes>
domain/repository/FooLocalRepository.kt    — local interface → List<FooEntity>
data/repository/FooRepositoryImpl.kt       — Ktor impl, returns raw DTO
data/repository/FooLocalRepositoryImpl.kt  — Room DAO impl, returns raw entities
domain/usecase/foo/GetFooUseCase.kt        — cache-first + all mapping
```

Register both bindings in `RepositoryModule` and use cases in `UseCaseModule`.

### Resource wrapper

All async results use `Resource<T>` (`common/Resource.kt`):

```kotlin
sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    class Success<T>(data: T?) : Resource<T>()
    class Error<T>(message: String) : Resource<T>()
}
```

ViewModels expose `StateFlow<Resource<T>>`; Composables collect it.

## Core infrastructure (`core/`)

### Database — Room KMP

- `AppDatabase.kt` — `@Database` class + `AppDatabaseConstructor` expect object. Room KSP generates the actual per platform — **do not add a manual actual in any `iosXxxMain`**.
- `DatabaseFactory.kt` — `expect fun getDatabaseBuilder()` + `createDatabase()`
- Android actual: Android `Context` via Koin + `BundledSQLiteDriver`, file: `patient_smartround.db`
- iOS actual: `NSFileManager` document directory + `BundledSQLiteDriver`, same filename
- `fallbackToDestructiveMigration(true)` — replace with proper `Migration` before shipping
- KSP targets declared in `dependencies {}`: `kspAndroid`, `kspIosArm64`, `kspIosSimulatorArm64`

### HTTP Client — Ktor

- `core/network/HttpClientFactory.kt` — `buildHttpClient(engine)` + `expect fun createHttpClient(tokenProvider)`
- Android: `OkHttp` engine; iOS: `Darwin` engine
- Config: `ContentNegotiation` + JSON (`ignoreUnknownKeys`, `isLenient`, `explicitNulls = false`), Napier logging, retry (3 retries, exponential backoff 15 s max)
- Base URL: `https://api.smartroundclinic.co.ke/` — defined in `common/Constants.kt`
- Auth: reads `KEY_ACCESS_TOKEN` from `KVault` and injects `Authorization: Bearer <token>` header

### DataStore

- `core/datastore/DataStoreFactory.kt` — `expect fun createDataStore()`; file: `app_prefs.preferences_pb`

### Secure Storage — KVault

- `core/storage/SecureStorageFactory.kt` — `expect fun createKVault()`
- Android: EncryptedSharedPreferences; iOS: Keychain service `ke.co.smartroundclinic.patient`

### Global Snackbar

- `core/snackbar/SnackbarController.kt` — Koin `single {}` with a `SharedFlow<String>`
- Call `snackbarController.show("message")` from any ViewModel for transient error display
- Collected in `App()` via `LaunchedEffect` and drives a `SnackbarHostState`

## Koin modules

| Module | File | Contents |
|---|---|---|
| `coreModule` | `koin/CoreModule.kt` | `AppDatabase`, `HttpClient`, `DataStore`, `KVault`, `SnackbarController` |
| `repositoryModule` | `koin/RepositoryModule.kt` | Patient repository bindings (add here as features are built) |
| `useCaseModule` | `koin/UseCaseModule.kt` | Patient use cases + ViewModels (add here as features are built) |

All three are loaded by `initKoin()` in both `SmartRoundApp.onCreate()` (Android) and `doInitKoin()` (iOS).

## Navigation (when building screens)

Use Navigation3 with a `NavDisplay` + `retain {}` back stack (not `rememberSaveable`):

```kotlin
val backStack = retain { mutableStateListOf<NavKey>(InitialDestination) }
NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider { entry<MyDestination> { MyScreen() } }
)
```

**Never put `ByteArray` in a `@Serializable` NavKey data class** — it causes `TransactionTooLargeException`. Use an in-memory ViewModel to hold binary data and keep NavKey classes to primitive/String fields only.

## Adding a Room entity

1. Create `@Entity` data class in `core/database/entity/`
2. Add to `@Database(entities = [...])` in `AppDatabase.kt`
3. Create `@Dao` interface in `core/database/dao/`
4. Add `abstract val yourDao: YourDao` to `AppDatabase`
5. Bump `version` in `@Database` (keep `fallbackToDestructiveMigration` pre-production)
6. Re-run KSP: `./gradlew :shared:kspDebugKotlinAndroid`

## Critical KMP rules

1. **Never put `room-compiler` in `implementation()`** — KSP processor only, via `add("ksp<Target>", ...)`.
2. **Use `org.jetbrains.androidx.*` for KMP ports** — `androidx.navigation3` and non-core `androidx.datastore` are Android-only.
3. **DataStore KMP uses `-core` artifacts** — `datastore-preferences-core`, NOT `datastore-preferences`.
4. **Coil networking uses `coil-network-ktor3`** — `coil-network-okhttp` has no iOS target.
5. **`Dispatchers.IO`** — import from `kotlinx.coroutines.IO` in commonMain (not `kotlinx.coroutines.Dispatchers`).
6. **Room KSP generates `AppDatabaseConstructor` actual** — never add a manual actual in `iosXxxMain`; it causes a KSP `PROCESSING_ERROR`.
7. **iOS framework name is `Shared`** — the Xcode project imports `import Shared`, not `import ComposeApp`.
