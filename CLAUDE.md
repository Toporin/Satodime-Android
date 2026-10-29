# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Satodime-Android is an Android application for interacting with Satodime NFC bearer chip cards to seal, unseal, and transfer cryptocurrency assets. The app communicates with physical Satodime cards via NFC to manage crypto vaults stored on the secure chip.

## Build Commands

- **Build APK**: `./gradlew clean build`
- **Build debug APK**: `./gradlew assembleDebug`
- **Build release APK**: `./gradlew assembleRelease`
- **Install debug on device**: `./gradlew installDebug`
- **Run tests**: `./gradlew test`
- **Run instrumented tests**: `./gradlew connectedAndroidTest`
- **Clean build**: `./gradlew clean`

## API Keys Configuration

The app requires API keys for blockchain explorers and services. These must be defined in `local.properties`:

```properties
API_KEY_ETHERSCAN=your_etherscan_key
API_KEY_ETHPLORER=your_ethplorer_key
API_KEY_BSCSCAN=your_bscscan_key
API_KEY_RARIBLE=your_rarible_key
API_KEY_COVALENT=your_covalent_key
API_KEY_PAYBIS_ID=your_paybis_id
API_KEY_PAYBIS_HMAC=your_paybis_hmac
```

These keys are loaded via BuildConfig at compile time and used by the Javacryptotools library for fetching balances, NFT metadata, and exchange rates.

## Architecture

### Core Pattern: MVVM with Jetpack Compose

- **UI Layer**: Jetpack Compose views in `ui/` package (53 UI files)
- **ViewModel**: `SharedViewModel` is the single source of truth for app state
- **Services**: `NFCCardService` singleton manages NFC communication
- **Navigation**: Single-activity architecture with Compose Navigation

### Key Components

**NFCCardService** (`services/NFCCardService.kt`)
- Singleton managing all NFC card operations
- Uses `SatochipCommandSet` for hardware communication
- Implements action-based pattern: set `actionType`, `actionIndex`, then `scanCardForAction()`
- Actions: DoNothing, GetStatus, SealSlot, UnsealSlot, ResetSlot, TransferSlot
- Maintains card state: slots, ownership status, authenticity status
- Must call `disableScanForAction()` to release NFC reader

**SharedViewModel** (`viewmodels/SharedViewModel.kt`)
- Single AndroidViewModel for entire app lifecycle
- Observes `NFCCardService` LiveData and syncs to Compose state
- Manages vault data fetching (balances, assets, prices) via coroutines
- Handles dialogs (NFC, ownership, authenticity)
- Initiates NFC actions by calling NFCCardService methods

**CardSlot** and **CardVault** (`data/`)
- `CardSlot`: Raw data from NFC card (key SLIP-44, state, pubkey)
- `CardVault`: Business logic wrapper around CardSlot
  - Initializes `BaseCoin` from javacryptotools for blockchain operations
  - Fetches balances, token lists, NFT lists, exchange rates
  - Each vault is tied to a specific blockchain via SLIP-44 standard

**Navigation** (`Navigation.kt`)
- Uses Jetpack Compose Navigation with `NavHost`
- Locks screen orientation to portrait
- Handles onboarding flow (FirstWelcome/SecondWelcome/ThirdWelcome)
- Main screen: `VaultsView` shows card slots/vaults

### External Library Dependencies

The app depends on two JAR libraries placed in `app/libs/`:
- `satochip-android-0.0.2.jar`: NFC communication wrapper for Android
- `satochip-lib-0.2.5.jar`: Core Satochip protocol implementation
- `javacryptotools-0.4.0.jar`: Blockchain explorers and crypto utilities

These libraries are not in Maven/Gradle repos and must be built separately from:
- https://github.com/Toporin/Satochip-Java
- https://github.com/Toporin/Javacryptotools

### Data Flow for NFC Operations

1. User triggers action in UI (e.g., unseal vault)
2. SharedViewModel sets action parameters in NFCCardService
3. SharedViewModel calls `NFCCardService.scanCardForAction(activity)`
4. User taps card, NFC reader detects and connects
5. `SatodimeCardListenerForAction` executes action on card
6. Result posted to `NFCCardService.resultCodeLive`
7. SharedViewModel observes result and updates UI state
8. UI automatically recomposes based on state change

### State Management Patterns

- Card connection state flows from NFCCardService → SharedViewModel → UI
- Card data (slots) stored in NFCCardService, mirrored in SharedViewModel
- Vault/balance data computed asynchronously in SharedViewModel coroutines
- Use `MutableStateFlow` for Compose-observed state, `MutableLiveData` for service-level state

## Translations

The app supports multiple languages via Android resources:
- `values/strings.xml`: English (default)
- `values-es/strings.xml`: Spanish (Colombia)
- `values-fr/strings.xml`: French
- `values-de/strings.xml`: German

When adding new UI text, always add entries to all four files. Use resource IDs (`R.string.*`) in code, never hardcoded strings.

## Testing

- Unit tests: `app/src/test/`
- Instrumented tests: `app/src/androidTest/`

The app uses Firebase Crashlytics for production error tracking.

## Preferences

User preferences stored via SharedPreferences with key "satodime":
- `SatodimePreferences.FIRST_TIME_LAUNCH`: Onboarding completion flag
- `SatodimePreferences.VERBOSE_MODE`: Debug logging toggle (visible in Settings)
- `SatodimePreferences.SELECTED_CURRENCY`: Fiat currency for value display (default: USD)

## Important Notes

- App requires NFC hardware; check `NfcAdapter.getDefaultAdapter()` availability
- Screen orientation locked to portrait throughout app
- Some blockchain operations can be slow (balance fetching); handle in coroutines
- SLIP-44 standard determines blockchain type from 4-byte key
- Card slots have states: UNINITIALIZED, SEALED, UNSEALED
- Authenticity and ownership can be verified via card certificates