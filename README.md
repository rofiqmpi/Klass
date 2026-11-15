# MPI Klass

Mymensingh Polytechnic Institute student, faculty, and academic operations application built with Kotlin Multiplatform and Jetpack Compose (Material 3).

## Key Features
- **Screen-Level Language Split**: **Welcome Screen** operates in Bengali (default), while **all inner screens** (Home, Routine, Menu, Profile, Edit Profile, Classrooms, Routine Generator, Blood Finder, Books Library, Teachers Directory, Notifications, and AI Assistant) run strictly in **English** with an instant offline English dictionary in `AppStrings`.
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
- **Personalized Routine Views & Notifications**:
  - **Student Home**: Displays published routine filtered by student's Department, Shift, Semester, and Group.
  - **Teacher Home**: Extracts and displays all classes assigned to the signed-in teacher across all departments, semesters, groups, and rooms, with a button to request slot/room changes.
  - **Classroom Routine Viewer**: Inspect weekly occupancy for any room or lab.
  - **Notifications**: Profile-matched upcoming class schedule alerts plus live Firestore notifications on routine publication and edit request approvals/declines.
- **Blood Donor Finder (Available in Everyone's Menu)**:
  - Accessible from Admin, Teacher, Student, and Guest Menus; searches institute members who registered their Blood Group, with blood-group filter chips (`A+`..`AB-`), department/address search, and zero-permission `ACTION_DIAL` call launcher.
- **Home & Menu Navigation**: Synchronized `HorizontalPager` sliding across Home and Menu screens, paired with swipe-to-open portrait mini-window header gestures.
- **Profile View & Edit Screens**: Adaptive Light/Dark glassmorphic layout with dynamic Department-aware Section/Group selector (`Group A/B/C` for `CT` & `ET`, `Group A/B` for others), Semesters 1–7, `Non-Tech` option for Teachers, gallery upload, and GPS location sync.
- **Admin Teachers Directory & Books Library (179 Official BTEB Probidhan-2022 Books)**: Complete CRUD backed by Firebase Firestore and SharedPreferences cache.
- **Cloud Persistence**: Firebase Auth & Firestore synchronization (`students`, `teachers`, `admins`, `users`, `books`, `classrooms`, `routines`, `routine_edit_requests`, `notifications`, `config`) with SharedPreferences offline cache.

## Architecture
- `app/`: Android application entry point, Manifest, icons, and launcher setup.
- `shared/src/commonMain/kotlin/com/mpi/klass/`: Shared Compose UI screens (`WelcomeScreen`, `RoleHomeScreen`, `GuestHomeScreen`, `ProfileScreen`, `EditProfileScreen`, `TeacherPreferencesScreen`, `ClassroomManagementScreen`, `RoutineGeneratorScreen`, `BloodFinderScreen`, `ClassRoutineTable`, `TeachersDirectoryScreen`, `BooksLibraryScreen`, `NotificationScreen`, `AiAssistantScreen`), pure Kotlin scheduler (`RoutineGeneratorEngine`), official BTEB curriculum (`BtebBooksCatalog`), minimal & 3D UI primitives (`UiPolishComponents`), localization (`AppStrings`), dialer expect declarations (`PhoneDialer`), and `RoleConfig`.
- `shared/src/androidMain/kotlin/com/mpi/klass/`: Android platform implementations (Firestore repositories & batch sync, `ACTION_DIAL` phone dialer, photo/date pickers, GPS location, haptic feedback, toast helpers).
- `firestore.rules`: Authenticated zero-trust security rules covering all app collections.

