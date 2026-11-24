package com.mpi.klass

/**
 * Local structured campus intelligence engine (and fallback for Firebase AI Logic).
 * Scoped strictly to 4 app domains: Books Library, Blood Donor, Teachers, and Class Routine.
 * Outputs declarative A2UI blocks alongside concise summary text, with zero preset questions.
 */
object ZedAiEngine {

    fun evaluate(
        query: String,
        snapshot: ZedContextSnapshot,
        todayEnglishDay: String = "Sunday",
    ): ZedReply {
        val raw = query.trim()
        val isBn = prefersBengali(raw)
        val firstName = snapshot.userDisplayName.trim().split(" ").firstOrNull()
            ?.takeIf { it.isNotBlank() && !it.equals("User", true) }
            ?: if (isBn) "বন্ধু" else "Friend"

        evaluateGreeting(raw, firstName, isBn, snapshot)?.let { return it }

        val q = raw.lowercase()

        // 1. Blood Donor
        val matchedBloodGroup = RoutineRules.BLOOD_GROUPS.firstOrNull { bg ->
            Regex("(?i)(?:^|\\s)${Regex.escape(bg)}(?:\\s|\$|[?,!।])").containsMatchIn(raw)
        }
        val asksBlood = matchedBloodGroup != null ||
            q.contains("blood") || q.contains("donor") || q.contains("rokto") || q.contains("রক্ত")
        if (asksBlood) {
            return formatBloodReply(matchedBloodGroup, q, snapshot, isBn)
        }

        // 2. Class Routine
        val mentionsRoutineKeyword = q.contains("routine") || q.contains("schedule") ||
            q.contains("period") || q.contains("today class") || q.contains("my class") ||
            q.contains("ajke") || q.contains("ajker") || q.contains("রুটিন") ||
            q.contains("পিরিয়ড") || q.contains("আজকের ক্লাস") || q.contains("কবে ক্লাস") ||
            RoutineRules.WORKING_DAYS.any { q.contains(it.lowercase()) }
        if (mentionsRoutineKeyword) {
            return formatRoutineReply(q, snapshot, todayEnglishDay, isBn)
        }

        // 3. Teachers
        val matchedTeachersByName = snapshot.teachers.filter { t ->
            t.name.length >= 3 && (
                q.split(Regex("\\s+")).any { part ->
                    part.length >= 3 && t.name.lowercase().contains(part) &&
                        part !in setOf("sir", "madam", "teacher", "mam", "islam", "khan", "rahman", "hossain", "ahmed", "md", "md.")
                } || (t.name.length >= 4 && q.contains(t.name.lowercase()))
                )
        }
        val mentionsTeacherKeyword = q.contains("teacher") || q.contains("faculty") ||
            q.contains("sir") || q.contains("madam") || q.contains("instructor") ||
            q.contains("shikkhok") || q.contains("শিক্ষক") || q.contains("স্যার") || q.contains("টিচার")
        if (matchedTeachersByName.isNotEmpty() || mentionsTeacherKeyword) {
            return formatTeachersReply(matchedTeachersByName, q, snapshot, isBn)
        }

        // 4. Books Library
        val digitCodes = Regex("\\b\\d{4,6}\\b").findAll(q).map { it.value }.toList()
        val matchedBooksByCode = if (digitCodes.isNotEmpty()) {
            snapshot.books.filter { book ->
                digitCodes.any { code -> book.subjectCode.contains(code, ignoreCase = true) }
            }
        } else {
            emptyList()
        }
        val matchedBooksByTitle = snapshot.books.filter { book ->
            val t = book.title.lowercase()
            t.length >= 4 && (q.contains(t) || t.split(" ").any { w -> w.length >= 5 && q.contains(w) })
        }
        val mentionsBookKeyword = q.contains("book") || q.contains("books") || q.contains("library") ||
            q.contains("syllabus") || q.contains("credit") || q.contains("boi") ||
            q.contains("বই") || q.contains("লাইব্রেরি") || q.contains("সিলেবাস")
        if (matchedBooksByCode.isNotEmpty() || matchedBooksByTitle.isNotEmpty() || mentionsBookKeyword) {
            val directMatches = (matchedBooksByCode + matchedBooksByTitle).distinctBy { it.id.ifBlank { it.subjectCode } }
            return formatBooksReply(directMatches, raw, snapshot, isBn)
        }

        // Generic "class" or "shift" fallback
        if (q.contains("class") || q.contains("ক্লাস") || q.contains("shift") || q.contains("শিফট")) {
            return formatRoutineReply(q, snapshot, todayEnglishDay, isBn)
        }

        // Department or semester overview
        val askedDeptOnly = detectRequestedDept(q)
        val mentionsSemesterWord = q.contains("semester") || q.contains("sem ") || q.endsWith(" sem") || q.contains("সেমিস্টার")
        val askedSemOnly = if (mentionsSemesterWord) RoutineRules.semesterNumber(raw) else null
        if (askedDeptOnly != null || askedSemOnly != null || q.contains("department") || q.contains("ডিপার্টমেন্ট") || q.contains("probidhan") || q.contains("প্রবিধান")) {
            return formatBooksReply(emptyList(), raw, snapshot, isBn)
        }

        // Strict Boundary
        val rejectMsg = if (isBn) {
            "দুঃখিত $firstName, আমি কেবল Books Library, Blood Donor, Teachers এবং Class Routine সম্পর্কেই উত্তর দিতে পারি।"
        } else {
            "Sorry $firstName, I am scoped strictly to Books Library, Blood Donor, Teachers, and Class Routine."
        }
        return ZedReply(
            answerText = rejectMsg,
            sourceBadge = "Zed AI",
            isSupportedDomain = false,
            blocks = listOf(
                ZedA2UiBlock.HeaderBanner(
                    title = if (isBn) "সীমাবদ্ধ বিষয়" else "Supported Campus Topics",
                    subtitle = "Books • Blood Donor • Teachers • Routine",
                    domain = "GENERAL",
                ),
                ZedA2UiBlock.CalloutCard(
                    message = rejectMsg,
                    isWarning = false,
                ),
            ),
        )
    }

    fun prefersBengali(text: String): Boolean {
        if (text.any { it in '\u0980'..'\u09FF' }) return true
        val l = text.lowercase()
        val banglishMarkers = listOf(
            "kemon", "valo", "bhalo", "korcho", "koro", "amar", "tomar", "tumi", "ache", "ase",
            "kothay", "koyta", "boier", "boi", "sir", "shikkhok", "ajke", "ajker",
            "somporke", "bolo", "dekhao", "khujo", "khuje", "rokto", "blood lagbe",
            "dhonnobad", "assalamu", "salam", "ki khobor", "parbe", "parba", "kobe",
        )
        return banglishMarkers.any { marker ->
            Regex("(^|\\s)${Regex.escape(marker)}(\\s|\$|[?!.,])").containsMatchIn(l) ||
                (marker.contains(" ") && l.contains(marker))
        }
    }

    private fun evaluateGreeting(
        raw: String,
        firstName: String,
        isBn: Boolean,
        snapshot: ZedContextSnapshot,
    ): ZedReply? {
        val l = raw.lowercase().trim()
        val greetings = setOf(
            "hi", "hello", "hey", "hii", "yo", "salam", "assalamu", "assalamualaikum",
            "nomoshkar", "kemon acho", "kemon aso", "kemon achen", "হাই", "হ্যালো", "সালাম", "আসসালামু আলাইকুম",
            "কেমন আছেন", "কেমন আছো", "কেমন আছিস", "কি খবর", "কী খবর", "কি অবস্থা", "কী অবস্থা",
            "ki khobor", "ki obostha", "how are you", "who are you", "tumi ke", "apni ke", "তুমি কে", "আপনি কে",
            "help", "sahajjo", "sahajjyo", "সাহায্য", "হেল্প", "what can you do", "parba", "parbe",
            "good morning", "good evening", "good afternoon", "shuvo sokal", "শুভ সকাল", "শুভ সন্ধ্যা",
        )
        val isGreeting = l.length <= 48 && greetings.any { g ->
            l == g || Regex("(^|\\s)${Regex.escape(g)}(\\s|\$|[?!.,])").containsMatchIn(l)
        }
        if (isGreeting) {
            val txt = if (isBn) {
                "হ্যালো $firstName! আমি Zed, আপনার ক্যাম্পাস অ্যাসিস্ট্যান্ট। Books, Teachers, Donors বা Routine নিয়ে যেকোনো প্রশ্ন করুন।"
            } else {
                "Hi $firstName! I am Zed, your MPI Klass assistant. Ask anything about Books, Teachers, Donors, or Routine."
            }
            return ZedReply(
                answerText = txt,
                sourceBadge = "Zed AI",
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = "Zed Campus Assistant",
                        subtitle = if (isBn) "MPI Klass লাইভ ডিরেক্টরি ও রুটিন" else "MPI Klass Live Campus Knowledge",
                        domain = "GENERAL",
                    ),
                    ZedA2UiBlock.StatRow(
                        listOf(
                            ZedStatMetric("Books", snapshot.books.size.toString()),
                            ZedStatMetric("Teachers", snapshot.teachers.size.toString()),
                            ZedStatMetric("Donors", snapshot.donors.size.toString()),
                        )
                    ),
                    ZedA2UiBlock.KeyValueCard(
                        title = if (isBn) "আমি যেসব তথ্য দিতে পারি" else "Supported Modules",
                        items = listOf(
                            ZedKeyValue("Books Library", if (isBn) "BTEB সিলেবাস, বিষয় কোড ও ক্রেডিট" else "BTEB curriculum, subject codes & credits"),
                            ZedKeyValue("Teachers", if (isBn) "শিক্ষক তালিকা, শিফট ও যোগাযোগ" else "Faculty members, shifts & contacts"),
                            ZedKeyValue("Blood Donor", if (isBn) "জরুরী রক্তের গ্রুপ ও ডোনার ফোন" else "Verified blood groups & donor dialer"),
                            ZedKeyValue("Class Routine", if (isBn) "দৈনিক ক্লাস, শিক্ষক ও রুম নম্বর" else "Daily timetable slots, teachers & rooms"),
                        ),
                    ),
                ),
            )
        }
        if (l.contains("thank") || l.contains("dhonnobad") || l.contains("tnx") || l.contains("ধন্যবাদ")) {
            return ZedReply(
                answerText = if (isBn) "স্বাগতম $firstName!" else "You're welcome, $firstName!",
                sourceBadge = "Zed AI",
                blocks = listOf(
                    ZedA2UiBlock.CalloutCard(
                        message = if (isBn) "আরও কোনো প্রশ্ন থাকলে লিখুন।" else "Feel free to ask any campus question anytime.",
                        isWarning = false,
                    )
                ),
            )
        }
        return null
    }

    private fun detectRequestedDept(q: String): String? {
        val tokens = q.uppercase().replace(Regex("[^A-Z0-9\\-]"), " ").split(Regex("\\s+"))
        for (dept in RoutineRules.ALL_TEACHER_DEPARTMENTS) {
            if (tokens.contains(dept.uppercase())) return dept
        }
        return when {
            q.contains("computer") -> "CST"
            q.contains("civil") -> "CT"
            q.contains("electrical") -> "ET"
            q.contains("mechanical") -> "MT"
            q.contains("power") -> "PT"
            q.contains("electronics") -> "ENT"
            q.contains("electro-medical") || q.contains("electromedical") -> "EMT"
            else -> null
        }
    }

    private fun formatBooksReply(
        directMatches: List<LibraryBook>,
        rawQuery: String,
        snapshot: ZedContextSnapshot,
        isBn: Boolean,
    ): ZedReply {
        if (snapshot.books.isEmpty()) {
            val emptyTxt = if (isBn) {
                "লাইব্রেরিতে কাস্টম বই সংরক্ষিত নেই। BTEB প্রবিধান অনুযায়ী নির্ধারিত বিষয়সমূহ Books Library মেনু থেকে দেখতে পাবেন।"
            } else {
                "No custom books stored yet. Standard BTEB curriculum subjects are available from Books Library."
            }
            return ZedReply(
                answerText = emptyTxt,
                sourceBadge = "Books Library",
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = "Books Library",
                        subtitle = "BTEB Curriculum Catalog",
                        domain = "BOOKS",
                    ),
                    ZedA2UiBlock.CalloutCard(emptyTxt, isWarning = false),
                ),
            )
        }
        val q = rawQuery.lowercase()
        val requestedDept = detectRequestedDept(q) ?: snapshot.userProfile?.department?.takeIf {
            q.contains("amar") || q.contains("my") || q.contains("আমার")
        }
        val requestedSem = RoutineRules.semesterNumber(rawQuery)?.toString()
            ?: if (q.contains("amar") || q.contains("my") || q.contains("আমার")) {
                snapshot.userProfile?.semester?.let { RoutineRules.semesterNumber(it)?.toString() }
            } else null

        val filtered = when {
            directMatches.isNotEmpty() -> directMatches
            requestedDept != null || requestedSem != null -> snapshot.books.filter { book ->
                val deptOk = requestedDept == null || book.departmentSemesters.keys.any {
                    it.equals(requestedDept, ignoreCase = true)
                }
                val semOk = requestedSem == null || book.departmentSemesters.values.flatten().any { s ->
                    RoutineRules.semesterNumber(s)?.toString() == requestedSem
                }
                deptOk && semOk
            }
            else -> snapshot.books
        }

        val scopeTag = listOfNotNull(requestedDept, requestedSem?.let { "Sem $it" })
            .joinToString(" • ")
            .takeIf { it.isNotBlank() }

        val topBookTag = filtered.firstOrNull()?.let { ": ${it.title} (${it.subjectCode})" }.orEmpty()
        val summary = if (isBn) {
            "Books Library — ${filtered.size}টি বই" + (scopeTag?.let { " ($it)" } ?: "") + topBookTag
        } else {
            "Books Library — ${filtered.size} book(s)" + (scopeTag?.let { " ($it)" } ?: "") + topBookTag
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = "Books Library",
                subtitle = scopeTag ?: "BTEB Curriculum Catalog",
                domain = "BOOKS",
            ),
            ZedA2UiBlock.StatRow(
                listOf(
                    ZedStatMetric("Matched", filtered.size.toString()),
                    ZedStatMetric("Catalog", snapshot.books.size.toString()),
                )
            ),
        )
        if (filtered.isEmpty()) {
            blocks.add(
                ZedA2UiBlock.CalloutCard(
                    message = if (isBn) "কোনো বই মেলেনি। কোড বা ডিপার্টমেন্ট (CST, CT, ET) লিখে খুঁজুন।"
                    else "No matching books found. Search by subject code or department (CST, CT, ET).",
                    isWarning = false,
                )
            )
        } else {
            blocks.add(
                ZedA2UiBlock.EntityListCard(
                    title = if (isBn) "পাওয়া বইসমূহ" else "Matched Subjects",
                    entities = filtered.take(6).map { b ->
                        val deptInfo = b.departmentSemesters.entries.take(2).joinToString(", ") { (d, sems) ->
                            "$d ${sems.joinToString("/") { RoutineRules.semesterShortLabel(it) }}"
                        }.ifBlank { "General" }
                        ZedEntityItem(
                            primary = b.title,
                            secondary = "${b.credit} Cr • ${b.theoryClassesPerWeek}T/${b.practicalClassesPerWeek}P • $deptInfo",
                            badge = b.subjectCode,
                        )
                    },
                )
            )
        }

        return ZedReply(
            answerText = summary,
            sourceBadge = "Books Library",
            blocks = blocks,
        )
    }

    private fun formatBloodReply(
        requestedGroup: String?,
        query: String,
        snapshot: ZedContextSnapshot,
        isBn: Boolean,
    ): ZedReply {
        val requestedDept = detectRequestedDept(query)
        val filtered = snapshot.donors.filter { d ->
            val groupOk = requestedGroup == null || d.bloodGroup.equals(requestedGroup, ignoreCase = true)
            val deptOk = requestedDept == null || d.department.equals(requestedDept, ignoreCase = true)
            groupOk && deptOk
        }
        val groupTitle = requestedGroup ?: (if (isBn) "সকল গ্রুপ" else "All Groups")
        val topDonorTag = filtered.firstOrNull()?.let { ": ${it.name} (${it.phone.ifBlank { it.department }})" }.orEmpty()
        val summary = if (filtered.isEmpty()) {
            val label = requestedGroup ?: (requestedDept ?: "")
            if (isBn) "${label.ifBlank { "এই ক্যাটাগরির" }} কোনো ব্লাড ডোনার পাওয়া যায়নি।"
            else "No ${label.ifBlank { "matching" }} blood donors found (${snapshot.donors.size} total)."
        } else {
            if (isBn) "Blood Donor ($groupTitle) — ${filtered.size} জন$topDonorTag"
            else "Blood Donor ($groupTitle) — ${filtered.size} donor(s)$topDonorTag"
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = "Blood Donor Directory",
                subtitle = groupTitle + (requestedDept?.let { " • $it" } ?: ""),
                domain = "BLOOD",
            ),
            ZedA2UiBlock.StatRow(
                listOf(
                    ZedStatMetric("Available", filtered.size.toString()),
                    ZedStatMetric("Total Registered", snapshot.donors.size.toString()),
                )
            ),
        )
        if (filtered.isEmpty()) {
            blocks.add(
                ZedA2UiBlock.KeyValueCard(
                    title = if (isBn) "ব্লাড ডোনার তথ্য" else "Donor Registration Info",
                    items = listOf(
                        ZedKeyValue(
                            if (isBn) "উপলব্ধ গ্রুপসমূহ" else "Supported Groups",
                            RoutineRules.BLOOD_GROUPS.joinToString(", "),
                        ),
                        ZedKeyValue(
                            if (isBn) "যুক্ত হওয়ার উপায়" else "How to Register",
                            if (isBn) "Profile বা Edit Profile থেকে নিজের রক্তের গ্রুপ ও ফোন নম্বর সেভ করুন।" else "Save your blood group & phone in Edit Profile to appear here.",
                        ),
                    ),
                )
            )
            blocks.add(ZedA2UiBlock.CalloutCard(message = summary, isWarning = false))
        } else {
            blocks.add(
                ZedA2UiBlock.EntityListCard(
                    title = if (isBn) "ডোনার তালিকা" else "Verified Donors",
                    entities = filtered.take(6).map { d ->
                        val meta = listOfNotNull(
                            d.department.ifBlank { "MPI" },
                            d.roleLabel.takeIf { it.isNotBlank() },
                            d.address.takeIf { it.isNotBlank() },
                        ).joinToString(" • ")
                        ZedEntityItem(
                            primary = d.name,
                            secondary = meta,
                            badge = d.bloodGroup,
                            phone = d.phone.takeIf { it.isNotBlank() },
                        )
                    },
                )
            )
        }

        return ZedReply(
            answerText = summary,
            sourceBadge = "Blood Donor",
            blocks = blocks,
        )
    }

    private fun formatTeachersReply(
        matchedByName: List<TeacherDirectoryItem>,
        query: String,
        snapshot: ZedContextSnapshot,
        isBn: Boolean,
    ): ZedReply {
        if (snapshot.teachers.isEmpty()) {
            val msg = if (isBn) {
                "টিচার ডিরেক্টরিতে এখনো কোনো শিক্ষক যুক্ত হননি। ময়মনসিংহ পলিটেকনিকের ৮টি টেকনোলজি বিভাগ রয়েছে:"
            } else {
                "No teachers found in the directory yet. Mymensingh Polytechnic has 8 technology departments:"
            }
            return ZedReply(
                answerText = msg,
                sourceBadge = "Teachers",
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = "Faculty & Teachers",
                        subtitle = "Mymensingh Polytechnic Institute",
                        domain = "TEACHERS",
                    ),
                    ZedA2UiBlock.KeyValueCard(
                        title = if (isBn) "একাডেমিক বিভাগসমূহ" else "Academic Departments",
                        items = listOf(
                            ZedKeyValue("CST", "Computer Science & Technology"),
                            ZedKeyValue("CT", "Civil Technology"),
                            ZedKeyValue("ET", "Electrical Technology"),
                            ZedKeyValue("MT", "Mechanical Technology"),
                            ZedKeyValue("PT", "Power Technology"),
                            ZedKeyValue("ENT", "Electronics Technology"),
                            ZedKeyValue("EMT", "Electro-Medical Technology"),
                            ZedKeyValue("Non-Tech", "RS / Related Studies"),
                        ),
                    ),
                    ZedA2UiBlock.CalloutCard(
                        message = if (isBn) {
                            "শিক্ষকগণ 'Teacher' হিসেবে সাইন ইন করলে বা প্রোফাইল আপডেট করলে তাদের যোগাযোগের তথ্য এখানে যুক্ত হবে।"
                        } else {
                            "Teacher contact info and schedules appear here automatically as faculty members register."
                        },
                        isWarning = false,
                    ),
                ),
            )
        }

        val requestedDept = detectRequestedDept(query)
        val filtered = when {
            matchedByName.isNotEmpty() -> matchedByName
            requestedDept != null -> snapshot.teachers.filter { it.department.equals(requestedDept, ignoreCase = true) }
            else -> snapshot.teachers
        }

        val routineEntries = snapshot.routinePlan?.entries.orEmpty()
        val topTeacherTag = filtered.firstOrNull()?.let { t ->
            val cnt = routineEntries.count {
                (t.email.isNotBlank() && it.teacherEmail.equals(t.email, true)) ||
                    it.teacherName.equals(t.name, true)
            }
            if (isBn) ": ${t.name} ($cnt টি ক্লাস)" else ": ${t.name} ($cnt class(es))"
        }.orEmpty()
        val summary = if (isBn) {
            "Teachers Directory — ${filtered.size} জন" + (requestedDept?.let { " ($it)" } ?: "") + topTeacherTag
        } else {
            "Teachers Directory — ${filtered.size} member(s)" + (requestedDept?.let { " ($it)" } ?: "") + topTeacherTag
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = "Faculty & Teachers",
                subtitle = requestedDept ?: "All Departments",
                domain = "TEACHERS",
            ),
            ZedA2UiBlock.StatRow(
                listOf(
                    ZedStatMetric("Matched", filtered.size.toString()),
                    ZedStatMetric("Total Faculty", snapshot.teachers.size.toString()),
                )
            ),
        )
        if (filtered.isEmpty()) {
            blocks.add(
                ZedA2UiBlock.CalloutCard(
                    message = if (isBn) "$requestedDept বিভাগের কোনো শিক্ষক পাওয়া যায়নি।" else "No teachers found for $requestedDept.",
                    isWarning = false,
                )
            )
        } else {
            blocks.add(
                ZedA2UiBlock.EntityListCard(
                    title = if (isBn) "শিক্ষক তালিকা" else "Faculty Members",
                    entities = filtered.take(6).map { t ->
                        val classCount = routineEntries.count {
                            (t.email.isNotBlank() && it.teacherEmail.equals(t.email, true)) ||
                                it.teacherName.equals(t.name, true)
                        }
                        val deptInfo = t.department.ifBlank { "MPI" }
                        val shiftInfo = t.shift.ifBlank { "Flexible" }
                        val cls = if (classCount > 0) " • $classCount classes/wk" else ""
                        ZedEntityItem(
                            primary = t.name,
                            secondary = "$shiftInfo$cls",
                            badge = deptInfo,
                            phone = t.phone.takeIf { it.isNotBlank() },
                        )
                    },
                )
            )
        }

        return ZedReply(
            answerText = summary,
            sourceBadge = "Teachers",
            blocks = blocks,
        )
    }

    private fun formatRoutineReply(
        query: String,
        snapshot: ZedContextSnapshot,
        todayEnglishDay: String,
        isBn: Boolean,
    ): ZedReply {
        val entries = snapshot.routinePlan?.entries.orEmpty()
        if (entries.isEmpty()) {
            val msg = if (isBn) {
                "এখনো কোনো ক্লাস রুটিন পাবলিশ করা হয়নি। নিচে সাধারণ শিফট সময়সূচি দেওয়া হলো:"
            } else {
                "No published class routine is available yet. Standard shift timings below:"
            }
            return ZedReply(
                answerText = msg,
                sourceBadge = "Routine",
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = "Class Routine",
                        subtitle = if (isBn) "MPI শিফট ও ক্লাস সময়সূচি" else "MPI Standard Shift Timings",
                        domain = "ROUTINE",
                    ),
                    ZedA2UiBlock.KeyValueCard(
                        title = if (isBn) "MPI শিফট সূচি" else "MPI Shift Schedule",
                        items = listOf(
                            ZedKeyValue(if (isBn) "১ম শিফট" else "1st Shift", "08:00 AM – 01:15 PM"),
                            ZedKeyValue(if (isBn) "২য় শিফট" else "2nd Shift", "01:30 PM – 06:45 PM"),
                            ZedKeyValue(if (isBn) "কার্যদিবস" else "Working Days", if (isBn) "রবিবার – বৃহস্পতিবার" else "Sunday – Thursday"),
                            ZedKeyValue(if (isBn) "স্ট্যাটাস" else "Status", if (isBn) "এডমিন প্যানেল থেকে রুটিন পাবলিশ হলে বিস্তারিত ক্লাস স্লট দেখা যাবে।" else "Full timetable slots will appear once published by admin."),
                        ),
                    ),
                    ZedA2UiBlock.CalloutCard(
                        message = if (isBn) {
                            "রুটিন তৈরি করতে বা দেখতে 'Routine Management' স্ক্রিন ব্যবহার করুন।"
                        } else {
                            "Use 'Routine Management' to view or publish updated timetable plans."
                        },
                        isWarning = false,
                    ),
                ),
            )
        }

        val askedDay = RoutineRules.WORKING_DAYS.firstOrNull { query.contains(it.lowercase()) }
            ?: if (query.contains("today") || query.contains("ajke") || query.contains("ajker") || query.contains("আজ")) {
                if (todayEnglishDay in RoutineRules.WORKING_DAYS) todayEnglishDay else "Sunday"
            } else null

        val profileDept = snapshot.userProfile?.department?.takeIf { it.isNotBlank() }
        val profileSem = snapshot.userProfile?.semester?.takeIf { it.isNotBlank() }
        val profileShift = snapshot.userProfile?.shift?.takeIf { it.isNotBlank() }

        val explicitDept = detectRequestedDept(query)
        val qDept = explicitDept ?: profileDept
        val qSem = RoutineRules.semesterNumber(query) ?: profileSem?.let { RoutineRules.semesterNumber(it) }

        val filtered = entries.filter { e ->
            val dayOk = askedDay == null || e.day.equals(askedDay, true)
            val deptOk = qDept == null || e.department.equals(qDept, true)
            val semOk = qSem == null || RoutineRules.semesterNumber(e.semester) == qSem
            val shiftOk = profileShift == null || explicitDept != null || e.shift.equals(profileShift, true)
            dayOk && deptOk && semOk && shiftOk
        }.sortedWith(compareBy({ RoutineRules.WORKING_DAYS.indexOf(it.day) }, { it.periodIndex }))

        val scopeLabel = listOfNotNull(askedDay, qDept, qSem?.let { "Sem $it" })
            .joinToString(" • ")
            .ifBlank { "Published Routine" }

        val topSlotTag = filtered.firstOrNull()?.let { e ->
            ": ${e.subjectTitle} • Room ${e.roomNumber.ifBlank { "TBA" }}"
        }.orEmpty()
        val summary = if (isBn) {
            "Routine ($scopeLabel) — ${filtered.size}টি ক্লাস$topSlotTag"
        } else {
            "Routine ($scopeLabel) — ${filtered.size} class(es)$topSlotTag"
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = "Class Routine",
                subtitle = scopeLabel,
                domain = "ROUTINE",
            ),
            ZedA2UiBlock.StatRow(
                listOf(
                    ZedStatMetric("Classes", filtered.size.toString()),
                    ZedStatMetric("Labs", filtered.count { it.isPractical }.toString()),
                )
            ),
        )
        if (filtered.isEmpty()) {
            blocks.add(
                ZedA2UiBlock.CalloutCard(
                    message = if (isBn) "এই ফিল্টারে কোনো ক্লাস পাওয়া যায়নি।" else "No classes matched that filter.",
                    isWarning = false,
                )
            )
        } else {
            blocks.add(
                ZedA2UiBlock.ScheduleListCard(
                    title = if (isBn) "ক্লাস সময়সূচি" else "Timetable Slots",
                    slots = filtered.take(6).map { e ->
                        ZedScheduleSlot(
                            dayAndPeriod = "${e.day.take(3)} P${e.periodIndex + 1}",
                            timeRange = "${e.startTime} – ${e.endTime}",
                            subject = "${e.subjectTitle} (${e.subjectCode})",
                            detail = "${e.teacherName.ifBlank { "TBA" }} • Room ${e.roomNumber.ifBlank { "TBA" }}",
                            isPractical = e.isPractical,
                        )
                    },
                )
            )
        }

        return ZedReply(
            answerText = summary,
            sourceBadge = "Routine",
            blocks = blocks,
        )
    }
}
