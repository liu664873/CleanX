# CleanX White-Label Architecture

CleanX keeps shared business behavior in `app/src/main` and package-specific UI in each flavor source set.

## Core Rules

- Shared business logic lives in `domain`, `data`, `feature`, `navigation`, `permissions`, `advertise`, and `config`.
- Flavor UI owns page structure, buttons, dialogs, animations, copy, and feature icons.
- `VariantProfile` owns package configuration only: enabled features, feature order, ads, legal links, notification shortcuts, and service keys.
- Pages do not call `AdvertiseSdk` directly; use `AdvertisePageMediator` or `AdManager`.
- Permissions are requested by page actions, not route-entry interception.
- Permission flow state lives in `main`; permission prompt UI is owned by each flavor through `VariantPermissionUi`.
- New route code should use `AppRoute`, `NavigationCommand`, and `AppNavigator`; `Screen` is only a compatibility layer for older UI.

## Adding A Vest Package

1. Add a product flavor in `app/build.gradle.kts`.
2. Add a flavor UI source set, for example `app/src/newvariant/java`.
3. Implement `currentVariantUiRegistry()` in the new flavor package.
4. Provide a `VariantPermissionUi` implementation for the package's permission dialogs.
5. Add package-specific feature UI mapping for titles, icons, dialogs, and route screens.
6. Implement `currentVariantModules()` when the package owns ViewModels or other flavor-only DI bindings.
7. Configure `VariantProfile` values through BuildConfig fields: enabled features, order, ads, legal URLs, and service keys.
8. Add notification shortcut layouts only if the package needs persistent notification shortcuts.
9. Build and test the new flavor.

## Feature Changes

- Add a new `FeatureKey`.
- Add exactly one `FeatureSpec` with route, group, and optional ad placements.
- Register the shared route UI through the flavor `VariantUiRegistry`.
- Enable the feature in the target flavor profile.
- Verify disabled features are hidden from home/file/toolbox entries, route registration, notifications, and ad entry policy.
- Verify new ad area keys exist in `ad_policy.json` or `native_ad_policy.json`.

## Ad Integration

- The `:advertise` module contains the company SDK.
- `AdvertiseConfigFactory` creates the SDK config.
- `AdvertiseConfigValidator` warns when manifest App ID, test IDs, or expected area keys look wrong.
- `AdEventLogger` records show requests, skipped ads, failures, and close callbacks.
- `AdPolicyResourceTest` checks that code-referenced ad area keys exist in local raw policy files.

## Remaining Refactor Notes

- Koin now supports flavor DI through `currentVariantModules()`.
- Settings-only UI state such as `ManagePermissionsViewModel` should live in the owning flavor source set.

## Test Checklist

- `.\gradlew.bat :app:testStoragecleanerDebugUnitTest`
- `.\gradlew.bat :app:assembleStoragecleanerDebug`
- `.\gradlew.bat :app:assembleStoragecleanerRelease`
- Manually verify Splash, Home, Junk Clean, Files, Toolbox, permissions, notifications, and return-home ads.
