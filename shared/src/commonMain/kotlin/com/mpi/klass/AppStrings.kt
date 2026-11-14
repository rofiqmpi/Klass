package com.mpi.klass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

val LocalAppLanguage = compositionLocalOf { AppLanguage.BENGALI }

object AppStrings {
    private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()

    // Reactive Compose state dictionary populated dynamically from cache + online module (0 hardcoded English)
    private val dynamicDict = mutableStateMapOf<String, String>()
    private val pendingTranslations = mutableSetOf<String>()
    private var cacheLoaded = false
    var isSyncingOnline by mutableStateOf(false)
        private set

    // Master list of Bengali source strings used across the app for first-open online dictionary pre-fetching
    private val appBengaliCatalog = listOf(
        // Welcome & Common
        "ভাষা",
        "ভাষা নির্বাচন করুন",
        "থিম নির্বাচন করুন",
        "এগিয়ে যান",
        "অ্যাপ সম্পর্কে",
        "ফোকাস মানে হলো হাজারটা ভালো আইডিয়াকে 'না' বলা। আর সেই একটা 'হ্যাঁ'-এর জন্য শেষ পর্যন্ত লড়ে যাওয়া।",
        "টার্মস অ্যান্ড কন্ডিশন",
        "ময়মনসিংহ সরকারি\nপলিটেকনিক ইনস্টিটিউট",
        "ময়মনসিংহ সরকারি পলিটেকনিক ইনস্টিটিউট",
        "অ্যাপ থেকে বের হতে আবার ব্যাক প্রেস করুন",
        "বন্ধ করুন",
        "ভার্সন ১.০.০",
        "অ্যাপের সকল ফিচারসমূহ",
        "শিক্ষার্থী, শিক্ষক ও প্রশাসনের সকল একাডেমিক কার্যক্রম সহজ, দ্রুত ও ডিজিটাল করার জন্য নির্মিত স্মার্ট ক্লাসরুম প্ল্যাটফর্ম।",
        "অ্যাপ বন্ধ করুন",
        "আপনি কি অ্যাপটি বন্ধ করতে চান?",
        "হ্যাঁ",
        "না",
        "বাতিল",
        "গেস্ট মোড",
        "গেস্ট",
        "গেস্ট ইউজার",
        "সাইন ইন",
        "সাইন ইন করুন",
        "Google অথবা ইমেইল অ্যাকাউন্ট\nব্যবহার করে সাইন ইন করুন",

        // About App Features
        "রোল-ভিত্তিক নিরাপদ সাইন-ইন",
        "শিক্ষার্থী, শিক্ষক ও এডমিনদের জন্য Google Sign-In এবং অতিথিদের জন্য সহজ গেস্ট মোড সুবিধা।",
        "স্মার্ট ক্লাস রুটিন",
        "সেমিস্টার, শিফট, ডিপার্টমেন্ট ও সেকশন অনুযায়ী সম্পূর্ণ ক্লাস রুটিন টেবিল এক নজরে দেখার সুবিধা।",
        "এআই স্টাডি অ্যাসিস্ট্যান্ট",
        "পড়াশোনা, কোডিং, অ্যাসাইনমেন্ট ও যেকোনো একাডেমিক প্রশ্নোত্তরের জন্য তাৎক্ষণিক স্মার্ট এআই সহায়তা।",
        "নোটিশ বোর্ড ও নোটিফিকেশন",
        "ইনস্টিটিউট ও ডিপার্টমেন্টের জরুরি নোটিশ, পরীক্ষার সময়সূচি ও আপডেট রিয়েল-টাইমে জানা।",
        "বুকস লাইব্রেরি ও ক্লাসরুম",
        "সেমিস্টার ভিত্তিক পাঠ্যবই, লেকচার নোট ও অনলাইন ক্লাসরুম সংক্রান্ত প্রয়োজনীয় তথ্য।",
        "শিক্ষকবৃন্দের ডিরেক্টরি",
        "ডিপার্টমেন্টের সম্মানিত শিক্ষকগণের তালিকা এবং প্রয়োজনীয় যোগাযোগের তথ্য।",
        "ক্লাউড প্রোফাইল ও অটো জিপিএস সিঙ্ক",
        "গ্যালারি থেকে প্রোফাইল ছবি আপলোড, নাম ও রোল সংরক্ষণ, ব্লাড গ্রুপ (ডোনার তথ্য) এবং অবস্থান পরিবর্তন বা অনলাইনে আসলে অটো জিপিএস ঠিকানা সিঙ্ক।",
        "এডমিন কন্ট্রোল প্যানেল",
        "নতুন এডমিন রিকোয়েস্ট অনুমোদন (Approve/Decline) এবং রোল-ভিত্তিক সাইন-ইন নিয়ন্ত্রণ ব্যবস্থা।",
        "থিম ও দ্বিভাষিক সুবিধা (BN/EN)",
        "লাইট, ডার্ক ও অটো সিস্টেম থিম এবং সম্পূর্ণ বাংলা ও ইংরেজি ভাষায় অ্যাপ ব্যবহারের সুবিধা।",

        // Terms & Conditions
        "সেবার শর্তাবলী ও প্রাইভেসি পলিসি",
        "দয়া করে নিচের নিয়মগুলো দেখে নিন:",
        "গোপনীয়তা ও সুরক্ষা",
        "শিক্ষার্থী ও শিক্ষকগণের অ্যাকাউন্টের গোপনীয়তা এবং ব্যক্তিগত তথ্য সম্পূর্ণ সুরক্ষিত রাখা হয়।",
        "নিরাপদ সাইন-ইন",
        "নিরাপদ ও ভেরিফাইড অথেনটিকেশন ব্যবস্থার মাধ্যমে আপনার অ্যাকাউন্ট সুরক্ষিত থাকে।",
        "এআই স্টাডি সহায়তা",
        "জেনারেটিভ এআই অ্যাসিস্ট্যান্ট শিক্ষার্থীদের পড়াশোনা, রুটিন এবং একাডেমিক প্রশ্নোত্তরে সহায়তা করে।",
        "অফিশিয়াল নোটিশ",
        "প্রতিষ্ঠান ও ডিপার্টমেন্টের জরুরি নোটিশ এবং রুটিন নিয়মিত আপডেট করা হয়।",
        "নিয়মাবলী ও শৃঙ্খলা",
        "অ্যাপ ব্যবহারের সময় প্রাতিষ্ঠানিক নিয়মকানুন ও শালীনতা বজায় রাখা বাধ্যতামূলক।",
        "সেবা আপডেট",
        "শিক্ষার্থীদের সুবিধার্থে অ্যাপে নতুন ফিচার ও আপডেট নিয়মিত যুক্ত করা হবে।",

        // Role Selection & Dashboards
        "শিক্ষার্থী",
        "শিক্ষক",
        "এডমিন",
        "এডমিনিস্ট্রেটর",
        "আপনার অ্যাকাউন্ট ক্যাটাগরি নির্বাচন করুন। দয়া করে ভুল তথ্য দেওয়া থেকে বিরত থাকুন।",
        "এডমিন ড্যাশবোর্ড",
        "শিক্ষক ড্যাশবোর্ড",
        "শিক্ষার্থী ড্যাশবোর্ড",

        // Navigation, Theme & Notification
        "হোম",
        "মেনু",
        "প্রোফাইল",
        "রুটিন",
        "নোটিফিকেশন",
        "AI সহকারী",
        "লাইট",
        "ডার্ক",
        "ডিফল্ট",
        "অটো",
        "কোনো নতুন নোটিফিকেশন নেই",
        "খুলতে ছেড়ে দিন",
        "মাঝখানে আনুন",

        // Routine Table
        "দিন",
        "সময়",
        "বিষয়",
        "কোড",
        "কক্ষ",
        "রুম",
        "রবিবার",
        "সোমবার",
        "মঙ্গলবার",
        "বুধবার",
        "বৃহস্পতিবার",

        // AI Assistant
        "স্বাগতম! আমি আপনার Klass Gemini AI স্টাডি সহকারী। ক্লাস নোটের সারাংশ, গণিত-বিজ্ঞান সমাধান, কুইজ প্রস্তুতি বা যেকোনো পড়াশোনার বিষয়ে আমাকে প্রশ্ন করতে পারেন।",
        "ক্লাস নোটের সারাংশ তৈরি করুন",
        "দয়া করে আমার পড়া বিষয়ের একটি পরিষ্কার ও পয়েন্ট ভিত্তিক সারাংশ তৈরি করুন:",
        "কঠিন বিষয় সহজে ব্যাখ্যা",
        "একটি জটিল বৈজ্ঞানিক বা গাণিতিক বিষয় সহজ উদাহরণ দিয়ে ব্যাখ্যা করুন:",
        "অনুশীলনী প্রশ্ন তৈরি",
        "পরীক্ষার প্রস্তুতির জন্য ৫টি গুরুত্বপূর্ণ প্রশ্ন এবং উত্তর তৈরি করুন:",
        "পড়ার রুটিন তৈরি করুন",
        "প্রতিদিনের পড়ার একটি কার্যকরী ও ভারসাম্যপূর্ণ সময়সূচি সাজিয়ে দিন।",
        "AI সংযোগে ত্রুটি হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।",
        "AI প্রতিক্রিয়া সময়সীমা অতিক্রান্ত হয়েছে (Timeout). অনুগ্রহ করে আবার চেষ্টা করুন।",
        "ফিরে যান",
        "Gemini AI সহকারী",
        "নতুন চ্যাট শুরু হয়েছে। আপনি কী জানতে চান?",
        "নতুন চ্যাট",
        "Gemini চিন্তা করছে...",
        "আবার চেষ্টা করুন",
        "দ্রুত জিজ্ঞাসা করুন:",
        "AI-কে যেকোনো প্রশ্ন করুন...",
        "পাঠান",

        // Menu Screen
        "প্রোফাইল সেটিংস",
        "আপনার অ্যাকাউন্ট ও ব্যক্তিগত তথ্য ম্যানেজ করুন",
        "বুকস লাইব্রেরি",
        "সেমিস্টার অনুযায়ী ডিপার্টমেন্টাল বই ও লেকচার নোট",
        "ক্লাসরুম",
        "অনলাইন ক্লাস ও সেমিস্টার রুটিন তথ্য",
        "শিক্ষকবৃন্দ",
        "সম্মানিত শিক্ষকগণের তালিকা ও যোগাযোগের তথ্য",
        "নোটিশ বোর্ড",
        "ইনস্টিটিউট ও ডিপার্টমেন্টের জরুরি আপডেট ও নোটিশ",
        "নতুন সাইন ইন নিয়ন্ত্রণ",
        "অ্যাডমিন, শিক্ষক ও শিক্ষার্থী সাইন ইন পারমিশন",
        "ক্লিক করে সেটিংস প্যানেল খুলুন",
        "সাইন ইন বন্ধ রাখলে শুধুমাত্র পূর্বের রেজিস্টার্ড ব্যবহারকারীরা লগইন করতে পারবেন। নতুনদের জন্য প্রবেশ বন্ধ থাকবে।",
        "এডমিন সাইন ইন",
        "শিক্ষক সাইন ইন",
        "শিক্ষার্থী সাইন ইন",
        "এডমিন রিকোয়েস্ট ও অনুমোদন",
        "টি আবেদন অনুমোদনের অপেক্ষায়",
        "কোনো পেন্ডিং আবেদন নেই",
        "বর্তমানে কোনো পেন্ডিং এডমিন অ্যাক্সেস রিকোয়েস্ট নেই।",
        "অ্যাপ থিম পরিবর্তন",
        "লাইট, ডার্ক অথবা অটো থিম সিলেক্ট করুন",
        "ক্লিক করে থিম সেটিংস খুলুন",
        "চালু আছে",
        "বন্ধ রয়েছে",
        "এডমিন রিকোয়েস্টকারী",
        "অপেক্ষমাণ",
        "অনুমোদন করুন",
        "প্রত্যাখ্যান করুন",

        // Profile & Edit Profile Screen
        "গ্যালারি থেকে ছবি আপলোড",
        "সেভ করুন",
        "নাম এডিট করুন",
        "এডিট",
        "এডিট করুন",
        "প্রোফাইল দেখুন",
        "শিক্ষার্থী প্রোফাইল",
        "শিক্ষক প্রোফাইল",
        "এডমিন প্রোফাইল",
        "আপনার প্রোফাইল সম্পাদনা করুন",
        "১. ব্যক্তিগত তথ্য (Personal Information)",
        "২. পারিবারিক তথ্য (Family Information)",
        "৩. যোগাযোগের তথ্য (Contact Information)",
        "৪. ঠিকানার তথ্য (Address Information)",
        "৫. একাডেমিক তথ্য (Academic Information)",
        "২. একাডেমিক তথ্য (Academic Information)",
        "শিক্ষার্থীর পূর্ণ নাম (Full Name)",
        "আপনার পুরো নাম লিখুন",
        "পিতার নাম (Father's Name)",
        "পিতার নাম",
        "পিতার নাম লিখুন",
        "মাতার নাম (Mother's Name)",
        "মাতার নাম",
        "মাতার নাম লিখুন",
        "জন্ম তারিখ",
        "জন্ম তারিখ নির্বাচন করুন",
        "গ্রুপ নির্বাচন করুন",
        "ধর্ম",
        "ধর্ম নির্বাচন করুন",
        "ইসলাম",
        "হিন্দুধর্ম",
        "বৌদ্ধধর্ম",
        "খ্রিস্টধর্ম",
        "অন্যান্য",
        "লিঙ্গ (Gender)",
        "লিঙ্গ",
        "লিঙ্গ নির্বাচন করুন",
        "পুরুষ (Male)",
        "মহিলা (Female)",
        "অন্যান্য (Other)",
        "বৈবাহিক অবস্থা",
        "অবিবাহিত (Unmarried)",
        "বিবাহিত (Married)",
        "মোবাইল নম্বর",
        "ইমেল ঠিকানা (Email)",
        "ইমেল ঠিকানা",
        "বর্তমান ঠিকানা",
        "স্থায়ী ঠিকানা",
        "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা",
        "ডিপার্টমেন্ট (Department)",
        "ডিপার্টমেন্ট নির্বাচন করুন",
        "সেমিস্টার",
        "সেমিস্টার নির্বাচন করুন",
        "১ম সেমিস্টার",
        "২য় সেমিস্টার",
        "৩য় সেমিস্টার",
        "৪র্থ সেমিস্টার",
        "৫ম সেমিস্টার",
        "৬ষ্ঠ সেমিস্টার",
        "৭ম সেমিস্টার",
        "৮ম সেমিস্টার",
        "শিফট",
        "১ম শিফট",
        "২য় শিফট",
        "শিফট নির্বাচন করুন",
        "সেকশন (Section)",
        "সেকশন",
        "সেকশন নির্বাচন করুন",
        "বোর্ড রোল নম্বর (Roll)",
        "যেমন: ৬৪৮১২৩",
        "রেজিস্ট্রেশন নম্বর (Registration)",
        "যেমন: ১৭৫৬৪২",
        "সংরক্ষণ করুন",
        "প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!",
        "লগআউট করুন",
        "ডিলিট একাউন্ট",
        "তথ্য প্রদান করা হয়নি",
        "ডিভাইস জিপিএস থেকে ঠিকানা সনাক্ত করা হচ্ছে...",
        "অটো জিপিএস ঠিকানা পেতে পারমিশন অনুমোদন করুন",
        "অটো ডিভাইস জিপিএস ঠিকানা",
        "ডিভাইসের আসল জিপিএস থেকে ঠিকানা খোঁজা হচ্ছে...",
        "অ্যান্ড্রয়েড জিপিএস পারমিশন অনুমোদন করুন",
        "অনলাইনে আসলে ও অবস্থান পরিবর্তনে অটো সিঙ্ক হয়",
        "সাইন আউট",
        "সাইন আউট করুন",
        "অ্যাকাউন্ট ডিলিট করুন",
        "অ্যাকাউন্ট মুছে ফেলতে চান?",
        "আপনার অ্যাকাউন্ট, প্রোফাইল তথ্য এবং সমস্ত সেশন ডাটাবেস থেকে স্থায়ীভাবে মুছে যাবে। এই কাজটি পূর্বাবস্থায় ফেরানো যাবে না।",
        "ডিলিট করুন",
        "নির্বাচন করুন",
        "নির্বাচন করতে ট্যাপ করুন",
        "তথ্য সংরক্ষণ করুন",

        // Errors & Sign-in Feedback
        "করণীয়:",
        "ইন্টারনেট সংযোগ নেই!",
        "অনুরোধ সম্পূর্ণ করা যায়নি!",
        "ওয়েব সেটআপ সম্পূর্ণ হয়নি",
        "সাইন ইন বন্ধ আছে",
        "এই Google অ্যাকাউন্ট অনুমোদিত নয়",
        "এডমিন রিকোয়েস্ট পেন্ডিং রয়েছে",
        "এডমিন রিকোয়েস্ট প্রত্যাখ্যাত হয়েছে",
        "আপনার ডিভাইসে সক্রিয় ইন্টারনেট সংযোগ পাওয়া যায়নি। অ্যাপটির সমস্ত সুবিধা পেতে ইন্টারনেট কানেকশন প্রয়োজন।",
        "আপনার অনুরোধটি সম্পূর্ণ করা সম্ভব হয়নি। প্রবেশকৃত জিমেইল অ্যাকাউন্টে সমস্যা থাকতে পারে অথবা নেটওয়ার্ক ও সার্ভার সংযোগে বিলম্ব হচ্ছে।",
        "Firebase কনফিগারেশন চেক করুন। Google sign-in provider এবং নেটওয়ার্ক সেটিংস যাচাই করুন।",
        "এডমিন এই ধরনের সাইন ইন সাময়িকভাবে বন্ধ রেখেছেন। পরে আবার চেষ্টা করুন অথবা এডমিনের সাথে যোগাযোগ করুন।",
        "শুধু Firebase Firestore-এর admins কালেকশনে থাকা অনুমোদিত email দিয়ে এডমিন হিসেবে প্রবেশ করা যাবে।",
        "আপনার এডমিন অ্যাক্সেস রিকোয়েস্টটি পেন্ডিং রয়েছে। পূর্বে অনুমোদিত কোনো এডমিন আপনার ইমেইলটি অনুমোদন (Approve) করলে আপনি এডমিন হিসেবে প্রবেশ করতে পারবেন।",
        "আপনার এডমিন অ্যাক্সেস রিকোয়েস্টটি এডমিন কর্তৃক বাতিল বা প্রত্যাখ্যান (Decline) করা হয়েছে।",
        "আপনার ডিভাইসের Wi-Fi অথবা মোবাইল ডাটা চালু আছে কিনা চেক করুন।",
        "ইন্টারনেট সংযোগ সক্রিয় করে পুনরায় চেষ্টা করুন।",
        "আপনার জিমেইল অ্যাকাউন্টটি সঠিক এবং নিবন্ধিত কিনা নিশ্চিত করুন।",
        "নেটওয়ার্ক সংযোগ পরীক্ষা করে কিছুক্ষণ পর পুনরায় চেষ্টা করুন।",
        "Firebase Project settings থেকে Web App config কপি করুন।",
        "Authentication-এ Google provider এবং Hosting domain অনুমোদন করুন।",
        "পরিবর্তনের পর ওয়েব অ্যাপটি আবার build ও deploy করুন।",
        "এডমিন এই রোলের সাইন ইন অনুমতি বন্ধ রেখেছেন।",
        "এডমিনের সাথে যোগাযোগ করুন এবং সাইন ইন চালু করতে অনুরোধ করুন।",
        "Firebase Console-এ Firestore Database > admins কালেকশনে আপনার email ডকুমেন্ট হিসেবে যোগ করুন।",
        "অনুমোদিত ইমেইল অ্যাকাউন্ট দিয়ে পুনরায় চেষ্টা করুন।",
        "বর্তমান অনুমোদিত এডমিনের সাথে যোগাযোগ করুন।",
        "এডমিনকে অ্যাপের মেনু স্ক্রিনের 'এডমিন রিকোয়েস্ট অনুমোদন' প্যানেল থেকে আপনার ইমেইলটি Approve করতে বলুন।",
        "প্রয়োজনে বিদ্যমান অনুমোদিত এডমিনের সাথে কথা বলুন।",
        "গুগল সাইন-ইন ব্যর্থ হয়েছে",
        "সাইন-ইন শুরু করা যায়নি",
        "ইমেইল সাইন-ইন ব্যর্থ হয়েছে",
        "Firebase কনফিগারেশন অনুপস্থিত",
        "জিপিএস লোকেশন",
    )

    fun preloadCacheSync() {
        if (!cacheLoaded) {
            cacheLoaded = true
            val cached = loadCachedTranslations()
            if (cached.isNotEmpty()) {
                dynamicDict.putAll(cached)
            }
        }
    }

    /**
     * Dynamically loads & syncs the online dictionary from cloud/internet on first app launch or language switch.
     */
    suspend fun initializeOnlineDictionary() {
        syncMutex.withLock {
            withContext(Dispatchers.Main) {
                preloadCacheSync()
                isSyncingOnline = true
            }
            try {
                // 1. Check cloud overrides in Firestore config/language_dictionary
                val cloudEntries = fetchOnlineDictionaryFromCloud()
                if (cloudEntries.isNotEmpty()) {
                    saveCachedTranslations(cloudEntries)
                    withContext(Dispatchers.Main) {
                        dynamicDict.putAll(cloudEntries)
                    }
                }

                // 2. Fetch any missing phrases dynamically from the online translation dictionary API
                val existingKeys = withContext(Dispatchers.Main) { dynamicDict.keys.toSet() }
                val missing = appBengaliCatalog.filter { !existingKeys.contains(it.trim()) }
                if (missing.isNotEmpty()) {
                    val fetched = fetchOnlineTranslations(missing)
                    if (fetched.isNotEmpty()) {
                        saveCachedTranslations(fetched)
                        withContext(Dispatchers.Main) {
                            dynamicDict.putAll(fetched)
                        }
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSyncingOnline = false
                }
            }
        }
    }

    private fun scheduleDynamicTranslation(text: String) {
        val key = text.trim()
        if (key.isBlank() || !containsBengali(key)) return
        synchronized(pendingTranslations) {
            if (!pendingTranslations.add(key)) return
        }
        moduleScope.launch {
            try {
                val fetched = fetchOnlineTranslations(listOf(key))
                if (fetched.isNotEmpty()) {
                    saveCachedTranslations(fetched)
                    withContext(Dispatchers.Main) {
                        dynamicDict.putAll(fetched)
                    }
                }
            } finally {
                synchronized(pendingTranslations) {
                    pendingTranslations.remove(key)
                }
            }
        }
    }

    private fun containsBengali(text: String): Boolean {
        for (ch in text) {
            if (ch in '\u0980'..'\u09FF') return true
        }
        return false
    }

    fun convertBengaliDigitsToAscii(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            when (ch) {
                '০' -> sb.append('0')
                '১' -> sb.append('1')
                '২' -> sb.append('2')
                '৩' -> sb.append('3')
                '৪' -> sb.append('4')
                '৫' -> sb.append('5')
                '৬' -> sb.append('6')
                '৭' -> sb.append('7')
                '৮' -> sb.append('8')
                '৯' -> sb.append('9')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    // Standard UI keys delegating exclusively to the online dictionary module
    fun languageBtn(lang: AppLanguage): String = translate("ভাষা", lang)
    fun registrationBtn(lang: AppLanguage): String = translate("এগিয়ে যান", lang)
    fun quote(lang: AppLanguage): String = translate(
        "ফোকাস মানে হলো হাজারটা ভালো আইডিয়াকে 'না' বলা। আর সেই একটা 'হ্যাঁ'-এর জন্য শেষ পর্যন্ত লড়ে যাওয়া।",
        lang,
    )
    fun termsAndConditions(lang: AppLanguage): String = translate("টার্মস অ্যান্ড কন্ডিশন", lang)
    fun signInTitle(lang: AppLanguage): String = translate("সাইন ইন করুন", lang)
    fun signInSubtitle(lang: AppLanguage): String = translate("Google অথবা ইমেইল অ্যাকাউন্ট\nব্যবহার করে সাইন ইন করুন", lang)
    fun guestMode(lang: AppLanguage): String = translate("গেস্ট মোড", lang)
    fun languageDialogTitle(lang: AppLanguage): String = translate("ভাষা নির্বাচন করুন", lang)
    fun closeAppDialogTitle(lang: AppLanguage): String = translate("অ্যাপ বন্ধ করুন", lang)
    fun closeAppDialogMessage(lang: AppLanguage): String = translate("আপনি কি অ্যাপটি বন্ধ করতে চান?", lang)
    fun yes(lang: AppLanguage): String = translate("হ্যাঁ", lang)
    fun no(lang: AppLanguage): String = translate("না", lang)
    fun cancel(lang: AppLanguage): String = translate("বাতিল", lang)

    /**
     * Translates any Bengali string dynamically from the online dictionary cache (triggers online fetch + UI recomposition if unseen).
     */
    fun translate(text: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI || text.isBlank()) return text
        if (!cacheLoaded) preloadCacheSync()

        val trimmed = text.trim()
        val cached = dynamicDict[trimmed]
        if (!cached.isNullOrBlank()) {
            return convertBengaliDigitsToAscii(cached)
        }

        if (!containsBengali(trimmed)) {
            return convertBengaliDigitsToAscii(text)
        }

        // If multi-line text has individual lines already translated, combine cleanly
        if (trimmed.contains('\n')) {
            val lines = trimmed.split('\n')
            val allLinesCached = lines.all { line ->
                val lt = line.trim()
                lt.isBlank() || !containsBengali(lt) || !dynamicDict[lt].isNullOrBlank()
            }
            if (allLinesCached) {
                return lines.joinToString("\n") { line ->
                    val lt = line.trim()
                    if (lt.isBlank()) ""
                    else convertBengaliDigitsToAscii(dynamicDict[lt] ?: lt)
                }
            }
        }

        // Queue dynamic online translation for any unseen Bengali text; Compose recomposes automatically when ready
        scheduleDynamicTranslation(trimmed)
        return convertBengaliDigitsToAscii(text)
    }

    suspend fun translateWithAiModel(text: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI || text.isBlank()) return text
        if (!cacheLoaded) preloadCacheSync()
        val trimmed = text.trim()
        dynamicDict[trimmed]?.let { return convertBengaliDigitsToAscii(it) }

        val fetched = fetchOnlineTranslations(listOf(trimmed))
        if (fetched.isNotEmpty()) {
            saveCachedTranslations(fetched)
            withContext(Dispatchers.Main) {
                dynamicDict.putAll(fetched)
            }
            fetched[trimmed]?.let { return convertBengaliDigitsToAscii(it) }
        }
        return convertBengaliDigitsToAscii(trimmed)
    }
}

/**
 * Convenient extension to translate strings dynamically throughout Composables & code.
 */
fun String.localized(lang: AppLanguage): String = AppStrings.translate(this, lang)

@Composable
fun String.localized(): String {
    val lang = LocalAppLanguage.current
    return AppStrings.translate(this, lang)
}
