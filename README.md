# Klass - Kotlin Multiplatform Project

This is a Kotlin Multiplatform project targeting Android and iOS.

## Project Structure

* **[/androidApp](./androidApp)** contains the Android application entry point and configuration.
* **[/iosApp](./iosApp)** contains the iOS application entry point and Xcode project.
* **[/shared](./shared/src)** contains shared code across Android and iOS applications:
  - **[commonMain](./shared/src/commonMain/kotlin)**: Shared UI (Jetpack Compose Multiplatform), business logic, and Firebase integration.
  - **[androidMain](./shared/src/androidMain/kotlin)**: Android-specific implementations.
  - **[iosMain](./shared/src/iosMain/kotlin)**: iOS-specific implementations and Swift bridge.

## Running the Apps

* **Android App**: `.\gradlew.bat :androidApp:assembleDebug`
* **iOS App**: Open the [/iosApp](./iosApp) directory in Xcode and run on simulator or device.

## Running Tests

- Android tests: `.\gradlew.bat :shared:testAndroidHostTest`
- iOS tests: `.\gradlew.bat :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Admin access

Google sign-in is reserved for approved administrators. Before the first admin login,
create a Firestore document at `admin_emails/{lowercase-email}`. Set `enabled` to
`true` (or omit it). A successful admin login also saves the administrator's profile
in `admins/{lowercase-email}`. Guest mode and email/password sign-in remain available
for other users; Facebook and Apple sign-in are not supported.

The admin app shell contains one Home, Profile, and Menu screen. The Menu screen
includes Profile, Books Library, Teacher, and Classroom entries.
