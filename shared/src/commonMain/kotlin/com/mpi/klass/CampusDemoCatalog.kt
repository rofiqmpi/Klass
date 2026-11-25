package com.mpi.klass

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Realistic Mymensingh Polytechnic Institute (MPI) campus classrooms, specialized labs,
 * and faculty dataset for full-campus verification and Admin one-tap demo testing.
 */
object CampusDemoCatalog {

    val demoClassrooms: List<ClassroomItem> by lazy {
        val allShifts = listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2)
        val rooms = mutableListOf<ClassroomItem>()

        fun addLab(
            id: String,
            roomNumber: String,
            floor: String,
            specialty: LabSpecialty,
            near: List<String> = emptyList(),
            capacity: Int = 40,
        ) {
            rooms.add(
                ClassroomItem(
                    id = id,
                    roomNumber = roomNumber,
                    buildingOrFloor = floor,
                    roomType = ClassroomType.LAB,
                    department = "ALL",
                    nearestDepartment = near.joinToString(", "),
                    nearestDepartments = near,
                    shifts = allShifts,
                    capacity = capacity,
                    labSpecialty = specialty.key,
                )
            )
        }

        // Dedicated Science Labs (Physics & Chemistry strictly isolated)
        (1..4).forEach { i ->
            addLab("demo_lab_physics_$i", "Physics-Lab-$i", "Science Building, ${(i + 1) / 2}F", LabSpecialty.PHYSICS)
        }
        (1..3).forEach { i ->
            addLab("demo_lab_chemistry_$i", "Chemistry-Lab-$i", "Science Building, ${(i + 2) / 2}F", LabSpecialty.CHEMISTRY)
        }

        // Dedicated Drawing & CAD Labs
        val drawingNear = listOf(
            listOf("CT", "MT"), listOf("CST", "ET"), listOf("PT", "ENT"),
            listOf("CT", "EMT"), listOf("MT", "PT"), listOf("ET", "ENT"), listOf("CST", "CT"),
        )
        drawingNear.forEachIndexed { idx, near ->
            val i = idx + 1
            addLab("demo_lab_drawing_$i", "Drawing-Lab-$i", "Academic Bldg 1, ${(idx % 3) + 1}F", LabSpecialty.DRAWING, near, 45)
        }

        // Computer Labs (9)
        listOf(
            "CST-Software-Lab-1" to listOf("CST"),
            "CST-Software-Lab-2" to listOf("CST"),
            "CST-Software-Lab-3" to listOf("CST"),
            "CST-Software-Lab-4" to listOf("CST"),
            "CST-Web-DB-Lab" to listOf("CST"),
            "CST-IoT-Micro-Lab" to listOf("CST", "ENT"),
            "CST-Network-Lab-1" to listOf("CST", "ENT"),
            "CST-Network-Lab-2" to listOf("CST"),
            "CST-Office-Apps-Lab" to listOf("CT", "MT", "PT", "ENT", "EMT"),
        ).forEachIndexed { idx, (name, near) ->
            addLab("demo_lab_cst_${idx + 1}", name, "IT & Academic Bldg, ${(idx % 4) + 1}F", LabSpecialty.COMPUTER, near)
        }

        // Electrical Labs (11)
        (1..4).forEach { i ->
            addLab("demo_lab_et_m$i", "ET-Machine-Lab-$i", "Electrical Block, 1F", LabSpecialty.ELECTRICAL, listOf("ET"))
            addLab("demo_lab_et_c$i", "ET-Circuit-Lab-$i", "Electrical Block, 2F", LabSpecialty.ELECTRICAL, listOf("ET", "ENT", "CST", "CT"))
        }
        (1..3).forEach { i ->
            addLab("demo_lab_et_p$i", "ET-Power-Wiring-Lab-$i", "Electrical Block, 3F", LabSpecialty.ELECTRICAL, listOf("ET", "PT", "EMT"))
        }

        // Electronics Labs (8)
        (1..4).forEach { i ->
            addLab("demo_lab_ent_e$i", "ENT-Electronics-Lab-$i", "Academic Bldg 3, 2F", LabSpecialty.ELECTRONICS, listOf("ENT", "EMT", "CST", "CT", "ET"))
        }
        (1..3).forEach { i ->
            addLab("demo_lab_ent_d$i", "ENT-Digital-Lab-$i", "Academic Bldg 3, 3F", LabSpecialty.ELECTRONICS, listOf("ENT", "CST"))
        }
        addLab("demo_lab_ent_c1", "ENT-Communication-Lab", "Academic Bldg 3, 3F", LabSpecialty.ELECTRONICS, listOf("ENT"))

        // Civil Labs (8)
        listOf(
            "CT-Survey-Lab-1", "CT-Survey-Lab-2", "CT-Material-Lab-1", "CT-Material-Lab-2",
            "CT-Hydraulics-Lab-1", "CT-Hydraulics-Lab-2", "CT-Geotech-Lab", "CT-Sanitary-Concrete-Lab",
        ).forEachIndexed { idx, name ->
            addLab("demo_lab_ct_${idx + 1}", name, "Civil Block, ${(idx % 3) + 1}F", LabSpecialty.CIVIL, listOf("CT"), 45)
        }

        // Mechanical Workshops (7)
        listOf(
            "MT-Machine-Shop-1" to listOf("MT"),
            "MT-Machine-Shop-2" to listOf("MT"),
            "MT-Machine-Shop-3" to listOf("MT"),
            "MT-Welding-Foundry-1" to listOf("MT", "PT"),
            "MT-Welding-Foundry-2" to listOf("MT", "CT"),
            "MT-Wood-SheetMetal-Shop" to listOf("CT", "MT", "ET"),
            "MT-Metrology-Fluid-Lab" to listOf("MT", "PT"),
        ).forEachIndexed { idx, (name, near) ->
            addLab("demo_lab_mt_${idx + 1}", name, "Workshop Complex, G/1F", LabSpecialty.MECHANICAL_WORKSHOP, near)
        }

        // Power & Automobile Labs (6)
        listOf(
            "PT-Auto-Engine-Lab-1" to listOf("PT"),
            "PT-Auto-Engine-Lab-2" to listOf("PT"),
            "PT-Heat-RAC-Lab-1" to listOf("PT", "MT"),
            "PT-Heat-RAC-Lab-2" to listOf("PT", "MT"),
            "PT-Transmission-Chassis-Lab" to listOf("PT"),
            "PT-PowerPlant-Lab" to listOf("PT"),
        ).forEachIndexed { idx, (name, near) ->
            addLab("demo_lab_pt_${idx + 1}", name, "Power Workshop, ${(idx % 2) + 1}F", LabSpecialty.POWER_AUTO, near)
        }

        // Electro-Medical / Biomedical Labs (5)
        (1..5).forEach { i ->
            addLab("demo_lab_emt_$i", "EMT-Biomedical-Lab-$i", "Medical Tech Wing, ${(i + 1) / 2}F", LabSpecialty.BIOMEDICAL, listOf("EMT"), 35)
        }

        // Shared Math, PE & Environmental Practical Rooms (9)
        (1..9).forEach { i ->
            addLab("demo_lab_general_$i", "Shared-Math-PE-Lab-$i", "Academic Annex, ${(i + 1) / 2}F", LabSpecialty.GENERAL, emptyList(), 50)
        }

        // Theory Classrooms across Buildings 1-4 (91 rooms for full 7-semester 112-group capacity)
        val theoryAllocation = listOf(
            "CST" to (101..112),
            "CT" to (201..217),
            "ET" to (301..317),
            "MT" to (401..412),
            "PT" to (501..512),
            "ENT" to (601..611),
            "EMT" to (701..710),
        )
        theoryAllocation.forEachIndexed { bIdx, (dept, range) ->
            range.forEach { num ->
                rooms.add(
                    ClassroomItem(
                        id = "demo_th_$num",
                        roomNumber = num.toString(),
                        buildingOrFloor = "Academic Bldg ${(bIdx % 4) + 1}, ${((num % 100) / 4) + 1}F",
                        roomType = ClassroomType.THEORY,
                        department = "ALL",
                        nearestDepartment = dept,
                        nearestDepartments = listOf(dept),
                        shifts = allShifts,
                        capacity = 60,
                    )
                )
            }
        }

        rooms
    }

    /**
     * Generates a complete, conflict-free faculty list across Non-Tech (Physics, Chemistry, Drawing,
     * Math, Humanities) and all 7 technical departments (CST, CT, ET, MT, PT, ENT, EMT) for
     * both shifts so every BTEB Probidhan-2022 subject in BtebBooksCatalog has assigned teachers.
     */
    val demoTeachers: List<TeacherSchedulingPref> by lazy {
        val books = BtebBooksCatalog.books
        val teachers = mutableListOf<TeacherSchedulingPref>()

        fun buildSemMapForCodes(targetCodes: Set<String>): Map<String, List<String>> {
            val map = linkedMapOf<String, MutableSet<String>>()
            for (b in books) {
                if (b.subjectCode !in targetCodes) continue
                for (sems in b.departmentSemesters.values) {
                    for (sem in sems) {
                        val key = RoutineRules.canonicalSemesterKey(sem).ifBlank { sem }
                        map.getOrPut(key) { linkedSetOf() }.add(b.subjectCode)
                    }
                }
            }
            return map.mapValues { it.value.toList() }
        }

        val physicsCodes = setOf("25912", "25922")
        val chemCodes = setOf("25913")
        val drawingCodes = setOf("21011")
        val mathCodes = setOf("25911", "25921", "25931")
        val langHumCodes = setOf("25711", "25712", "25721", "25722", "25811", "25812", "25831", "25841", "25851", "25852", "25853")

        val nonTechSpecs = listOf(
            // Physics (8)
            Triple("physics1", "Dr. Mahfuzur Rahman (Physics)", physicsCodes),
            Triple("physics2", "Sabina Yasmin (Physics)", physicsCodes),
            Triple("physics3", "Tariqul Islam (Physics)", physicsCodes),
            Triple("physics4", "Nusrat Sultana (Physics)", physicsCodes),
            Triple("physics5", "Mahbub Alam (Physics)", physicsCodes),
            Triple("physics6", "Farhana Yeasmin (Physics)", physicsCodes),
            Triple("physics7", "Kamrul Ahsan (Physics)", physicsCodes),
            Triple("physics8", "Monira Khatun (Physics)", physicsCodes),
            // Chemistry (5)
            Triple("chem1", "Nazmul Haque (Chemistry)", chemCodes),
            Triple("chem2", "Farzana Akter (Chemistry)", chemCodes),
            Triple("chem3", "Sajjad Hossain (Chemistry)", chemCodes),
            Triple("chem4", "Ashrafun Nahar (Chemistry)", chemCodes),
            Triple("chem5", "Habibul Bashar (Chemistry)", chemCodes),
            // Drawing (6)
            Triple("draw1", "Engr. Kamrul Hasan (Drawing)", drawingCodes),
            Triple("draw2", "Engr. Mizanur Rahman (Drawing)", drawingCodes),
            Triple("draw3", "Engr. Mahmudul Hasan (Drawing)", drawingCodes),
            Triple("draw4", "Engr. Rashedul Islam (Drawing)", drawingCodes),
            Triple("draw5", "Engr. Tarek Aziz (Drawing)", drawingCodes),
            Triple("draw6", "Engr. Shakhawat Hossain (Drawing)", drawingCodes),
            // Mathematics (12)
            Triple("math1", "Anisur Rahman (Math)", mathCodes),
            Triple("math2", "Nusrat Jahan (Math)", mathCodes),
            Triple("math3", "Rafiqul Islam (Math)", mathCodes),
            Triple("math4", "Kamal Uddin (Math)", mathCodes),
            Triple("math5", "Sirajul Islam (Math)", mathCodes),
            Triple("math6", "Ruksana Begum (Math)", mathCodes),
            Triple("math7", "Enamul Haque (Math)", mathCodes),
            Triple("math8", "Tanjina Akter (Math)", mathCodes),
            Triple("math9", "Saifur Rahman (Math)", mathCodes),
            Triple("math10", "Meherun Nesa (Math)", mathCodes),
            Triple("math11", "Zahirul Islam (Math)", mathCodes),
            Triple("math12", "Al-Amin Hossain (Math)", mathCodes),
            // Humanities & Management (13)
            Triple("hum1", "Selina Begum (Non-Tech)", langHumCodes),
            Triple("hum2", "Mosharraf Hossain (Non-Tech)", langHumCodes),
            Triple("hum3", "Nasrin Akter (Non-Tech)", langHumCodes),
            Triple("hum4", "Zakaria Ahmed (Non-Tech)", langHumCodes),
            Triple("hum5", "Feroza Begum (Non-Tech)", langHumCodes),
            Triple("hum6", "Abul Kalam (Non-Tech)", langHumCodes),
            Triple("hum7", "Shirin Akter (Non-Tech)", langHumCodes),
            Triple("hum8", "Nizamul Haque (Non-Tech)", langHumCodes),
            Triple("hum9", "Rokeya Sultana (Non-Tech)", langHumCodes),
            Triple("hum10", "Sazzadul Karim (Non-Tech)", langHumCodes),
            Triple("hum11", "Tasnim Jahan (Non-Tech)", langHumCodes),
            Triple("hum12", "Rehana Parvin (Non-Tech)", langHumCodes),
            Triple("hum13", "Maksudur Rahman (Non-Tech)", langHumCodes),
        )

        for (shift in listOf(RoutineRules.SHIFT_1, RoutineRules.SHIFT_2)) {
            val shiftKey = if (shift == RoutineRules.SHIFT_1) "s1" else "s2"
            nonTechSpecs.forEachIndexed { idx, (slug, name, codes) ->
                teachers.add(
                    TeacherSchedulingPref(
                        email = "demo_${slug}_$shiftKey@mpi.edu.bd",
                        name = "$name [${shift.substringBefore(" ")}]",
                        department = "Non-Tech",
                        shift = shift,
                        timePreference = TeacherTimePreference.entries[idx % 3],
                        semesterSubjects = buildSemMapForCodes(codes),
                        schedulingSetupComplete = true,
                    )
                )
            }

            // Technical departments: 12-18 faculty per dept per shift so all 7 semesters stay balanced
            val deptNames = mapOf(
                "CST" to listOf(
                    "Shafiqul Islam", "Taslima Khatun", "Jahangir Alam", "Mehedi Hasan", "Ariful Islam", "Shamima Akter",
                    "Rakibul Hasan", "Naimul Islam", "Sadia Afrin", "Tanvirul Haque", "Foysal Ahmed", "Sabbir Rahman",
                    "Farhan Sadik", "Tarek Mahmud", "Sumona Akter",
                ),
                "CT" to listOf(
                    "Abdur Razzaque", "Habibur Rahman", "Ferdous Ara", "Kamruzzaman", "Masud Rana", "Salma Khatun",
                    "Touhidul Islam", "Nurul Amin", "Sohel Rana", "Mahmudur Rahman", "Elias Kabir", "Jannatul Ferdous",
                    "Monowar Hossain", "Shamim Reza", "Azharul Islam", "Farida Parvin", "Mokhlesur Rahman", "Nasir Uddin",
                    "Rafiqul Hasan", "Sharmin Jahan",
                ),
                "ET" to listOf(
                    "Mostafizur Rahman", "Delwar Hossain", "Sharmin Sultana", "Monirul Islam", "Kawsar Ahmed", "Hasina Begum",
                    "Azizul Haque", "Lutfur Rahman", "Rezaul Haque", "Shamim Ahmed", "Tariqul Alam", "Nasima Akter",
                    "Babul Mia", "Harun-or-Rashid", "Sohag Hossain", "Dilruba Khanam", "Abul Bashar", "Shariful Alam",
                    "Kamal Hossain", "Farzana Yasmin",
                ),
                "MT" to listOf(
                    "Golam Kibria", "Zakir Hossain", "Ashraful Alam", "Nizam Uddin", "Mofazzal Hossain", "Jasim Uddin",
                    "Sadek Ali", "Humayun Kabir", "Mainul Islam", "Rashida Khatun", "Mostafa Kamal", "Jahidul Islam", "Nuruzzaman",
                    "Imran Mahmud",
                ),
                "PT" to listOf(
                    "Badrul Alam", "Saiful Islam", "Rezaul Karim", "Motiur Rahman", "Aminul Islam", "Shahabuddin",
                    "Tofazzal Hossain", "Khairul Bashar", "Shariful Islam", "Asmaul Husna", "Rofiqul Islam", "Belal Hossain", "Mizanur Mia",
                    "Zahid Hasan",
                ),
                "ENT" to listOf(
                    "Shahinur Rahman", "Tahmina Akter", "Khaled Mahmud", "Imran Hossain", "Biplob Hossain", "Moushumi Akter",
                    "Rubel Mia", "Ahsan Habib", "Sumaiya Islam", "Ashikur Rahman", "Laila Begum", "Rashedul Karim",
                    "Sabbir Ahmed", "Nusrat Ferdous",
                ),
                "EMT" to listOf(
                    "Dr. Humayun Kabir", "Roksana Parvin", "Tanvir Ahmed", "Faruk Hossain", "Nadia Islam", "Shoriful Islam",
                    "Sumon Mia", "Dr. Farzana Islam", "Mizanur Chowdhury", "Tania Sultana", "Mahfuzul Haque", "Arifa Khatun",
                ),
            )

            for (dept in RoutineRules.TECH_DEPARTMENTS) {
                val deptBooks = books.filter { b ->
                    b.departmentSemesters.keys.any { RoutineRules.normalizeDepartmentShort(it) == dept } &&
                        b.subjectCode !in physicsCodes &&
                        b.subjectCode !in chemCodes &&
                        b.subjectCode !in drawingCodes &&
                        b.subjectCode !in mathCodes &&
                        b.subjectCode !in langHumCodes
                }
                val faculty = deptNames[dept].orEmpty()
                val semMap = linkedMapOf<String, MutableSet<String>>()
                deptBooks.forEach { b ->
                    b.departmentSemesters.forEach { (d, sems) ->
                        if (RoutineRules.normalizeDepartmentShort(d) == dept) {
                            sems.forEach { sem ->
                                val sk = RoutineRules.canonicalSemesterKey(sem).ifBlank { sem }
                                semMap.getOrPut(sk) { linkedSetOf() }.add(b.subjectCode)
                            }
                        }
                    }
                }
                val fullSemMap = semMap.mapValues { it.value.toList() }
                faculty.forEachIndexed { idx, fname ->
                    teachers.add(
                        TeacherSchedulingPref(
                            email = "demo_${dept.lowercase()}_${idx + 1}_$shiftKey@mpi.edu.bd",
                            name = "$fname ($dept)",
                            department = dept,
                            shift = shift,
                            timePreference = TeacherTimePreference.entries[idx % 3],
                            semesterSubjects = fullSemMap,
                            schedulingSetupComplete = true,
                        )
                    )
                }
            }
        }
        teachers
    }

    suspend fun seedDemoCampusToFirestore(): Result<Pair<Int, Int>> = coroutineScope {
        var roomsSaved = 0
        var teachersSaved = 0
        var firstErr: Throwable? = null
        demoClassrooms.chunked(24).forEach { batch ->
            val results = batch.map { room ->
                async { saveClassroomInFirestore(room) }
            }.map { it.await() }
            results.forEach { res ->
                if (res.isSuccess) roomsSaved++ else if (firstErr == null) firstErr = res.exceptionOrNull()
            }
        }
        demoTeachers.chunked(24).forEach { batch ->
            val results = batch.map { teacher ->
                async { saveTeacherSchedulingPrefInFirestore(teacher) }
            }.map { it.await() }
            results.forEach { res ->
                if (res.isSuccess) teachersSaved++ else if (firstErr == null) firstErr = res.exceptionOrNull()
            }
        }
        if (roomsSaved > 0 || teachersSaved > 0) {
            Result.success(roomsSaved to teachersSaved)
        } else {
            Result.failure(firstErr ?: Exception("Failed to seed demo campus data"))
        }
    }
}
