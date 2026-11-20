package com.mpi.klass

/**
 * Local-only Zed Assistant strictly scoped to 4 app domains:
 * 1. Books Library
 * 2. Blood Donor
 * 3. Teachers
 * 4. Class Routine
 *
 * Zero external API calls. Every reply is concise, structured, and short.
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

        // Simple short greeting only
        evaluateGreeting(raw, firstName, isBn)?.let { return it }

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

        // 2. Class Routine (check explicit routine/day/period before generic subject/teacher)
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

        // 4. Books Library (by 4-6 digit code, title, or book keyword)
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

        // Generic "class" fallback (if user asked e.g. "ক্লাস কয়টায়" or "CST 5th class") -> Routine
        if (q.contains("class") || q.contains("ক্লাস")) {
            return formatRoutineReply(q, snapshot, todayEnglishDay, isBn)
        }

        // Strict Boundary: reject everything else cleanly & briefly
        val rejectMsg = if (isBn) {
            "দুঃখিত $firstName, আমি কেবল **Books Library, Blood Donor, Teachers** এবং **Class Routine** সম্পর্কেই সংক্ষিপ্ত উত্তর দিতে পারি। এর বাইরে কিছু জানাতে পারছি না।"
        } else {
            "Sorry $firstName, I can only answer short questions about **Books Library, Blood Donor, Teachers**, and **Class Routine**."
        }
        return ZedReply(
            answerText = rejectMsg,
            sourceBadge = "Zed AI",
            isSupportedDomain = false,
            suggestedFollowUps = if (isBn) {
                listOf("CST বই দেখাও", "ব্লাড ডোনার", "টিচার তালিকা", "আজকের রুটিন")
            } else {
                listOf("CST Books", "Blood Donors", "Teachers", "Today's Routine")
            },
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

    private fun evaluateGreeting(raw: String, firstName: String, isBn: Boolean): ZedReply? {
        val l = raw.lowercase().trim()
        val greetings = setOf(
            "hi", "hello", "hey", "hii", "yo", "salam", "assalamu", "assalamualaikum",
            "nomoshkar", "kemon acho", "kemon aso", "হাই", "হ্যালো", "সালাম", "আসসালামু আলাইকুম",
        )
        val isGreeting = l.length <= 36 && greetings.any { g ->
            l == g || Regex("(^|\\s)${Regex.escape(g)}(\\s|\$|[?!.,])").containsMatchIn(l)
        }
        if (isGreeting) {
            val txt = if (isBn) {
                "হ্যালো $firstName! 👋 **Books, Blood Donor, Teachers** বা **Routine** নিয়ে যেকোনো প্রশ্ন করো—সংক্ষেপে উত্তর দিচ্ছি।"
            } else {
                "Hi $firstName! 👋 Ask me about **Books Library, Blood Donor, Teachers**, or **Routine** for a quick summary."
            }
            return ZedReply(
                answerText = txt,
                sourceBadge = "Zed AI",
                suggestedFollowUps = if (isBn) {
                    listOf("CST বই", "ব্লাড ডোনার", "টিচার তালিকা", "আজকের রুটিন")
                } else {
                    listOf("CST Books", "Blood Donors", "Teachers", "Today's Routine")
                },
            )
        }
        if (l.contains("thank") || l.contains("dhonnobad") || l.contains("tnx") || l.contains("ধন্যবাদ")) {
            return ZedReply(
                answerText = if (isBn) "স্বাগতম $firstName! 😊" else "You're welcome, $firstName! 😊",
                sourceBadge = "Zed AI",
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
            return ZedReply(
                answerText = if (isBn) "লাইব্রেরিতে এখনো কোনো বই নেই।" else "No books are available in the library yet.",
                sourceBadge = "Books Library",
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

        val header = if (isBn) {
            "📚 **Books Library** — ${filtered.size}টি বই" + (scopeTag?.let { " ($it)" } ?: "") + ":"
        } else {
            "📚 **Books Library** — ${filtered.size} book(s)" + (scopeTag?.let { " ($it)" } ?: "") + ":"
        }

        val body = if (filtered.isEmpty()) {
            if (isBn) "কোনো বই মেলেনি। কোড বা ডিপার্টমেন্ট (CST, CT, ET) দিয়ে খুঁজুন।"
            else "No matching books found. Try a subject code or department (CST, CT, ET)."
        } else {
            filtered.take(4).joinToString("\n") { b ->
                val deptInfo = b.departmentSemesters.entries.take(2).joinToString(", ") { (d, sems) ->
                    "$d ${sems.joinToString("/")}"
                }.ifBlank { "General" }
                "• **${b.subjectCode}** • ${b.title} (${b.credit} Cr | $deptInfo)"
            } + if (filtered.size > 4) {
                if (isBn) "\n• +${filtered.size - 4}টি আরও বই" else "\n• +${filtered.size - 4} more books"
            } else ""
        }

        return ZedReply(
            answerText = "$header\n$body",
            sourceBadge = "Books Library",
            suggestedFollowUps = if (isBn) listOf("CST বই দেখাও", "৫ম সেমিস্টার বই") else listOf("CST books", "5th semester books"),
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

        if (filtered.isEmpty()) {
            val label = requestedGroup ?: (requestedDept ?: "")
            return ZedReply(
                answerText = if (isBn) {
                    "🩸 ${label.ifBlank { "এই ক্যাটাগরির" }} কোনো ব্লাড ডোনার এখনো পাওয়া যায়নি (মোট ${snapshot.donors.size} জন)।"
                } else {
                    "🩸 No ${label.ifBlank { "matching" }} blood donors found (${snapshot.donors.size} total)."
                },
                sourceBadge = "Blood Donor",
            )
        }

        val groupTitle = requestedGroup ?: (if (isBn) "সকল গ্রুপ" else "All Groups")
        val header = if (isBn) {
            "🩸 **Blood Donor ($groupTitle)** — ${filtered.size} জন:"
        } else {
            "🩸 **Blood Donor ($groupTitle)** — ${filtered.size} donor(s):"
        }
        val list = filtered.take(4).joinToString("\n") { d ->
            val dept = d.department.ifBlank { "MPI" }
            "• **${d.name}** [${d.bloodGroup}] • $dept — 📞 ${d.phone.ifBlank { "N/A" }}"
        } + if (filtered.size > 4) {
            if (isBn) "\n• +${filtered.size - 4} জন আরও ডোনার" else "\n• +${filtered.size - 4} more donors"
        } else ""

        return ZedReply(
            answerText = "$header\n$list",
            sourceBadge = "Blood Donor",
            suggestedFollowUps = listOf("O+ blood donor", "A+ blood donor", "B+ blood donor"),
        )
    }

    private fun formatTeachersReply(
        matchedByName: List<TeacherDirectoryItem>,
        query: String,
        snapshot: ZedContextSnapshot,
        isBn: Boolean,
    ): ZedReply {
        if (snapshot.teachers.isEmpty()) {
            return ZedReply(
                answerText = if (isBn) "টিচার ডিরেক্টরিতে এখনো কোনো তথ্য নেই।" else "No teachers found in the directory yet.",
                sourceBadge = "Teachers",
            )
        }

        val requestedDept = detectRequestedDept(query)
        val filtered = when {
            matchedByName.isNotEmpty() -> matchedByName
            requestedDept != null -> snapshot.teachers.filter { it.department.equals(requestedDept, ignoreCase = true) }
            else -> snapshot.teachers
        }

        val routineEntries = snapshot.routinePlan?.entries.orEmpty()
        val header = if (isBn) {
            "👨‍🏫 **Teachers** — ${filtered.size} জন" + (requestedDept?.let { " ($it)" } ?: "") + ":"
        } else {
            "👨‍🏫 **Teachers** — ${filtered.size} member(s)" + (requestedDept?.let { " ($it)" } ?: "") + ":"
        }

        val body = if (filtered.isEmpty()) {
            if (isBn) "$requestedDept বিভাগের কোনো শিক্ষক পাওয়া যায়নি।" else "No teachers found for $requestedDept."
        } else {
            filtered.take(4).joinToString("\n") { t ->
                val classCount = routineEntries.count {
                    (t.email.isNotBlank() && it.teacherEmail.equals(t.email, true)) ||
                        it.teacherName.equals(t.name, true)
                }
                val deptInfo = t.department.ifBlank { "MPI" }
                val shiftInfo = t.shift.ifBlank { "Flexible" }
                val classPart = if (classCount > 0) {
                    if (isBn) " • $classCount টি ক্লাস" else " • $classCount class(es)"
                } else ""
                val phonePart = t.phone.takeIf { it.isNotBlank() }?.let { " • 📞 $it" } ?: ""
                "• **${t.name}** ($deptInfo, $shiftInfo)$classPart$phonePart"
            } + if (filtered.size > 4) {
                if (isBn) "\n• +${filtered.size - 4} জন আরও শিক্ষক" else "\n• +${filtered.size - 4} more teachers"
            } else ""
        }

        return ZedReply(
            answerText = "$header\n$body",
            sourceBadge = "Teachers",
            suggestedFollowUps = if (isBn) listOf("CST টিচার", "আজকের রুটিন") else listOf("CST teachers", "Today's routine"),
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
            return ZedReply(
                answerText = if (isBn) "এখনো কোনো ক্লাস রুটিন পাবলিশ করা হয়নি।" else "No published class routine is available yet.",
                sourceBadge = "Routine",
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
            .ifBlank { "Published" }

        val header = if (isBn) {
            "🗓️ **Routine ($scopeLabel)** — ${filtered.size}টি ক্লাস:"
        } else {
            "🗓️ **Routine ($scopeLabel)** — ${filtered.size} class(es):"
        }

        val body = if (filtered.isEmpty()) {
            if (isBn) "এই ফিল্টারে কোনো ক্লাস পাওয়া যায়নি।" else "No classes matched that filter."
        } else {
            filtered.take(5).joinToString("\n") { e ->
                val dayShort = e.day.take(3)
                val labTag = if (e.isPractical) " [Lab]" else ""
                val teacher = e.teacherName.ifBlank { "TBA" }
                val room = e.roomNumber.ifBlank { "TBA" }
                "• **$dayShort P${e.periodIndex}** (${e.startTime}): ${e.subjectTitle}$labTag — $teacher (Room $room)"
            } + if (filtered.size > 5) {
                if (isBn) "\n• +${filtered.size - 5}টি আরও ক্লাস" else "\n• +${filtered.size - 5} more classes"
            } else ""
        }

        return ZedReply(
            answerText = "$header\n$body",
            sourceBadge = "Routine",
            suggestedFollowUps = if (isBn) listOf("Sunday রুটিন", "CST ৫ম সেমিস্টার রুটিন") else listOf("Sunday routine", "CST routine"),
        )
    }
}
