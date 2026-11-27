package com.mpi.klass

/**
 * Moderation, PII scrubbing, and factual self-learning guardrails for Zed AI.
 */
object ZedModeration {

    private val EN_PROFANITY_TOKENS = setOf(
        "fuck", "fucking", "fucker", "motherfucker", "shit", "bullshit", "bitch", "bastard",
        "asshole", "dick", "cock", "pussy", "whore", "slut", "cunt", "porn", "nude", "sex",
        "rape", "idiot", "moron", "retard", "loser", "chutiya", "mc", "bc",
    )

    private val BANGLISH_PROFANITY_TOKENS = setOf(
        "bokachoda", "boka choda", "choda", "chudi", "chud", "magi", "khanki", "beshya",
        "haramjada", "haramzada", "kuttar", "suorer", "suor", "sala", "shala", "sali",
        "madarchod", "madar", "baal", "bal", "gud", "bhoda", "boda", "nunu", "dudu",
        "gandu", "randi", "fokir", "hijra",
    )

    private val BN_PROFANITY_STEMS = listOf(
        "চোদ", "মাগি", "খানি", "খানকি", "বেশ্যা", "হারামজাদা", "শুয়োর", "কুত্তার",
        "মাদারচোদ", "বোকাচোদা", "শালা", "শালী", "ভোদাই", "ভোদা", "নুনু", "গান্ডু",
        "রান্ডি", "হিজড়া", "যৌন", "পর্ন",
    )

    private val SPLIT_REGEX = Regex("[^\\p{L}\\p{N}]+")
    private val EMAIL_REGEX = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val BD_PHONE_REGEX = Regex("(?:\\+?88)?01[3-9]\\d{8}")

    fun containsProfanityOrAbuse(text: String): Boolean {
        val raw = text.trim().lowercase()
        if (raw.isBlank()) return false
        val tokens = raw.split(SPLIT_REGEX).filter { it.isNotBlank() }
        if (tokens.any { it in EN_PROFANITY_TOKENS || it in BANGLISH_PROFANITY_TOKENS }) {
            return true
        }
        return BN_PROFANITY_STEMS.any { stem -> raw.contains(stem) }
    }

    fun containsSensitivePii(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false
        if (EMAIL_REGEX.containsMatchIn(trimmed)) return true
        if (BD_PHONE_REGEX.containsMatchIn(trimmed.replace("-", "").replace(" ", ""))) return true
        val lower = trimmed.lowercase()
        return lower.contains("password") ||
            lower.contains("পাসওয়ার্ড") ||
            lower.contains("পাসওয়ার্ড") ||
            lower.contains("secret key") ||
            lower.contains("api key")
    }

    fun asksForRestrictedPersonalData(text: String): Boolean {
        val l = text.trim().lowercase()
        if (l.isBlank()) return false
        val asksSecret = l.contains("password") || l.contains("পাসওয়ার্ড") || l.contains("পাসওয়ার্ড") ||
            l.contains("api key") || l.contains("token") || l.contains("database credential")
        val asksPersonalContact = (l.contains("phone") || l.contains("mobile") || l.contains("number") ||
            l.contains("email") || l.contains("ইমেইল") || l.contains("মেইল") ||
            l.contains("ফোন") || l.contains("মোবাইল") || l.contains("নম্বর")) &&
            !l.contains("subject code") && !l.contains("book code") && !l.contains("room number")
        return asksSecret || asksPersonalContact
    }

    private val OBVIOUS_FALSEHOODS = listOf(
        "earth is flat", "পৃথিবী সমতল", "sun rises in the west", "সূর্য পশ্চিমে ওঠে",
        "2+2=5", "2 + 2 = 5", "water is dry", "mpi is in dhaka", "ময়মনসিংহ পলিটেকনিক ঢাকায়",
        "ময়মনসিংহ পলিটেকনিক ঢাকায়", "fake news", "rumor", "গুজব",
    )

    fun isUnverifiedOrFalseClaim(text: String): Boolean {
        val l = text.trim().lowercase()
        return OBVIOUS_FALSEHOODS.any { l.contains(it) }
    }

    fun sanitizeLearnedEntry(
        topic: String,
        fact: String,
        domain: String,
        authorRole: String,
    ): ZedKnowledgeEntry? {
        val cleanTopic = topic.trim()
        val cleanFact = fact.trim()
        if (cleanTopic.length < 2 || cleanFact.length < 8 || cleanFact.length > 480) return null
        if (containsProfanityOrAbuse(cleanTopic) || containsProfanityOrAbuse(cleanFact)) return null
        if (containsSensitivePii(cleanTopic) || containsSensitivePii(cleanFact)) return null
        if (isUnverifiedOrFalseClaim(cleanTopic) || isUnverifiedOrFalseClaim(cleanFact)) return null

        val safeDomain = when (domain.trim().uppercase()) {
            "BOOKS", "ROUTINE", "TEACHERS", "CLASSROOMS", "BLOOD", "GENERAL" -> domain.trim().uppercase()
            else -> "GENERAL"
        }
        return ZedKnowledgeEntry(
            id = "",
            topic = cleanTopic.take(115),
            fact = cleanFact.take(480),
            domain = safeDomain,
            authorRole = authorRole.ifBlank { "USER" },
            verified = true,
            createdAt = 0L,
        )
    }

    /**
     * Extracts explicit factual teaching statements from chat when the user uses clear teaching intent
     * (e.g. "Remember: ...", "মনে রাখো ...") and verifies against moderation & PII rules.
     */
    fun extractUserTaughtFactCandidate(
        query: String,
        snapshot: ZedContextSnapshot,
    ): ZedKnowledgeEntry? {
        val raw = query.trim()
        if (raw.length < 12 || raw.contains("?")) return null
        if (containsProfanityOrAbuse(raw) || containsSensitivePii(raw) || isUnverifiedOrFalseClaim(raw)) {
            return null
        }

        val prefixes = listOf(
            "remember that", "remember:", "learn this:", "note that", "fyi:",
            "মনে রাখো", "মনে রেখো", "জেনে রাখো", "নোট করো", "তথ্য:",
        )
        val lower = raw.lowercase()
        val matchedPrefix = prefixes.firstOrNull { lower.startsWith(it) } ?: return null
        val payload = raw.substring(matchedPrefix.length).trim().trimStart(':', '-', '—', ',', ' ')
        if (payload.length < 8) return null

        val domain = when {
            lower.contains("book") || lower.contains("syllabus") || lower.contains("বই") || lower.contains("কোড") -> "BOOKS"
            lower.contains("blood") || lower.contains("donor") || lower.contains("রক্ত") -> "BLOOD"
            lower.contains("teacher") || lower.contains("শিক্ষক") -> "TEACHERS"
            lower.contains("room") || lower.contains("lab") || lower.contains("রুম") -> "CLASSROOMS"
            lower.contains("routine") || lower.contains("class") || lower.contains("রুটিন") -> "ROUTINE"
            else -> "GENERAL"
        }
        val topic = payload.split(Regex("[.।,:]")).firstOrNull()?.trim()?.take(60)?.ifBlank { "Campus Note" }
            ?: "Campus Note"

        // Reject contradictions against verified institute facts
        if (lower.contains("department") && lower.contains("100")) return null
        if (lower.contains("friday") && (lower.contains("working day") || lower.contains("regular class"))) return null
        if (snapshot.books.any { it.subjectCode == payload && payload.length == 5 && !lower.contains(it.title.lowercase()) }) {
            return null
        }

        return sanitizeLearnedEntry(
            topic = topic,
            fact = payload,
            domain = domain,
            authorRole = snapshot.userRole?.name ?: "USER",
        )
    }
}

data class ZedBookFilter(
    val dept: String? = null,
    val semesterKey: String? = null,
    val semesterNumber: Int? = null,
    val subjectCode: String? = null,
    val practicalOnly: Boolean? = null,
    val creditFilter: String? = null,
    val searchKeywords: List<String> = emptyList(),
)

/**
 * Local structured campus intelligence engine (and fallback for Firebase AI Logic).
 * Enforces moderation, zero PII exposure, rich Books & Blood Finder filtering, learned knowledge recall,
 * and Google A2UI actionable blocks.
 */
object ZedAiEngine {

    fun parseBookFilter(rawQuery: String, userDept: String? = null, userSem: String? = null): ZedBookFilter {
        val q = rawQuery.lowercase()
        val dept = detectRequestedDept(q) ?: userDept?.takeIf {
            q.contains("amar") || q.contains("my") || q.contains("আমার")
        }
        val semNum = RoutineRules.semesterNumber(rawQuery)
            ?: if (q.contains("amar") || q.contains("my") || q.contains("আমার")) {
                userSem?.let { RoutineRules.semesterNumber(it) }
            } else null
        val semKey = semNum?.let { RoutineRules.canonicalSemesterKey(it.toString()) }?.takeIf { it.isNotBlank() }

        val code = Regex("\\b\\d{4,6}\\b").find(rawQuery)?.value
        val practicalOnly = when {
            q.contains("practical") || q.contains("lab") || q.contains("ব্যবহারিক") || q.contains("ল্যাব") -> true
            q.contains("theory") || q.contains("থিউরি") || q.contains("তাত্ত্বিক") -> false
            else -> null
        }

        val creditMatch = Regex("\\b([1-5])\\s*(?:credit|cr|ক্রেডিট)\\b", RegexOption.IGNORE_CASE).find(rawQuery)
        val creditFilter = creditMatch?.groupValues?.getOrNull(1)

        val stopWords = setOf(
            "book", "books", "library", "syllabus", "subject", "subjects", "code", "codes",
            "credit", "credits", "semester", "sem", "dept", "department", "show", "find", "list",
            "give", "all", "what", "which", "are", "the", "for", "of", "in", "with", "and",
            "practical", "theory", "lab", "বই", "লাইব্রেরি", "সিলেবাস", "সেমিস্টার", "দাও", "দেখাও",
            "সব", "কোন", "কয়টা", "আছে", "আমার", "ডিপার্টমেন্ট",
        ) + RoutineRules.ALL_TEACHER_DEPARTMENTS.map { it.lowercase() }

        val keywords = q.split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { tok ->
                tok.length >= 3 && tok !in stopWords && tok != code &&
                    RoutineRules.semesterNumber(tok) == null
            }

        return ZedBookFilter(
            dept = dept,
            semesterKey = semKey,
            semesterNumber = semNum,
            subjectCode = code,
            practicalOnly = practicalOnly,
            creditFilter = creditFilter,
            searchKeywords = keywords,
        )
    }

    fun filterBooks(books: List<LibraryBook>, filter: ZedBookFilter): List<LibraryBook> {
        return books.filter { b ->
            val deptOk = filter.dept == null || b.departmentSemesters.keys.any {
                RoutineRules.normalizeDepartmentShort(it).equals(filter.dept, ignoreCase = true)
            }
            if (!deptOk) return@filter false

            val semOk = filter.semesterNumber == null || b.departmentSemesters.values.flatten().any { s ->
                RoutineRules.semesterNumber(s) == filter.semesterNumber
            }
            if (!semOk) return@filter false

            val codeOk = filter.subjectCode == null || b.subjectCode.contains(filter.subjectCode, ignoreCase = true)
            if (!codeOk) return@filter false

            val typeOk = when (filter.practicalOnly) {
                true -> b.practicalClassesPerWeek > 0
                false -> b.theoryClassesPerWeek > 0
                null -> true
            }
            if (!typeOk) return@filter false

            val creditOk = filter.creditFilter == null || b.credit.trim().startsWith(filter.creditFilter)
            if (!creditOk) return@filter false

            if (filter.searchKeywords.isEmpty()) return@filter true
            val titleLower = b.title.lowercase()
            val codeLower = b.subjectCode.lowercase()
            filter.searchKeywords.any { kw -> titleLower.contains(kw) || codeLower.contains(kw) }
        }.sortedWith(compareBy<LibraryBook> { it.subjectCode }.thenBy { it.title })
    }

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

        // Guardrail 1: Refuse profanity / obscene messages — never learn or answer casually
        if (ZedModeration.containsProfanityOrAbuse(raw)) {
            val blockedMsg = if (isBn) {
                "দুঃখিত, আমি অশালীন বা আপত্তিকর কোনো মেসেজের উত্তর দিই না এবং এ ধরনের কোনো শব্দ শিখি না। অনুগ্রহ করে শালীন ভাষায় প্রশ্ন করুন।"
            } else {
                "Sorry, I do not reply to profane or abusive messages and never learn inappropriate language. Please ask respectfully."
            }
            return ZedReply(
                answerText = blockedMsg,
                sourceBadge = "Moderated",
                isSupportedDomain = false,
                wasModerated = true,
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = if (isBn) "নিরাপদ আচরণ নীতি" else "Safety Boundary",
                        subtitle = if (isBn) "অশালীন ভাষা গ্রহণযোগ্য নয়" else "Respectful Academic Assistant",
                        domain = "GENERAL",
                    ),
                    ZedA2UiBlock.CalloutCard(
                        message = blockedMsg,
                        isWarning = true,
                    ),
                ),
            )
        }

        // Guardrail 2: Protect personal passwords, phone numbers, emails & security secrets
        if (ZedModeration.asksForRestrictedPersonalData(raw)) {
            val secMsg = if (isBn) {
                "অ্যাপের নিরাপত্তার স্বার্থে Zed AI কোনো ইউজারের পাসওয়ার্ড, ব্যক্তিগত ইমেইল বা ফোন নম্বর শেয়ার করে না। প্রয়োজনে নিচের বাটন থেকে অনুমোদিত স্ক্রিনে দেখুন।"
            } else {
                "For app security and privacy, Zed AI never exposes user passwords, personal emails, or phone numbers. Use the authorized app screens below instead."
            }
            return ZedReply(
                answerText = secMsg,
                sourceBadge = "Privacy Protected",
                isSupportedDomain = true,
                wasModerated = true,
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = if (isBn) "নিরাপত্তা সুরক্ষা" else "Privacy & Security Guard",
                        subtitle = if (isBn) "পাসওয়ার্ড, ফোন ও ইমেইল সুরক্ষিত" else "Credentials & PII Excluded",
                        domain = "GENERAL",
                    ),
                    ZedA2UiBlock.CalloutCard(message = secMsg, isWarning = true),
                    ZedA2UiBlock.ActionRow(
                        title = if (isBn) "অনুমোদিত ডিরেক্টরি খুলুন" else "Open Authorized Directories",
                        actions = listOf(
                            ZedA2UiAction(label = if (isBn) "Teachers দেখুন" else "Open Teachers", actionType = ZedActionType.OPEN_TEACHERS),
                            ZedA2UiAction(label = if (isBn) "Blood Finder খুলুন" else "Open Blood Finder", actionType = ZedActionType.OPEN_BLOOD),
                        ),
                    ),
                ),
            )
        }

        // Guardrail 3: Verified Chat Self-Learning
        ZedModeration.extractUserTaughtFactCandidate(raw, snapshot)?.let { learned ->
            val confirmMsg = if (isBn) {
                "ধন্যবাদ $firstName! সঠিক ও যাচাইযোগ্য তথ্যটি শিখে নিরাপদে সংরক্ষণ করা হয়েছে: \"${learned.fact}\""
            } else {
                "Got it, $firstName! I verified and saved this factual note cleanly: \"${learned.fact}\""
            }
            return ZedReply(
                answerText = confirmMsg,
                sourceBadge = "Learned Knowledge",
                isSupportedDomain = true,
                learnedFact = learned,
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = if (isBn) "নতুন তথ্য শেখা হয়েছে" else "Verified Fact Learned",
                        subtitle = learned.domain,
                        domain = learned.domain,
                    ),
                    ZedA2UiBlock.KeyValueCard(
                        title = learned.topic,
                        items = listOf(
                            ZedKeyValue(if (isBn) "তথ্য" else "Stored Fact", learned.fact),
                            ZedKeyValue(if (isBn) "স্ট্যাটাস" else "Status", if (isBn) "যাচাইকৃত ও সংরক্ষিত" else "Verified & Saved"),
                        ),
                    ),
                ),
            )
        }

        evaluateGreeting(raw, firstName, isBn, snapshot)?.let { return it }

        val q = raw.lowercase()

        // Recall matching verified learned facts
        val matchingLearned = snapshot.learnedKnowledge.filter { entry ->
            val tLower = "${entry.topic} ${entry.fact}".lowercase()
            q.split(Regex("[^\\p{L}\\p{N}]+")).any { tok -> tok.length >= 3 && tLower.contains(tok) }
        }

        // 1. Blood Donor (NO phone/email PII)
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

        // 3. Teachers (NO phone/email PII)
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

        // 4. Classrooms & Specialized Lab Routing (e.g. "Physics practical kon lab e hobe?")
        val asksWherePracticalRuns = (q.contains("practical") || q.contains("ব্যবহারিক") || q.contains("ব্যবহারিক ক্লাস")) &&
            (q.contains("where") || q.contains("kothay") || q.contains("kon ") || q.contains("which ") || q.contains("কোথায়") || q.contains("কোথায়") || q.contains("কোন"))
        if (q.contains("classroom") || q.contains("room") || q.contains("lab") ||
            q.contains("রুম") || q.contains("ক্লাসরুম") || q.contains("ল্যাব") || asksWherePracticalRuns
        ) {
            return formatClassroomsReply(q, snapshot, isBn)
        }

        // 5. Books Library (with full filtering: dept, semester, code, credit, theory/practical)
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
            q.contains("theory") || q.contains("practical") ||
            q.contains("বই") || q.contains("লাইব্রেরি") || q.contains("সিলেবাস") ||
            q.contains("ব্যবহারিক") || q.contains("ক্রেডিট")
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

        // If a learned fact matched, surface it clearly!
        if (matchingLearned.isNotEmpty()) {
            val top = matchingLearned.first()
            return ZedReply(
                answerText = "${top.topic}: ${top.fact}",
                sourceBadge = "Learned Knowledge",
                isSupportedDomain = true,
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = if (isBn) "শেখা তথ্য থেকে উত্তর" else "Learned Campus Knowledge",
                        subtitle = top.domain,
                        domain = top.domain,
                    ),
                    ZedA2UiBlock.KeyValueCard(
                        title = if (isBn) "সংরক্ষিত যাচাইকৃত তথ্য" else "Verified Knowledge",
                        items = matchingLearned.take(4).map { ZedKeyValue(it.topic, it.fact) },
                    ),
                ),
            )
        }

        val rejectMsg = if (isBn) {
            "দুঃখিত $firstName, আমি Books Library, Blood Donor, Teachers, Classrooms এবং Class Routine সম্পর্কে নির্ভুল উত্তর ও অ্যাকশন দিতে পারি।"
        } else {
            "Sorry $firstName, I specialize in Books Library, Blood Finder, Teachers, Classrooms, and Class Routine."
        }
        return ZedReply(
            answerText = rejectMsg,
            sourceBadge = "Zed AI",
            isSupportedDomain = false,
            blocks = listOf(
                ZedA2UiBlock.HeaderBanner(
                    title = if (isBn) "সাপোর্টেড বিষয়" else "Supported Campus Modules",
                    subtitle = "Books • Blood • Teachers • Classrooms • Routine".localized(),
                    domain = "GENERAL",
                ),
                ZedA2UiBlock.CalloutCard(message = rejectMsg, isWarning = false),
                ZedA2UiBlock.ActionRow(
                    title = if (isBn) "সরাসরি খুলুন" else "Quick Navigation",
                    actions = listOf(
                        ZedA2UiAction("Books Library", ZedActionType.OPEN_BOOKS),
                        ZedA2UiAction("Blood Finder", ZedActionType.OPEN_BLOOD),
                        ZedA2UiAction("Class Routine", ZedActionType.OPEN_ROUTINE),
                    ),
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
                "হ্যালো $firstName! আমি Zed। Books Library, Blood Finder, Teachers, Classrooms ও Class Routine থেকে সরাসরি ফিল্টার করে সাহায্য করতে পারি।"
            } else {
                "Hi $firstName! I am Zed. Ask me to filter Books, find Blood Donors, check Classrooms, Teachers, or Routine."
            }
            return ZedReply(
                answerText = txt,
                sourceBadge = "Zed AI",
                blocks = listOf(
                    ZedA2UiBlock.HeaderBanner(
                        title = "Zed AI Assistant".localized(),
                        subtitle = if (isBn) "স্মার্ট ও নিরাপদ ক্যাম্পাস অ্যাসিস্ট্যান্ট" else "Privacy-Safe Campus Intelligence",
                        domain = "GENERAL",
                    ),
                    ZedA2UiBlock.StatRow(
                        listOf(
                            ZedStatMetric("Books", snapshot.books.size.toString()),
                            ZedStatMetric("Teachers", snapshot.teachers.size.toString()),
                            ZedStatMetric("Donors", snapshot.donors.size.toString()),
                        )
                    ),
                    ZedA2UiBlock.ActionRow(
                        title = if (isBn) "দ্রুত কাজ শুরু করুন" else "Quick Actions",
                        actions = listOf(
                            ZedA2UiAction(if (isBn) "বুকস লাইব্রেরি" else "Books Library", ZedActionType.OPEN_BOOKS),
                            ZedA2UiAction(if (isBn) "ব্লাড ফাইন্ডার" else "Blood Finder", ZedActionType.OPEN_BLOOD),
                            ZedA2UiAction(if (isBn) "ক্লাস রুটিন" else "Class Routine", ZedActionType.OPEN_ROUTINE),
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
                        message = if (isBn) "আরও কোনো প্রশ্ন থাকলে লিখুন।" else "Feel free to ask any question anytime.",
                        isWarning = false,
                    )
                ),
            )
        }
        return null
    }

    internal fun detectRequestedDept(q: String): String? {
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
        val catalog = if (snapshot.books.isNotEmpty()) snapshot.books else BtebBooksCatalog.books
        val filter = parseBookFilter(
            rawQuery = rawQuery,
            userDept = snapshot.userProfile?.department,
            userSem = snapshot.userProfile?.semester,
        )
        val filtered = if (directMatches.isNotEmpty() && filter.dept == null && filter.semesterNumber == null && filter.practicalOnly == null) {
            directMatches
        } else {
            val f = filterBooks(catalog, filter)
            if (f.isNotEmpty()) f else directMatches
        }

        val scopeTag = listOfNotNull(
            filter.dept,
            filter.semesterNumber?.let { "Sem $it" },
            when (filter.practicalOnly) {
                true -> "Practical"
                false -> "Theory"
                null -> null
            },
            filter.creditFilter?.let { "$it Cr" },
        ).joinToString(" • ").takeIf { it.isNotBlank() }

        val topBookTag = filtered.firstOrNull()?.let { ": ${it.title} (${it.subjectCode})" }.orEmpty()
        val summary = if (isBn) {
            "Books Library — ${filtered.size}টি বই পাওয়া গেছে" + (scopeTag?.let { " ($it)" } ?: "") + topBookTag
        } else {
            "Books Library — ${filtered.size} matching book(s)" + (scopeTag?.let { " ($it)" } ?: "") + topBookTag
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = "Books Library".localized(),
                subtitle = scopeTag ?: "BTEB Curriculum Catalog",
                domain = "BOOKS",
            ),
            ZedA2UiBlock.StatRow(
                listOf(
                    ZedStatMetric("Matched", filtered.size.toString()),
                    ZedStatMetric("Theory", filtered.count { it.theoryClassesPerWeek > 0 }.toString()),
                    ZedStatMetric("Practical", filtered.count { it.practicalClassesPerWeek > 0 }.toString()),
                )
            ),
        )
        if (filtered.isEmpty()) {
            blocks.add(
                ZedA2UiBlock.CalloutCard(
                    message = if (isBn) "কোনো বই মেলেনি। বিষয় কোড বা ডিপার্টমেন্ট (CST, CT, ET) লিখে খুঁজুন।"
                    else "No matching books found. Search by subject code, credit, or department (CST, CT, ET).",
                    isWarning = false,
                )
            )
        } else {
            blocks.add(
                ZedA2UiBlock.EntityListCard(
                    title = if (isBn) "ফিল্টার করা বইসমূহ" else "Matched Books",
                    entities = filtered.take(8).map { b ->
                        val deptInfo = b.departmentSemesters.entries.take(2).joinToString(", ") { (d, sems) ->
                            "$d ${sems.joinToString("/") { RoutineRules.semesterShortLabel(it) }}"
                        }.ifBlank { "General" }
                        ZedEntityItem(
                            primary = b.title,
                            secondary = "${b.credit} Cr • ${b.theoryClassesPerWeek}T/${b.practicalClassesPerWeek}P • $deptInfo",
                            badge = b.subjectCode,
                            action = ZedA2UiAction(
                                label = "Copy Code".localized(),
                                actionType = ZedActionType.COPY_TEXT,
                                query = b.subjectCode,
                            ),
                        )
                    },
                )
            )
        }
        blocks.add(
            ZedA2UiBlock.ActionRow(
                title = if (isBn) "লাইব্রেরিতে ফিল্টার সহ দেখুন" else "Open Filtered Books",
                actions = listOf(
                    ZedA2UiAction(
                        label = if (isBn) "Books Library খুলুন" else "Open Books Library",
                        actionType = ZedActionType.OPEN_BOOKS,
                        dept = filter.dept.orEmpty(),
                        semester = filter.semesterKey.orEmpty(),
                        query = filter.subjectCode ?: filter.searchKeywords.firstOrNull().orEmpty(),
                    ),
                ),
            )
        )

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
        val wantsStudent = query.contains("student") || query.contains("শিক্ষার্থী")
        val wantsTeacher = query.contains("teacher") || query.contains("শিক্ষক")
        val filtered = snapshot.donors.filter { d ->
            val groupOk = requestedGroup == null || d.bloodGroup.equals(requestedGroup, ignoreCase = true)
            val deptOk = requestedDept == null || d.department.equals(requestedDept, ignoreCase = true)
            val roleOk = when {
                wantsStudent -> d.roleLabel.equals("Student", ignoreCase = true)
                wantsTeacher -> d.roleLabel.equals("Teacher", ignoreCase = true)
                else -> true
            }
            groupOk && deptOk && roleOk
        }
        val groupTitle = requestedGroup ?: (if (isBn) "সকল গ্রুপ" else "All Groups")
        val summary = if (filtered.isEmpty()) {
            val label = requestedGroup ?: (requestedDept ?: "")
            if (isBn) "${label.ifBlank { "এই ক্যাটাগরির" }} কোনো ব্লাড ডোনার পাওয়া যায়নি।"
            else "No ${label.ifBlank { "matching" }} blood donors found (${snapshot.donors.size} registered)."
        } else {
            val deptsCount = filtered.mapNotNull { it.department.takeIf { d -> d.isNotBlank() } }.distinct().size
            val topDonorNames = filtered.take(2).joinToString(", ") { it.name }
            if (isBn) {
                "Blood Finder ($groupTitle) — ${filtered.size} জন ডোনার (${deptsCount}টি ডিপার্টমেন্ট): $topDonorNames। প্রাইভেসি সুরক্ষার জন্য সরাসরি Blood Finder স্ক্রিন থেকে যোগাযোগ করুন।"
            } else {
                "Blood Finder ($groupTitle) — ${filtered.size} donor(s) across $deptsCount dept(s): $topDonorNames. Open Blood Finder below to contact safely."
            }
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = "Blood Finder".localized(),
                subtitle = groupTitle + (requestedDept?.let { " • $it" } ?: ""),
                domain = "BLOOD",
            ),
            ZedA2UiBlock.StatRow(
                listOf(
                    ZedStatMetric("Matched", filtered.size.toString()),
                    ZedStatMetric("Total Donors", snapshot.donors.size.toString()),
                )
            ),
        )
        if (filtered.isEmpty()) {
            blocks.add(
                ZedA2UiBlock.KeyValueCard(
                    title = if (isBn) "ব্লাড ডোনার তথ্য" else "Donor Summary",
                    items = listOf(
                        ZedKeyValue(
                            if (isBn) "সমর্থিত গ্রুপ" else "Supported Groups",
                            RoutineRules.BLOOD_GROUPS.joinToString(", "),
                        ),
                        ZedKeyValue(
                            if (isBn) "নিরাপত্তা" else "Privacy Policy",
                            if (isBn) "ডোনারের ফোন নম্বর ও ইমেইল AI-তে প্রদর্শন করা হয় না।" else "Donor phones & emails are protected and hidden from AI.",
                        ),
                    ),
                )
            )
        } else {
            blocks.add(
                ZedA2UiBlock.EntityListCard(
                    title = if (isBn) "উপলব্ধ ডোনার (নিরাপদ তালিকা)" else "Available Donors",
                    entities = filtered.take(8).map { d ->
                        val meta = listOfNotNull(
                            d.department.ifBlank { "MPI" },
                            d.roleLabel.takeIf { it.isNotBlank() },
                        ).joinToString(" • ")
                        ZedEntityItem(
                            primary = d.name,
                            secondary = meta,
                            badge = d.bloodGroup,
                        )
                    },
                )
            )
        }
        blocks.add(
            ZedA2UiBlock.ActionRow(
                title = if (isBn) "ব্লাড ডিরেক্টরি খুলুন" else "Blood Finder Action",
                actions = listOf(
                    ZedA2UiAction(
                        label = if (requestedGroup != null) "Open Blood Finder ($requestedGroup)" else "Open Blood Finder",
                        actionType = ZedActionType.OPEN_BLOOD,
                        bloodGroup = requestedGroup.orEmpty(),
                        query = requestedDept.orEmpty(),
                    ),
                ),
            )
        )

        return ZedReply(
            answerText = summary,
            sourceBadge = "Blood Finder",
            blocks = blocks,
        )
    }

    private fun formatTeachersReply(
        matchedByName: List<TeacherDirectoryItem>,
        query: String,
        snapshot: ZedContextSnapshot,
        isBn: Boolean,
    ): ZedReply {
        val requestedDept = detectRequestedDept(query)
        val filtered = when {
            matchedByName.isNotEmpty() -> matchedByName
            requestedDept != null -> snapshot.teachers.filter { it.department.equals(requestedDept, ignoreCase = true) }
            else -> snapshot.teachers
        }

        val routineEntries = snapshot.routinePlan?.entries.orEmpty()
        val summary = if (filtered.isEmpty()) {
            if (isBn) "টিচার ডিরেক্টরিতে মেলানো কোনো শিক্ষক পাওয়া যায়নি।"
            else "No matching teachers found in the directory."
        } else {
            val topTeacher = filtered.first()
            val topClassCount = routineEntries.count {
                (topTeacher.email.isNotBlank() && it.teacherEmail.equals(topTeacher.email, true)) ||
                    it.teacherName.equals(topTeacher.name, true)
            }
            if (isBn) {
                "Teachers — ${filtered.size} জন শিক্ষক" + (requestedDept?.let { " ($it)" } ?: "") +
                    ": ${topTeacher.name} (${topClassCount} টি ক্লাস)"
            } else {
                "Teachers — ${filtered.size} faculty member(s)" + (requestedDept?.let { " ($it)" } ?: "") +
                    ": ${topTeacher.name} (${topClassCount} class(es))"
            }
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = "Teachers".localized(),
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
        if (filtered.isNotEmpty()) {
            blocks.add(
                ZedA2UiBlock.EntityListCard(
                    title = if (isBn) "শিক্ষক তালিকা" else "Faculty Members",
                    entities = filtered.take(8).map { t ->
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
                        )
                    },
                )
            )
        }
        blocks.add(
            ZedA2UiBlock.ActionRow(
                title = if (isBn) "টিচার্স স্ক্রিন" else "Directory Action",
                actions = listOf(
                    ZedA2UiAction(
                        label = if (isBn) "Teachers খুলুন" else "Open Teachers",
                        actionType = ZedActionType.OPEN_TEACHERS,
                    ),
                ),
            )
        )

        return ZedReply(
            answerText = summary,
            sourceBadge = "Teachers",
            blocks = blocks,
        )
    }

    internal fun detectRequestedLabSpecialty(
        query: String,
        books: List<LibraryBook> = BtebBooksCatalog.books,
    ): Pair<LabSpecialty, LibraryBook?>? {
        val q = query.lowercase()
        val catalog = if (books.isNotEmpty()) books else BtebBooksCatalog.books

        // 1. Direct subject code match
        val codeMatch = Regex("\\b\\d{5}\\b").find(q)?.value
        if (codeMatch != null) {
            val matchedBook = catalog.firstOrNull { it.subjectCode.equals(codeMatch, true) }
            if (matchedBook != null) {
                val spec = RoutineGeneratorEngine.inferSubjectLabSpecialty(matchedBook)
                if (spec != LabSpecialty.GENERAL || matchedBook.practicalClassesPerWeek > 0) {
                    return spec to matchedBook
                }
            }
        }

        // 2. Explicit specialty keyword match (English, Banglish, Bengali)
        val keywordSpec = when {
            q.contains("physics") || q.contains("ফিজিক্স") || q.contains("পদার্থ") -> LabSpecialty.PHYSICS
            q.contains("chemistry") || q.contains("কেমিস্ট্রি") || q.contains("রসায়ন") || q.contains("রসায়ন") -> LabSpecialty.CHEMISTRY
            q.contains("drawing") || q.contains("drafting") || q.contains("cad") || q.contains("ড্রইং") -> LabSpecialty.DRAWING
            q.contains("biomedical") || q.contains("electro-medical") || q.contains("বায়োমেডিকেল") -> LabSpecialty.BIOMEDICAL
            q.contains("computer") || q.contains("software") || q.contains("programming") || q.contains("network") || q.contains("কম্পিউটার") -> LabSpecialty.COMPUTER
            q.contains("electronics") || q.contains("electronic") || q.contains("ইলেকট্রনিক্স") -> LabSpecialty.ELECTRONICS
            q.contains("electrical") || q.contains("ইলেকট্রিক্যাল") -> LabSpecialty.ELECTRICAL
            q.contains("civil") || q.contains("survey") || q.contains("hydraulics") || q.contains("সিভিল") || q.contains("সার্ভে") -> LabSpecialty.CIVIL
            q.contains("workshop") || q.contains("welding") || q.contains("machine shop") || q.contains("mechanical") || q.contains("মেকানিক্যাল") || q.contains("ওয়েল্ডিং") -> LabSpecialty.MECHANICAL_WORKSHOP
            q.contains("automobile") || q.contains("auto") || q.contains("ic engine") || q.contains("rac") || q.contains("power") || q.contains("অটোমোবাইল") || q.contains("পাওয়ার") -> LabSpecialty.POWER_AUTO
            else -> null
        }
        if (keywordSpec != null) {
            val sampleBook = catalog.firstOrNull { RoutineGeneratorEngine.inferSubjectLabSpecialty(it) == keywordSpec }
            return keywordSpec to sampleBook
        }

        // 3. Fallback: match by book title substring
        val matchedBook = catalog.firstOrNull { b ->
            val t = b.title.lowercase()
            t.length >= 5 && (q.contains(t) || t.split(" ").any { w -> w.length >= 5 && q.contains(w) })
        }
        if (matchedBook != null) {
            return RoutineGeneratorEngine.inferSubjectLabSpecialty(matchedBook) to matchedBook
        }
        return null
    }

    private fun formatClassroomsReply(
        query: String,
        snapshot: ZedContextSnapshot,
        isBn: Boolean,
    ): ZedReply {
        val dept = detectRequestedDept(query)
        val specMatch = detectRequestedLabSpecialty(query, snapshot.books)
        val requestedSpec = specMatch?.first
        val matchedBook = specMatch?.second

        val specLabs = if (requestedSpec != null) {
            snapshot.classrooms.filter { room ->
                room.roomType == ClassroomType.LAB && room.effectiveLabSpecialty() == requestedSpec
            }
        } else {
            emptyList()
        }

        val filtered = when {
            requestedSpec != null && specLabs.isNotEmpty() -> specLabs
            else -> snapshot.classrooms.filter { room ->
                (dept == null || room.department.equals(dept, true) || dept in room.effectiveNearestDepartments()) &&
                    (!query.contains("lab") && !query.contains("ল্যাব") || room.roomType == ClassroomType.LAB)
            }
        }

        val summary = if (requestedSpec != null) {
            val subjTag = matchedBook?.let { "${it.title} (${it.subjectCode})" } ?: requestedSpec.label
            val roomNames = specLabs.take(3).joinToString(", ") { it.roomNumber }
            if (isBn) {
                if (specLabs.isNotEmpty()) {
                    "$subjTag এর ব্যবহারিক ক্লাস ${requestedSpec.label}-এ হয়। ক্যাম্পাসে নির্ধারিত ল্যাব (${specLabs.size}টি): $roomNames।"
                } else {
                    "$subjTag এর ব্যবহারিক ক্লাস সাধারণত ${requestedSpec.label}-এ নির্ধারিত হয় (বর্তমান ডাটাবেজে এখনো ${requestedSpec.label} রুম যুক্ত নেই)।"
                }
            } else {
                if (specLabs.isNotEmpty()) {
                    "$subjTag practicals are held in ${requestedSpec.label}. Campus labs (${specLabs.size}): $roomNames."
                } else {
                    "$subjTag practicals require a dedicated ${requestedSpec.label} (no ${requestedSpec.label} room added yet)."
                }
            }
        } else if (isBn) {
            "Classrooms — ${filtered.size}টি রুম ও ল্যাব পাওয়া গেছে"
        } else {
            "Classrooms — ${filtered.size} room(s) & lab(s) found"
        }

        val blocks = mutableListOf<ZedA2UiBlock>(
            ZedA2UiBlock.HeaderBanner(
                title = requestedSpec?.label ?: "Classrooms",
                subtitle = requestedSpec?.let { if (isBn) "ল্যাব ও বিষয় ম্যাপিং" else "Subject-to-Lab Specialty Routing" }
                    ?: (dept ?: "Theory & Labs"),
                domain = "CLASSROOMS",
            ),
            ZedA2UiBlock.StatRow(
                listOf(
                    ZedStatMetric("Matched", filtered.size.toString()),
                    ZedStatMetric("Labs", filtered.count { it.roomType == ClassroomType.LAB }.toString()),
                    ZedStatMetric("Theory", filtered.count { it.roomType == ClassroomType.THEORY }.toString()),
                )
            ),
        )
        if (requestedSpec != null) {
            blocks.add(
                ZedA2UiBlock.KeyValueCard(
                    title = if (isBn) "রুটিন ইঞ্জিন ল্যাব নিয়ম" else "Lab Specialty Rule",
                    items = listOfNotNull(
                        matchedBook?.let { ZedKeyValue(if (isBn) "সাবজেক্ট" else "Subject", "${it.title} (${it.subjectCode})") },
                        ZedKeyValue(if (isBn) "নির্ধারিত ল্যাব" else "Required Lab", requestedSpec.label),
                        ZedKeyValue(
                            if (isBn) "আইসোলেশন" else "Isolation Rule",
                            if (requestedSpec == LabSpecialty.PHYSICS || requestedSpec == LabSpecialty.CHEMISTRY) {
                                if (isBn) "শুধুমাত্র এই বিষয়ের ব্যবহারিক ক্লাস এখানে বরাদ্দ হয়" else "Dedicated exclusively to ${requestedSpec.label} practicals"
                            } else {
                                if (isBn) "সংশ্লিষ্ট টেকনিক্যাল ব্যবহারিকের জন্য অগ্রাধিকার" else "Prioritized for ${requestedSpec.label} subjects"
                            },
                        ),
                    ),
                )
            )
        }
        if (filtered.isNotEmpty()) {
            blocks.add(
                ZedA2UiBlock.EntityListCard(
                    title = if (isBn) "ক্লাসরুম ও ল্যাব" else "Rooms & Labs",
                    entities = filtered.take(8).map { r ->
                        val specOrType = if (r.roomType == ClassroomType.LAB) r.effectiveLabSpecialty().label else r.roomType.label
                        val loc = r.buildingOrFloor.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()
                        ZedEntityItem(
                            primary = "Room ${r.roomNumber}",
                            secondary = "$specOrType • Cap ${r.capacity}$loc",
                            badge = r.effectiveNearestDepartments().firstOrNull() ?: r.department,
                        )
                    },
                )
            )
        }
        blocks.add(
            ZedA2UiBlock.ActionRow(
                title = if (isBn) "ক্লাসরুম দেখুন" else "Room Action",
                actions = listOf(
                    ZedA2UiAction(
                        label = if (isBn) "Classrooms খুলুন" else "Open Classrooms",
                        actionType = ZedActionType.OPEN_CLASSROOMS,
                    ),
                ),
            )
        )
        return ZedReply(answerText = summary, sourceBadge = "Classrooms", blocks = blocks)
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
                        title = "Class Routine".localized(),
                        subtitle = if (isBn) "MPI শিফট ও ক্লাস সময়সূচি" else "MPI Standard Shift Timings",
                        domain = "ROUTINE",
                    ),
                    ZedA2UiBlock.KeyValueCard(
                        title = if (isBn) "MPI শিফট সূচি" else "MPI Shift Schedule",
                        items = listOf(
                            ZedKeyValue(if (isBn) "১ম শিফট" else "1st Shift", "08:00 AM – 01:15 PM"),
                            ZedKeyValue(if (isBn) "২য় শিফট" else "2nd Shift", "01:30 PM – 06:45 PM"),
                            ZedKeyValue(if (isBn) "কার্যদিবস" else "Working Days", if (isBn) "রবিবার – বৃহস্পতিবার" else "Sunday – Thursday"),
                        ),
                    ),
                    ZedA2UiBlock.ActionRow(
                        title = if (isBn) "রুটিন ট্যাব" else "Open Routine",
                        actions = listOf(
                            ZedA2UiAction(if (isBn) "হোম রুটিন খুলুন" else "Open Home Routine", ZedActionType.OPEN_ROUTINE),
                        ),
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
                title = "Class Routine".localized(),
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
                    slots = filtered.take(8).map { e ->
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
        blocks.add(
            ZedA2UiBlock.ActionRow(
                title = if (isBn) "সম্পূর্ণ রুটিন দেখুন" else "Routine Action",
                actions = listOf(
                    ZedA2UiAction(if (isBn) "রুটিন স্ক্রিন খুলুন" else "Open Routine", ZedActionType.OPEN_ROUTINE),
                ),
            )
        )

        return ZedReply(
            answerText = summary,
            sourceBadge = "Routine",
            blocks = blocks,
        )
    }
}
