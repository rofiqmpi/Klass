# MPI Klass

Mymensingh Polytechnic Institute student, faculty, and academic operations application built with Kotlin Multiplatform and Jetpack Compose (Material 3).

## Key Features
- **Restored Slate & Cobalt Theme with Home Screen Preserved & Social-App Flat Inner Screens**:
  - **Original Color Palette & Large Legible Typography**: Crisp Slate canvas (`#F8FAFC` Light / `#0B1120` Dark) with edge-to-edge `surfaceContainerLowest` feed surfaces (`#FFFFFF` / `#0B1120`), Cobalt primary (`#2563EB` / `#60A5FA`), Cyan/Teal tertiary (`#0F766E` / `#2DD4BF`), Welcome Screen `#8CB9D9` / `#8E7864` / `#94A3B8` 3D accents, and unified `Hind Siliguri` typography scaled up (`17.5.sp` item titles, `15.sp` subtitles, `24.sp` top titles) for instant readability.
  - **Tactile 3D Primary Action Buttons**: Press-animated 3D extruded action buttons (`WelcomeMinimal3DButton`, `WelcomeMinimal3DIconButton`) preserved for primary CTAs, with flat edge-to-edge lists replacing repetitive boxed cards across inner screens.
  - **Restored Original Home Screen & Stationary Bottom Bar**: Floating `HomeRoutineHeader` featuring a compact profile info section next to the avatar (e.g., `6/1/CST-A`) alongside elevated Profile, **Zed AI** (`SparkleIcon`), and Notification (`Res.drawable.notification`) circular action buttons; classic bordered `ClassRoutineTable` with subtle practical lab tinting; and a stationary tactile 3D split `HomeMenuBottomBar` outside the `HorizontalPager` (stays fixed while sliding between Home and Menu) preserving the original `home` and `menu` icons with Cobalt active states and display corner-radius adaptation (`homeShape` / `menuShape`).
  - **Edge-to-Edge Inner Screens & Content-Matched Shimmer Skeletons**: Edge-to-edge list rows separated by clean `0.5.dp` inset dividers and `8.dp` tonal section bands (no boxed cards around rows), colorful circular leading badges (`44.dp–50.dp`), borderless pill search bars, bold underlined/pill tabs, consistent header **Zed AI** (`SparkleIcon`) & Notification circular icon buttons (`HeaderAiNotificationActions`) across inner screens, and Notification-style animated card shimmer skeleton loaders tailored to each screen's layout.
- **Screen-Level Language Split**: **Welcome Screen** operates in Bengali (default), while **all inner screens** run strictly in **English** with an instant offline English dictionary in `AppStrings`.
- **Welcome & Strict Single-Role Authentication (`1-Email = 1-Role`)**:
  - Google Sign-In (`CredentialManager`) and email/password flows for Student, Teacher, Admin, and Guest Mode.
  - Enforces a strict **one-email-per-role lock**: once an email is registered under `ADMIN`, `TEACHER`, or `STUDENT`, attempting to sign in under a different category displays a clear `ROLE_MISMATCH` popup and prevents cross-role account creation or overwrites. User profile records are isolated by role collection (`admins`, `teachers`, `students`, plus `users` metadata index).
- **Mandatory Non-Skippable Sequential Teacher Subjects Setup (Edit Profile & Onboarding)**:
  - Teachers complete a **mandatory sequential flow** (`All Departments` → `Semester 1st–7th` → `Books`) across the seven BTEB departments (`CST`, `CT`, `ET`, `MT`, `PT`, `ENT`, `EMT`) and all seven semesters, accumulating selected teaching subjects across multiple departments and semesters.
  - Accessible exclusively from **Edit Profile** (`edit_teaching_prefs_row`) and automatic Teacher login onboarding (never shown as an inline edit button on read-only Profile View), with no Skip option—teachers cannot exit onboarding until setup is saved.
- **Shared Classroom & Lab Management + Sensor-Based Admin Campus Map**:
  - Manage Theory Classrooms and Labs (`Add`, `Edit`, `Single Delete`, and `Delete All & Fresh Entry`) with room number, building/floor, type (`Theory` / `Lab`), **optional Nearest Department zone** (for proximity ranking, without locking rooms to a single department), and shift availability (`1st Shift`, `2nd Shift`)—with no seating capacity inputs or display clutter.
  - **Admin Campus Map (`admin_campus_map`)**: Uses zero-permission Android `SensorManager` (`TYPE_ROTATION_VECTOR` / accelerometer + magnetometer azimuth heading and accelerometer step-peak dead reckoning) rendered on an interactive 2D Compose `Canvas` campus map. Admins can walk to each department to tap **"Pin Current Position"**, tap directly on the canvas, fine-tune with N/S/E/W step nudges, and save department `(x, y)` meters to Firestore `config/campus_map` + offline `SharedPreferences`.
  - All roles can inspect individual Classroom & Lab weekly routines and free/occupied periods.
- **Automated Proximity-Aware 6-Month Constraint-Based Routine Engine**:
  - Supports **5 working days/week** (`Sunday`–`Thursday`; Friday & Saturday off, no half/short classes).
  - **Group Rules**: `CT` and `ET` have **3 groups** (`Group A`, `Group B`, `Group C`); all other 5 departments (`CST`, `MT`, `PT`, `ENT`, `EMT`) have **2 groups** (`Group A`, `Group B`).
  - Admin configures **Shift 1 Start Time**, **Shift 2 Start Time**, **Period Duration (minutes)**, **Periods Per Day**, and **Running Semesters (1–7)** before generating.
  - **Hard Constraints & Shortest-Distance Room Ranking**: Zero teacher overlap per `(shift, day, period)`, zero room/lab overlap per `(shift, day, period)`, and **Single-Teacher-per-Group-Subject Lock**. All compatible free rooms (`Theory`/`Lab` + shift) are shared institute-wide and ranked dynamically by shortest Euclidean distance (`CampusMapConfig.distanceBetween`) between the class department and each room's `nearestDepartment` zone.
  - **Draft, Manual Slot Edit & Admin Approval Workflow**: Generated routines start as **Draft** for conflict review, live clash-checked manual editing/swapping, and **Approve & Publish**. Teachers can submit **Edit/Swap Requests** requiring Admin approval.
- **Zed AI Academic Assistant (`Firebase.ai` + App Check + Secrets Fallback + A2UI Design)**:
  - Powered by **Firebase AI Logic** (`Firebase.ai(backend = GenerativeBackend.googleAI())` protected by Firebase **App Check** — reCAPTCHA Enterprise in release and Debug provider in debug), with optional `GEMINI_API_KEY` REST fallback via **Secrets** (`.env` / GitHub Actions Secrets → `BuildConfig` → `KlassRuntimeConfig`) when App Check isn't registered on a separate release Firebase project, plus resilient `ZedAiEngine` offline grounding across BTEB books, teachers, blood donors, campus map zones, and live published schedules.
  - **Clean Minimal Chat UX**: No pre-configured quick-question chips or follow-up prompt suggestions; shows a smooth animated 3-dot **AI typing indicator** (`isAiTyping`) with natural typing delay before rendering answers.
  - **Declarative A2UI Structured Rendering (`ZedA2UiSurfaceView`)**: Responses return typed A2UI blocks (`HeaderBanner`, `StatRow`, `KeyValueCard`, interactive `EntityListCard` with direct dialer CTAs for teachers/blood donors, `ScheduleListCard`, and `CalloutCard`) parsed from Gemini `responseSchema` structured JSON.
- **Personalized Routine Views & Real Automatic Android Push Notifications**:
  - **Student Home**: Displays published routine filtered by student's Department, Shift, Semester, and Group.
  - **Teacher Home**: Extracts and displays all classes assigned to the signed-in teacher across all departments, semesters, groups, and rooms, with a button to request slot/room changes.
  - **Classroom Routine Viewer**: Inspect weekly occupancy for any room or lab.
  - **Real Android Push Notifications**: FCM (`KlassMessagingService`) + immediate login welcome push + local `AlarmManager` & `BroadcastReceiver` per-class weekly reminders with `BOOT_COMPLETED` recovery.
- **Blood Donor Finder (Available in Everyone's Menu)**:
  - Accessible from Admin, Teacher, Student, and Guest Menus; searches institute members who registered their Blood Group, with blood-group filter chips (`A+`..`AB-`), department/address search, and zero-permission `ACTION_DIAL` call launcher.
- **Home & Menu Navigation & Account Controls**: Synchronized `HorizontalPager` sliding across Home and Menu screens with stationary tactile 3D bottom bar. Sign Out and Delete Account live cleanly in Menu.
- **Read-Only Facebook-Style Profile View & Edit Profile**:
  - **Profile Screen (Strictly Read-Only)**: Status-bar-inclusive cover photo beneath a transparent fading header (`Back` on left; `Edit profile`, **Zed AI**, and `Notification` with unread dot on right), rounded `22.dp` shoulder, centered shadow-free circular avatar (`140.dp`), centered Name + verified badge, role/department subtitle, Bio, `Edit profile` CTA pill, filter pills (`All`, `About`), and read-only `Academic`, `Contact`, and `Personal` sections with per-field visibility badges (`Public` / `Private`).
  - **Edit Profile Screen**: Facebook-style editor (without `Pinned details`) featuring per-field privacy (`Public` / `Private`), photo & cover pickers, password management, and Teacher **Teaching Subjects** entry.
- **Sequential Signup & Teacher Onboarding**: New Student and Teacher accounts complete a minimal one-question-per-screen profile wizard before entering the app. Teacher sign-up continues into the existing department → semester → subject flow and cannot reach Home until at least one teaching subject is saved.
- **Android APK Releases & Update Checks**: Each push to `master` automatically builds signed release and debug APKs and uploads them as the `klass-android-apks` Actions artifact. Pushing a `vMAJOR.MINOR.PATCH` tag publishes a GitHub Release; the release workflow can also be started manually. Release builds check GitHub Releases for newer versions and open Android's package installer after download; Android still requires the user to confirm installation.
- **Admin Teachers Directory & Books Library (179 Official BTEB Probidhan-2022 Books)**: Complete CRUD backed by Firebase Firestore and SharedPreferences cache.
- **Cloud Persistence**: Firebase Auth & Firestore synchronization (`students`, `teachers`, `admins`, `users`, `books`, `classrooms`, `routines`, `routine_edit_requests`, `notifications`, `config/campus_map`) with SharedPreferences offline cache.

## Architecture
- `app/`: Android application entry point, Firebase App Check initialization (`RecaptchaAppCheckProviderFactory` / `DebugAppCheckProviderFactory`), Manifest, FCM messaging service, icons, and launcher setup.
- `shared/src/commonMain/kotlin/com/mpi/klass/`: Shared Compose UI screens (`WelcomeScreen`, `RoleHomeScreen`, `GuestHomeScreen`, `ProfileScreen`, `EditProfileScreen`, `TeacherPreferencesScreen`, `ClassroomManagementScreen`, `CampusMapScreen`, `RoutineGeneratorScreen`, `BloodFinderScreen`, `ClassRoutineTable`, `TeachersDirectoryScreen`, `BooksLibraryScreen`, `NotificationScreen`, `AiAssistantScreen`), pure Kotlin scheduler (`RoutineGeneratorEngine`), A2UI models & grounding engine (`ZedModels`, `ZedAiEngine`, `ZedAiBackend`), campus map & sensor interfaces (`CampusMapModels`, `CampusMapSensor`), official BTEB curriculum (`BtebBooksCatalog`), UI primitives (`UiPolishComponents`), localization (`AppStrings`), dialer (`PhoneDialer`), and `RoleConfig`.
- `shared/src/commonMain/kotlin/com/mpi/klass/theme/`: Google Material 3 tonal `Color.kt`, `Theme.kt`, typography, system bar theming, and custom Material-style `VectorIcons.kt`.
- `shared/src/androidMain/kotlin/com/mpi/klass/`: Android platform implementations (`ZedAiBackend.android.kt` with `Firebase.ai` + `gemini-3.8-flash` `responseSchema`, `CampusMapSensor.android.kt` with `SensorManager` heading + step dead reckoning, Firestore repositories, `config/campus_map` sync, `ACTION_DIAL` phone dialer, photo/date pickers, GPS location, toast helpers).
- `firestore.rules`: Authenticated zero-trust security rules covering all app collections (`admins`, `teachers`, `students`, `users`, `books`, `classrooms`, `routines`, `routine_edit_requests`, `notifications`, `config`).
- `firebase-blueprint.json`: Structured schema blueprint for all Firestore entities and collections.

## Android releases and updates

The `Android APKs and GitHub Release` workflow runs **automatically on every push to `master`** (as well as tags and manual triggers). It automatically queries the target public release repository (`rofiqmpi/Klass-Releases` by default) for the latest version, computes the next incremental semantic version and `versionCode`, builds and signs the release APK, publishes the GitHub Release (`Klass-release.apk` + `update.json`) to that repository, and updates Firestore `config/app_update` **without requiring any manual Admin Panel intervention**.

Configure these repository Actions secrets:

- `GOOGLE_SERVICES_JSON`: the complete Firebase `google-services.json`.
- `ANDROID_KEYSTORE_BASE64`: base64-encoded, persistent release keystore.
- `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`.
- `PUBLIC_RELEASE_REPO`: Owner/Repository name of your public release repo (e.g., `rofiqmpi/Klass-Releases`).
- `PUBLIC_RELEASE_TOKEN`: GitHub Personal Access Token with repo write permissions to publish releases to `PUBLIC_RELEASE_REPO`.
- Optional runtime secrets:
  - `GEMINI_API_KEY`: optional fallback Gemini API key.
  - `RECAPTCHA_SITE_KEY`: Firebase App Check reCAPTCHA Enterprise site key.
  - `FIRESTORE_DATABASE_ID`: optional Firestore named database ID.
  - `APP_UPDATE_URL`: optional public JSON release feed URL.

Keep the same release keystore in `ANDROID_KEYSTORE_BASE64` for every build. The current signed `Klass-release.apk` certificate fingerprints are:

- SHA-1: `A0:CF:B3:13:F4:C0:5F:9D:22:B5:F6:D7:9F:84:9F:B5:3E:BC:F4:16`
- SHA-256: `B1:F9:5D:B3:D5:74:9F:50:3B:BC:08:BB:43:1F:6D:1C:42:6C:3C:51:97:49:9D:4A:5F:4E:1B:EC:5D:4D:E0:C2`

Add both fingerprints to the Android app in Firebase Console under Project settings → Your apps → SHA certificate fingerprints.

### In-App Update System
Release builds use a built-in `HttpURLConnection` checker that automatically checks for newer versions at launch and when the app returns to the foreground:
1. Custom public feed URL configured via the `APP_UPDATE_URL` secret (`KlassRuntimeConfig.appUpdateUrl`).
2. Public Firestore `config/app_update` document automatically synced on GitHub Actions push.
3. Public GitHub Releases API (`releases/latest`) on `PUBLIC_RELEASE_REPO` (defaults to `rofiqmpi/Klass-Releases`) expecting an asset named `Klass-release.apk`.

Users are prompted inside the app when a newer release is available. Android still requires the user to confirm installation in the system package installer.

