# MPI Klass

Mymensingh Polytechnic Institute student, faculty, and academic operations application built with Kotlin Multiplatform and Jetpack Compose (Material 3).

## Key Features
- **Restored Slate & Cobalt Theme with Home Screen Preserved & Social-App Flat Inner Screens**:
  - **Original Color Palette & Large Legible Typography**: Crisp Slate canvas (`#F8FAFC` Light / `#0B1120` Dark) with edge-to-edge `surfaceContainerLowest` feed surfaces (`#FFFFFF` / `#0B1120`), Cobalt primary (`#2563EB` / `#60A5FA`), Cyan/Teal tertiary (`#0F766E` / `#2DD4BF`), Welcome Screen `#8CB9D9` / `#8E7864` / `#94A3B8` 3D accents, and unified `Hind Siliguri` typography scaled up (`17.5.sp` item titles, `15.sp` subtitles, `24.sp` top titles) for instant readability.
  - **Tactile 3D Primary Action Buttons**: Press-animated 3D extruded action buttons (`WelcomeMinimal3DButton`, `WelcomeMinimal3DIconButton`) preserved for primary CTAs, with flat edge-to-edge lists replacing repetitive boxed cards across inner screens.
  - **Restored Original Home Screen & Stationary Bottom Bar**: Floating `HomeRoutineHeader` featuring a compact profile info section next to the avatar (e.g., `6/1/CST-A`) alongside elevated Profile, AI (`SparkleIcon`), and Notification (`Res.drawable.notification`) circular action buttons; classic bordered `ClassRoutineTable` with subtle practical lab tinting; and a stationary tactile 3D split `HomeMenuBottomBar` outside the `HorizontalPager` (stays fixed while sliding between Home and Menu) preserving the original `home` and `menu` icons with Cobalt active states and display corner-radius adaptation (`homeShape` / `menuShape`).
  - **Telegram / Facebook / TikTok-Style Inner Screens**: Edge-to-edge list rows separated by clean `0.5.dp` inset dividers and `8.dp` tonal section bands (no boxed cards around rows), colorful circular leading badges (`44.dp–50.dp`), borderless pill search bars, bold underlined/pill tabs, and consistent header AI & Notification circular icon buttons (`HeaderAiNotificationActions`) across inner screens.
- **Screen-Level Language Split**: **Welcome Screen** operates in Bengali (default), while **all inner screens** run strictly in **English** with an instant offline English dictionary in `AppStrings`.
- **Welcome & Role Authentication**: Google Sign-In and email/role flows for Student, Teacher, Admin, and Guest Mode.
- **Teacher Onboarding & Scheduling Preferences**:
  - Teachers configure their **Preferred Class Time Slot** (`Early Periods`, `Late Periods`, or `Any Time`), **Department** (`CST`, `CT`, `ET`, `MT`, `PT`, `ENT`, `EMT`, or `Non-Tech`), **Shift**, and **Semester-wise Multiple Subjects (Semesters 1–7)** selected from the Books Library.
  - Triggered automatically upon Teacher login if setup is not yet complete, and editable anytime from Teacher Profile or Menu.
- **Classroom & Lab Management (Admin CRUD + Bulk Reset & Schedule Viewer)**:
  - Manage Theory Classrooms and Labs (`Add`, `Edit`, `Single Delete`, and `Delete All & Fresh Entry`) with room number, building/floor, type (`Theory` / `Lab`), assigned department (`ALL` or specific department), shift availability (`1st Shift`, `2nd Shift`), and capacity.
  - All roles can inspect individual Classroom & Lab weekly routines and free/occupied periods.
- **Automated 6-Month Constraint-Based Routine Engine**:
  - Supports **5 working days/week** (`Sunday`–`Thursday`; Friday & Saturday off, no half/short classes).
  - **Group Rules**: `CT` and `ET` have **3 groups** (`Group A`, `Group B`, `Group C`); all other 5 departments (`CST`, `MT`, `PT`, `ENT`, `EMT`) have **2 groups** (`Group A`, `Group B`).
  - Admin configures **Shift 1 Start Time**, **Shift 2 Start Time**, **Period Duration (minutes)**, **Periods Per Day**, and **Running Semesters (1–7)** before generating.
  - **Hard Constraints**: Zero teacher overlap per `(shift, day, period)`, zero room/lab overlap per `(shift, day, period)`, and **Single-Teacher-per-Group-Subject Lock** (never splits one group's subject across multiple teachers). Matches labs to Lab rooms, spreads Theory/Practical evenly across days, respects teacher time preferences, and reports any teacher/room shortage conflicts.
  - **Draft, Manual Slot Edit & Admin Approval Workflow**: Generated routines start as **Draft** for conflict review, live clash-checked manual editing/swapping, and **Approve & Publish**. Teachers can submit **Edit/Swap Requests** requiring Admin approval.
- **Personalized Routine Views & Real Automatic Android Push Notifications**:
  - **Student Home**: Displays published routine filtered by student's Department, Shift, Semester, and Group.
  - **Teacher Home**: Extracts and displays all classes assigned to the signed-in teacher across all departments, semesters, groups, and rooms, with a button to request slot/room changes.
  - **Classroom Routine Viewer**: Inspect weekly occupancy for any room or lab.
  - **Real Local Android Push Notifications (Zero Firestore Storage)**:
    - Immediately sends an Android heads-up welcome push notification when logging in / entering from Welcome Screen (`POST_NOTIFICATIONS` runtime permission flow on Android 13+).
    - Automatically schedules recurring weekly per-class push alarms via `AlarmManager` + `BroadcastReceiver` (advance reminder before class + class start alert) matched to each user's live weekly schedule, with automatic `+7d` rescheduling and `BOOT_COMPLETED` recovery—stored strictly on-device with zero Firestore notification writes.
- **Blood Donor Finder (Available in Everyone's Menu)**:
  - Accessible from Admin, Teacher, Student, and Guest Menus; searches institute members who registered their Blood Group, with blood-group filter chips (`A+`..`AB-`), department/address search, and zero-permission `ACTION_DIAL` call launcher.
- **Home & Menu Navigation**: Synchronized `HorizontalPager` sliding across Home and Menu screens with stationary tactile 3D bottom bar and direct tap actions on header controls.
- **Profile View & Edit Screens**: Social-app full-bleed profile layout with large avatar hero, dynamic Department-aware Section/Group selector (`Group A/B/C` for `CT` & `ET`, `Group A/B` for others), Semesters 1–7, `Non-Tech` option for Teachers, gallery upload, and GPS location sync.
- **Admin Teachers Directory & Books Library (179 Official BTEB Probidhan-2022 Books)**: Complete CRUD backed by Firebase Firestore and SharedPreferences cache.
- **Cloud Persistence**: Firebase Auth & Firestore synchronization (`students`, `teachers`, `admins`, `users`, `books`, `classrooms`, `routines`, `routine_edit_requests`, `config`) with SharedPreferences offline cache.

## Architecture
- `app/`: Android application entry point, Manifest, icons, and launcher setup.
- `shared/src/commonMain/kotlin/com/mpi/klass/`: Shared Compose UI screens (`WelcomeScreen`, `RoleHomeScreen`, `GuestHomeScreen`, `ProfileScreen`, `EditProfileScreen`, `TeacherPreferencesScreen`, `ClassroomManagementScreen`, `RoutineGeneratorScreen`, `BloodFinderScreen`, `ClassRoutineTable`, `TeachersDirectoryScreen`, `BooksLibraryScreen`, `NotificationScreen`, `AiAssistantScreen`), pure Kotlin scheduler (`RoutineGeneratorEngine`), official BTEB curriculum (`BtebBooksCatalog`), Google Material 3 UI primitives (`UiPolishComponents`), localization (`AppStrings`), dialer expect declarations (`PhoneDialer`), and `RoleConfig`.
- `shared/src/commonMain/kotlin/com/mpi/klass/theme/`: Google Material 3 tonal `Color.kt`, `Theme.kt`, typography, system bar theming, and custom Material-style `VectorIcons.kt`.
- `shared/src/androidMain/kotlin/com/mpi/klass/`: Android platform implementations (Firestore repositories & batch sync, `ACTION_DIAL` phone dialer, photo/date pickers, GPS location, toast helpers).
- `firestore.rules`: Authenticated zero-trust security rules covering all app collections.

