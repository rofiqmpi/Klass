# MPI Klass

Mymensingh Polytechnic Institute student and faculty information application built with Kotlin Multiplatform and Jetpack Compose (Material 3).

## Key Features
- **Welcome & Role Authentication**: Google Sign-In and email/role flows for Student, Teacher, Admin, and Guest Mode.
- **Dynamic Online Language Module (`BN` / `EN`)**: Translates interface text dynamically via online dictionary & cloud caching.
- **Home & Menu Navigation**: Synchronized `HorizontalPager` sliding across Home and Menu screens, paired with a persistent floating top header (Close, AI, Notification) featuring drag-to-open window animations.
- **Profile View & Edit Screens**:
  - **Profile View**: Gradient canvas with glassmorphic overlay, scroll-collapsing Brand Card Bar (equal 4-side padding, animates cleanly to a `1:1` clickable Profile Picture on scroll), user name & role subtitle, 5 distinct labeled sections (Personal, Family, Contact, Address, Academic Information), gradient Logout and Delete Account actions, and Footer.
  - **Edit Profile**: Compact editable form across the same 5 labeled sections (native Date Picker, dropdowns, manual Permanent Address, auto-tracked GPS Current Address) with real-time Firebase Firestore & local persistence.
- **Cloud Persistence**: Firebase Auth and Firestore synchronization (`students`, `teachers`, `admins`, `users`) backed by SharedPreferences local cache.

## Architecture
- `app/`: Android application entry point, Manifest, icons, and launcher setup.
- `shared/src/commonMain/kotlin/com/mpi/klass/`: Shared Compose UI screens (`WelcomeScreen`, `RoleHomeScreen`, `GuestHomeScreen`, `ProfileScreen`, `EditProfileScreen`, `NotificationScreen`, `AiScreen`), localization (`AppStrings`), and `RoleConfig`.
- `shared/src/androidMain/kotlin/com/mpi/klass/`: Android platform implementations (Firestore/Auth persistence, photo/date pickers, GPS location, toast helpers).
