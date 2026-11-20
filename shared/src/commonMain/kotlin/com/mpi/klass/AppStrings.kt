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
        "ছেড়ে দিন",
        "মাঝখানে",

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
        "২. যোগাযোগের তথ্য (Contact Information)",
        "৩. যোগাযোগের তথ্য (Contact Information)",
        "৩. ঠিকানার তথ্য (Address Information)",
        "৪. ঠিকানার তথ্য (Address Information)",
        "৪. একাডেমিক তথ্য (Academic Information)",
        "৫. একাডেমিক তথ্য (Academic Information)",
        "২. একাডেমিক তথ্য (Academic Information)",
        "পূর্ণ নাম (Full Name)",
        "শিক্ষার্থীর পূর্ণ নাম (Full Name)",
        "শিক্ষকের পূর্ণ নাম (Full Name)",
        "এডমিনের পূর্ণ নাম (Full Name)",
        "শিক্ষকের নাম, ডিপার্টমেন্ট বা ইমেইল খুঁজুন...",
        "মোট শিক্ষক",
        "জন",
        "এখনো কোনো শিক্ষক লগইন করেননি",
        "শিক্ষক হিসেবে অ্যাপে সাইন ইন করলে তাদের তালিকা এখানে মেসেঞ্জার কার্ড আকারে দেখা যাবে।",
        "কোনো শিক্ষক পাওয়া যায়নি",
        "সক্রিয়",
        "তালিকা রিফ্রেশ করুন",
        "তথ্য লোড হচ্ছে...",
        "এইমাত্র",
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
        "নন টেক",
        "শিক্ষক মুছে ফেলুন",
        "আপনি কি নিশ্চিতভাবে এই শিক্ষককে ডাটাবেস থেকে স্থায়ীভাবে মুছে ফেলতে চান?",
        "শিক্ষককে ডাটাবেস থেকে মুছে ফেলা হয়েছে!",
        "শিক্ষক মুছে ফেলা ব্যর্থ হয়েছে। আবার চেষ্টা করুন।",
        "বইয়ের নাম, কোড, ডিপার্টমেন্ট বা সেমিস্টার খুঁজুন...",
        "এড বই",
        "নতুন বই যুক্ত করুন",
        "বই এডিট করুন",
        "মোট বই",
        "টি",
        "বইয়ের নাম",
        "বইয়ের নাম লিখুন",
        "সাবজেক্ট কোড",
        "যেমন: ২৫৯১১",
        "ডিপার্টমেন্ট → সেমিস্টার (একাধিক হতে পারে)",
        "এক বা একাধিক ডিপার্টমেন্ট নির্বাচন করে সেমিস্টার বেছে নিন",
        "থিওরি ক্লাস (সপ্তাহে)",
        "প্র্যাকটিকেল ক্লাস (সপ্তাহে)",
        "ক্রেডিট",
        "থিওরি",
        "প্র্যাকটিকেল",
        "সপ্তাহে",
        "বই মুছে ফেলুন",
        "আপনি কি নিশ্চিতভাবে এই বইটি লাইব্রেরি থেকে স্থায়ীভাবে মুছে ফেলতে চান?",
        "বই সফলভাবে সংরক্ষিত হয়েছে!",
        "বই সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।",
        "বই মুছে ফেলা হয়েছে!",
        "বই মুছে ফেলা ব্যর্থ হয়েছে। আবার চেষ্টা করুন।",
        "অনুগ্রহ করে বইয়ের নাম ও সাবজেক্ট কোড লিখুন",
        "কমপক্ষে একটি ডিপার্টমেন্ট ও সেমিস্টার নির্বাচন করুন",
        "এখনো কোনো বই যুক্ত করা হয়নি",
        "নতুন বই যোগ করতে উপরের 'এড বই' বাটনে ক্লিক করুন।",
        "কোনো বই পাওয়া যায়নি",
        "সব",
        "সবগুলো",
        "ডিপার্টমেন্ট",
        "সব ফিল্টার মুছুন",
        "মুছুন",
        "কোড",
        "শিক্ষকের তথ্য",
        "যোগ করুন",
        "আপডেট করুন",
        "মোট নিবন্ধিত শিক্ষক তালিকা",
        "ডিপার্টমেন্ট ও সেমিস্টার ভিত্তিক বই সংগ্রহ",
        "নতুন নোটিশ আসলে এখানে রিয়েল-টাইম আপডেট দেখা যাবে",
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
        "সংরক্ষণ করা হচ্ছে...",
        "প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!",
        "তথ্য সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।",
        "লগআউট করুন",
        "ডিলিট একাউন্ট",
        "মুছে ফেলা হচ্ছে...",
        "আপনি কি নিশ্চিতভাবে আপনার অ্যাকাউন্টটি চিরতরে ডিলিট করতে চান? এটি করলে আপনার সকল তথ্য মুছে যাবে।",
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
        "রক্তের গ্রুপ",
    )

    private val staticEnglishDict: Map<String, String> = mapOf(
        // Welcome & Common
        "ভাষা" to "Language",
        "ভাষা নির্বাচন করুন" to "Select Language",
        "থিম নির্বাচন করুন" to "Select Theme",
        "এগিয়ে যান" to "Continue",
        "অ্যাপ সম্পর্কে" to "About App",
        "ফোকাস মানে হলো হাজারটা ভালো আইডিয়াকে 'না' বলা। আর সেই একটা 'হ্যাঁ'-এর জন্য শেষ পর্যন্ত লড়ে যাওয়া।" to "Focus is about saying no to a thousand good ideas, and fighting until the end for that one yes.",
        "টার্মস অ্যান্ড কন্ডিশন" to "Terms & Conditions",
        "ময়মনসিংহ সরকারি\nপলিটেকনিক ইনস্টিটিউট" to "Mymensingh Government\nPolytechnic Institute",
        "ময়মনসিংহ সরকারি পলিটেকনিক ইনস্টিটিউট" to "Mymensingh Polytechnic Institute",
        "অ্যাপ থেকে বের হতে আবার ব্যাক প্রেস করুন" to "Press back again to exit the app",
        "বন্ধ করুন" to "Close",
        "ভার্সন ১.০.০" to "Version 1.0.0",
        "অ্যাপের সকল ফিচারসমূহ" to "All App Features",
        "শিক্ষার্থী, শিক্ষক ও প্রশাসনের সকল একাডেমিক কার্যক্রম সহজ, দ্রুত ও ডিজিটাল করার জন্য নির্মিত স্মার্ট ক্লাসরুম প্ল্যাটফর্ম।" to "A smart classroom platform built to simplify and digitize academic activities for students, teachers, and admins.",
        "অ্যাপ বন্ধ করুন" to "Close App",
        "আপনি কি অ্যাপটি বন্ধ করতে চান?" to "Are you sure you want to close the app?",
        "হ্যাঁ" to "Yes",
        "না" to "No",
        "বাতিল" to "Cancel",
        "গেস্ট মোড" to "Guest Mode",
        "গেস্ট" to "Guest",
        "গেস্ট ইউজার" to "Guest User",
        "সাইন ইন" to "Sign In",
        "সাইন ইন করুন" to "Sign In",
        "Google অথবা ইমেইল অ্যাকাউন্ট\nব্যবহার করে সাইন ইন করুন" to "Sign in using your Google or email account",

        // Role Selection & Dashboards
        "শিক্ষার্থী" to "Student",
        "শিক্ষক" to "Teacher",
        "এডমিন" to "Admin",
        "এডমিনিস্ট্রেটর" to "Administrator",
        "আপনার অ্যাকাউন্ট ক্যাটাগরি নির্বাচন করুন। দয়া করে ভুল তথ্য দেওয়া থেকে বিরত থাকুন।" to "Select your account category. Please provide accurate information.",
        "এডমিন ড্যাশবোর্ড" to "Admin Dashboard",
        "শিক্ষক ড্যাশবোর্ড" to "Teacher Dashboard",
        "শিক্ষার্থী ড্যাশবোর্ড" to "Student Dashboard",

        // Navigation, Theme & Notification
        "হোম" to "Home",
        "মেনু" to "Menu",
        "প্রোফাইল" to "Profile",
        "রুটিন" to "Routine",
        "নোটিফিকেশন" to "Notifications",
        "AI সহকারী" to "AI Assistant",
        "লাইট" to "Light",
        "ডার্ক" to "Dark",
        "ডিফল্ট" to "Default",
        "অটো" to "Auto",
        "কোনো নতুন নোটিফিকেশন নেই" to "No New Notifications",
        "খুলতে ছেড়ে দিন" to "Release to open",
        "মাঝখানে আনুন" to "Drag to center",
        "ছেড়ে দিন" to "Release",
        "মাঝখানে" to "Center",

        // Routine Table
        "দিন" to "Day",
        "সময়" to "Time",
        "বিষয়" to "Subject",
        "কোড" to "Code",
        "কক্ষ" to "Room",
        "রুম" to "Room",
        "রবিবার" to "Sunday",
        "সোমবার" to "Monday",
        "মঙ্গলবার" to "Tuesday",
        "বুধবার" to "Wednesday",
        "বৃহস্পতিবার" to "Thursday",

        // AI Assistant (Zed)
        "স্বাগতম! আমি আপনার Klass Gemini AI স্টাডি সহকারী। ক্লাস নোটের সারাংশ, গণিত-বিজ্ঞান সমাধান, কুইজ প্রস্তুতি বা যেকোনো পড়াশোনার বিষয়ে আমাকে প্রশ্ন করতে পারেন।" to "Welcome! I am Zed, your custom MPI Klass AI assistant. Ask me about books, teachers, classrooms, routine, or mpi.politech.gov.bd!",
        "ক্লাস নোটের সারাংশ তৈরি করুন" to "Summarize Class Notes",
        "দয়া করে আমার পড়া বিষয়ের একটি পরিষ্কার ও পয়েন্ট ভিত্তিক সারাংশ তৈরি করুন:" to "Please create a clear bullet-point summary of my study topic:",
        "কঠিন বিষয় সহজে ব্যাখ্যা" to "Explain Hard Topic",
        "একটি জটিল বৈজ্ঞানিক বা গাণিতিক বিষয় সহজ উদাহরণ দিয়ে ব্যাখ্যা করুন:" to "Explain a complex scientific or math concept with simple examples:",
        "অনুশীলনী প্রশ্ন তৈরি" to "Generate Practice Quiz",
        "পরীক্ষার প্রস্তুতির জন্য ৫টি গুরুত্বপূর্ণ প্রশ্ন এবং উত্তর তৈরি করুন:" to "Create 5 important practice questions with answers for exam prep:",
        "পড়ার রুটিন তৈরি করুন" to "Build Study Plan",
        "প্রতিদিনের পড়ার একটি কার্যকরী ও ভারসাম্যপূর্ণ সময়সূচি সাজিয়ে দিন।" to "Build an effective and balanced daily study schedule for me.",
        "AI সংযোগে ত্রুটি হয়েছে। অনুগ্রহ করে আবার চেষ্টা করুন।" to "AI connection failed. Please try again.",
        "AI প্রতিক্রিয়া সময়সীমা অতিক্রান্ত হয়েছে (Timeout). অনুগ্রহ করে আবার চেষ্টা করুন।" to "AI response timed out. Please try again.",
        "ফিরে যান" to "Back",
        "Gemini AI সহকারী" to "Zed AI Assistant",
        "নতুন চ্যাট শুরু হয়েছে। আপনি কী জানতে চান?" to "New chat started. How can Zed help you?",
        "নতুন চ্যাট" to "New Chat",
        "Gemini চিন্তা করছে..." to "Zed is thinking...",
        "আবার চেষ্টা করুন" to "Retry",
        "দ্রুত জিজ্ঞাসা করুন:" to "Quick Prompts:",
        "AI-কে যেকোনো প্রশ্ন করুন..." to "Ask Zed anything...",
        "পাঠান" to "Send",

        // Menu Screen
        "প্রোফাইল সেটিংস" to "Profile Settings",
        "আপনার অ্যাকাউন্ট ও ব্যক্তিগত তথ্য ম্যানেজ করুন" to "Manage your account & personal information",
        "বুকস লাইব্রেরি" to "Books Library",
        "সেমিস্টার অনুযায়ী ডিপার্টমেন্টাল বই ও লেকচার নোট" to "Departmental books & curriculum by semester",
        "ক্লাসরুম" to "Classrooms & Labs",
        "অনলাইন ক্লাস ও সেমিস্টার রুটিন তথ্য" to "Rooms, labs & room schedules",
        "শিক্ষকবৃন্দ" to "Teachers Directory",
        "সম্মানিত শিক্ষকগণের তালিকা ও যোগাযোগের তথ্য" to "Faculty list & contact information",
        "নোটিশ বোর্ড" to "Notice Board",
        "ইনস্টিটিউট ও ডিপার্টমেন্টের জরুরি আপডেট ও নোটিশ" to "Institute alerts, today's classes & notices",
        "নতুন সাইন ইন নিয়ন্ত্রণ" to "Sign-In Access Control",
        "অ্যাডমিন, শিক্ষক ও শিক্ষার্থী সাইন ইন পারমিশন" to "Admin, teacher & student login permissions",
        "ক্লিক করে সেটিংস প্যানেল খুলুন" to "Tap to open access settings",
        "সাইন ইন বন্ধ রাখলে শুধুমাত্র পূর্বের রেজিস্টার্ড ব্যবহারকারীরা লগইন করতে পারবেন। নতুনদের জন্য প্রবেশ বন্ধ থাকবে।" to "When turned off, only previously registered users can sign in; new registrations are paused.",
        "এডমিন সাইন ইন" to "Admin Sign-In",
        "শিক্ষক সাইন ইন" to "Teacher Sign-In",
        "শিক্ষার্থী সাইন ইন" to "Student Sign-In",
        "এডমিন রিকোয়েস্ট ও অনুমোদন" to "Admin Requests & Approvals",
        "টি আবেদন অনুমোদনের অপেক্ষায়" to "requests awaiting approval",
        "কোনো পেন্ডিং আবেদন নেই" to "No pending requests",
        "বর্তমানে কোনো পেন্ডিং এডমিন অ্যাক্সেস রিকোয়েস্ট নেই।" to "There are currently no pending admin access requests.",
        "অ্যাপ থিম পরিবর্তন" to "App Theme",
        "লাইট, ডার্ক অথবা অটো থিম সিলেক্ট করুন" to "Choose Light, Dark, or Auto theme",
        "ক্লিক করে থিম সেটিংস খুলুন" to "Tap to choose app theme",
        "চালু আছে" to "Enabled",
        "বন্ধ রয়েছে" to "Disabled",
        "এডমিন রিকোয়েস্টকারী" to "Admin Applicant",
        "অপেক্ষমাণ" to "Pending",
        "অনুমোদন করুন" to "Approve",
        "প্রত্যাখ্যান করুন" to "Decline",

        // Profile, Edit Profile, Teachers Directory & Books Library
        "গ্যালারি থেকে ছবি আপলোড" to "Upload photo from gallery",
        "সেভ করুন" to "Save",
        "নাম এডিট করুন" to "Edit Name",
        "এডিট" to "Edit",
        "এডিট করুন" to "Edit Profile",
        "প্রোফাইল দেখুন" to "View Profile",
        "শিক্ষার্থী প্রোফাইল" to "Student Profile",
        "শিক্ষক প্রোফাইল" to "Teacher Profile",
        "এডমিন প্রোফাইল" to "Admin Profile",
        "আপনার প্রোফাইল সম্পাদনা করুন" to "Edit Your Profile",
        "১. ব্যক্তিগত তথ্য (Personal Information)" to "1. Personal Information",
        "২. পারিবারিক তথ্য (Family Information)" to "2. Family Information",
        "২. যোগাযোগের তথ্য (Contact Information)" to "2. Contact Information",
        "৩. যোগাযোগের তথ্য (Contact Information)" to "3. Contact Information",
        "৩. ঠিকানার তথ্য (Address Information)" to "3. Address Information",
        "৪. ঠিকানার তথ্য (Address Information)" to "4. Address Information",
        "৪. একাডেমিক তথ্য (Academic Information)" to "4. Academic Information",
        "৫. একাডেমিক তথ্য (Academic Information)" to "5. Academic Information",
        "২. একাডেমিক তথ্য (Academic Information)" to "2. Academic Information",
        "পূর্ণ নাম (Full Name)" to "Full Name",
        "শিক্ষার্থীর পূর্ণ নাম (Full Name)" to "Student Full Name",
        "শিক্ষকের পূর্ণ নাম (Full Name)" to "Teacher Full Name",
        "এডমিনের পূর্ণ নাম (Full Name)" to "Admin Full Name",
        "শিক্ষকের নাম, ডিপার্টমেন্ট বা ইমেইল খুঁজুন..." to "Search teacher name, department or email...",
        "মোট শিক্ষক" to "Total Teachers",
        "জন" to "teachers",
        "এখনো কোনো শিক্ষক লগইন করেননি" to "No teachers signed in yet",
        "শিক্ষক হিসেবে অ্যাপে সাইন ইন করলে তাদের তালিকা এখানে মেসেঞ্জার কার্ড আকারে দেখা যাবে।" to "When teachers sign in, their cards will appear here.",
        "কোনো শিক্ষক পাওয়া যায়নি" to "No matching teachers found",
        "সক্রিয়" to "Active",
        "তালিকা রিফ্রেশ করুন" to "Refresh list",
        "তথ্য লোড হচ্ছে..." to "Loading data...",
        "এইমাত্র" to "Just now",
        "আপনার পুরো নাম লিখুন" to "Enter your full name",
        "পিতার নাম (Father's Name)" to "Father's Name",
        "পিতার নাম" to "Father's Name",
        "পিতার নাম লিখুন" to "Enter father's name",
        "মাতার নাম (Mother's Name)" to "Mother's Name",
        "মাতার নাম" to "Mother's Name",
        "মাতার নাম লিখুন" to "Enter mother's name",
        "জন্ম তারিখ" to "Date of Birth",
        "জন্ম তারিখ নির্বাচন করুন" to "Select date of birth",
        "রক্তের গ্রুপ" to "Blood Group",
        "গ্রুপ নির্বাচন করুন" to "Select Blood Group",
        "ধর্ম" to "Religion",
        "ধর্ম নির্বাচন করুন" to "Select Religion",
        "ইসলাম" to "Islam",
        "হিন্দুধর্ম" to "Hinduism",
        "বৌদ্ধধর্ম" to "Buddhism",
        "খ্রিস্টধর্ম" to "Christianity",
        "অন্যান্য" to "Other",
        "লিঙ্গ (Gender)" to "Gender",
        "লিঙ্গ" to "Gender",
        "লিঙ্গ নির্বাচন করুন" to "Select Gender",
        "পুরুষ (Male)" to "Male",
        "মহিলা (Female)" to "Female",
        "অন্যান্য (Other)" to "Other",
        "বৈবাহিক অবস্থা" to "Marital Status",
        "অবিবাহিত (Unmarried)" to "Unmarried",
        "বিবাহিত (Married)" to "Married",
        "মোবাইল নম্বর" to "Mobile Number",
        "ইমেল ঠিকানা (Email)" to "Email Address",
        "ইমেল ঠিকানা" to "Email Address",
        "বর্তমান ঠিকানা" to "Current Address",
        "স্থায়ী ঠিকানা" to "Permanent Address",
        "গ্রাম/মহল্লা, ডাকঘর, উপজেলা, জেলা" to "Village/Road, Post Office, Upazila, District",
        "ডিপার্টমেন্ট (Department)" to "Department",
        "ডিপার্টমেন্ট নির্বাচন করুন" to "Select Department",
        "সেমিস্টার" to "Semester",
        "সেমিস্টার নির্বাচন করুন" to "Select Semester",
        "১ম সেমিস্টার" to "1st Semester",
        "২য় সেমিস্টার" to "2nd Semester",
        "৩য় সেমিস্টার" to "3rd Semester",
        "৪র্থ সেমিস্টার" to "4th Semester",
        "৫ম সেমিস্টার" to "5th Semester",
        "৬ষ্ঠ সেমিস্টার" to "6th Semester",
        "৭ম সেমিস্টার" to "7th Semester",
        "নন টেক" to "Non-Tech",
        "শিক্ষক মুছে ফেলুন" to "Delete Teacher",
        "আপনি কি নিশ্চিতভাবে এই শিক্ষককে ডাটাবেস থেকে স্থায়ীভাবে মুছে ফেলতে চান?" to "Are you sure you want to permanently remove this teacher from the database?",
        "শিক্ষককে ডাটাবেস থেকে মুছে ফেলা হয়েছে!" to "Teacher deleted from database!",
        "শিক্ষক মুছে ফেলা ব্যর্থ হয়েছে। আবার চেষ্টা করুন।" to "Failed to delete teacher. Please try again.",
        "বইয়ের নাম, কোড, ডিপার্টমেন্ট বা সেমিস্টার খুঁজুন..." to "Search book title, subject code, department or semester...",
        "এড বই" to "Add Book",
        "নতুন বই যুক্ত করুন" to "Add New Book",
        "বই এডিট করুন" to "Edit Book",
        "মোট বই" to "Total Books",
        "টি" to "items",
        "বইয়ের নাম" to "Book Title",
        "বইয়ের নাম লিখুন" to "Enter book title",
        "সাবজেক্ট কোড" to "Subject Code",
        "যেমন: ২৫৯১১" to "e.g. 25911",
        "ডিপার্টমেন্ট → সেমিস্টার (একাধিক হতে পারে)" to "Department -> Semesters (Multiple)",
        "এক বা একাধিক ডিপার্টমেন্ট নির্বাচন করে সেমিস্টার বেছে নিন" to "Select one or more departments and semesters",
        "থিওরি ক্লাস (সপ্তাহে)" to "Theory Classes / Week",
        "প্র্যাকটিকেল ক্লাস (সপ্তাহে)" to "Practical Classes / Week",
        "ক্রেডিট" to "Credit",
        "থিওরি" to "Theory",
        "প্র্যাকটিকেল" to "Practical",
        "সপ্তাহে" to "week",
        "বই মুছে ফেলুন" to "Delete Book",
        "আপনি কি নিশ্চিতভাবে এই বইটি লাইব্রেরি থেকে স্থায়ীভাবে মুছে ফেলতে চান?" to "Are you sure you want to permanently delete this book from the library?",
        "বই সফলভাবে সংরক্ষিত হয়েছে!" to "Book saved successfully!",
        "বই সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।" to "Failed to save book. Please try again.",
        "বই মুছে ফেলা হয়েছে!" to "Book deleted!",
        "বই মুছে ফেলা ব্যর্থ হয়েছে। আবার চেষ্টা করুন।" to "Failed to delete book. Please try again.",
        "অনুগ্রহ করে বইয়ের নাম ও সাবজেক্ট কোড লিখুন" to "Please enter both book title and subject code.",
        "কমপক্ষে একটি ডিপার্টমেন্ট ও সেমিস্টার নির্বাচন করুন" to "Please select at least one department and semester.",
        "এখনো কোনো বই যুক্ত করা হয়নি" to "No books added yet",
        "নতুন বই যোগ করতে উপরের 'এড বই' বাটনে ক্লিক করুন।" to "Tap 'Add Book' above to add a curriculum book.",
        "কোনো বই পাওয়া যায়নি" to "No matching books found",
        "সব" to "All",
        "সবগুলো" to "All Semesters",
        "ডিপার্টমেন্ট" to "Department",
        "সব ফিল্টার মুছুন" to "Clear All Filters",
        "মুছুন" to "Delete",
        "শিক্ষকের তথ্য" to "Teacher Details",
        "যোগ করুন" to "Add",
        "আপডেট করুন" to "Update",
        "মোট নিবন্ধিত শিক্ষক তালিকা" to "Registered Teachers Directory",
        "ডিপার্টমেন্ট ও সেমিস্টার ভিত্তিক বই সংগ্রহ" to "Curriculum books by department & semester",
        "নতুন নোটিশ আসলে এখানে রিয়েল-টাইম আপডেট দেখা যাবে" to "Today's classes & live notices appear here in real time",
        "শিফট" to "Shift",
        "১ম শিফট" to "1st Shift",
        "২য় শিফট" to "2nd Shift",
        "শিফট নির্বাচন করুন" to "Select Shift",
        "সেকশন (Section)" to "Group / Section",
        "সেকশন" to "Group / Section",
        "সেকশন নির্বাচন করুন" to "Select Group / Section",
        "বোর্ড রোল নম্বর (Roll)" to "Board Roll Number",
        "যেমন: ৬৪৮১২৩" to "e.g. 648123",
        "রেজিস্ট্রেশন নম্বর (Registration)" to "Registration Number",
        "যেমন: ১৭৫৬৪২" to "e.g. 175642",
        "সংরক্ষণ করুন" to "Save Changes",
        "সংরক্ষণ করা হচ্ছে..." to "Saving...",
        "প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে!" to "Profile saved successfully!",
        "তথ্য সংরক্ষণ ব্যর্থ হয়েছে। আবার চেষ্টা করুন।" to "Failed to save profile. Please try again.",
        "লগআউট করুন" to "Sign Out",
        "ডিলিট একাউন্ট" to "Delete Account",
        "মুছে ফেলা হচ্ছে..." to "Deleting...",
        "আপনি কি নিশ্চিতভাবে আপনার অ্যাকাউন্টটি চিরতরে ডিলিট করতে চান? এটি করলে আপনার সকল তথ্য মুছে যাবে।" to "Are you sure you want to permanently delete your account? All your profile data will be erased.",
        "তথ্য প্রদান করা হয়নি" to "Not Provided",
        "ডিভাইস জিপিএস থেকে ঠিকানা সনাক্ত করা হচ্ছে..." to "Detecting address from device GPS...",
        "অটো জিপিএস ঠিকানা পেতে পারমিশন অনুমোদন করুন" to "Grant location permission for automatic GPS address",
        "অটো ডিভাইস জিপিএস ঠিকানা" to "Auto Device GPS Address",
        "ডিভাইসের আসল জিপিএস থেকে ঠিকানা খোঁজা হচ্ছে..." to "Fetching location via GPS...",
        "অ্যান্ড্রয়েড জিপিএস পারমিশন অনুমোদন করুন" to "Please allow GPS permission",
        "অনলাইনে আসলে ও অবস্থান পরিবর্তনে অটো সিঙ্ক হয়" to "Auto-syncs when online or location changes",
        "সাইন আউট" to "Sign Out",
        "সাইন আউট করুন" to "Sign Out",
        "অ্যাকাউন্ট ডিলিট করুন" to "Delete Account",
        "অ্যাকাউন্ট মুছে ফেলতে চান?" to "Delete Account Permanently?",
        "আপনার অ্যাকাউন্ট, প্রোফাইল তথ্য এবং সমস্ত সেশন ডাটাবেস থেকে স্থায়ীভাবে মুছে যাবে। এই কাজটি পূর্বাবস্থায় ফেরানো যাবে না।" to "Your account, profile details, and session will be permanently removed. This cannot be undone.",
        "ডিলিট করুন" to "Delete",
        "নির্বাচন করুন" to "Select",
        "নির্বাচন করতে ট্যাপ করুন" to "Tap to select",
        "তথ্য সংরক্ষণ করুন" to "Save Data",

        // Errors & Sign-in Feedback
        "করণীয়:" to "What to do:",
        "ইন্টারনেট সংযোগ নেই!" to "No Internet Connection!",
        "অনুরোধ সম্পূর্ণ করা যায়নি!" to "Request Could Not Be Completed!",
        "ওয়েব সেটআপ সম্পূর্ণ হয়নি" to "Firebase Configuration Incomplete",
        "সাইন ইন বন্ধ আছে" to "Sign-In Currently Disabled",
        "এই Google অ্যাকাউন্ট অনুমোদিত নয়" to "Google Account Not Authorized",
        "এডমিন রিকোয়েস্ট পেন্ডিং রয়েছে" to "Admin Request Pending",
        "এডমিন রিকোয়েস্ট প্রত্যাখ্যাত হয়েছে" to "Admin Request Declined",
        "আপনার ডিভাইসে সক্রিয় ইন্টারনেট সংযোগ পাওয়া যায়নি। অ্যাপটির সমস্ত সুবিধা পেতে ইন্টারনেট কানেকশন প্রয়োজন।" to "No active internet connection was detected on your device. Please connect to the internet to continue.",
        "আপনার অনুরোধটি সম্পূর্ণ করা সম্ভব হয়নি। প্রবেশকৃত জিমেইল অ্যাকাউন্টে সমস্যা থাকতে পারে অথবা নেটওয়ার্ক ও সার্ভার সংযোগে বিলম্ব হচ্ছে।" to "Unable to complete sign-in. There may be an issue with the Google account or a temporary network delay.",
        "Firebase কনফিগারেশন চেক করুন। Google sign-in provider এবং নেটওয়ার্ক সেটিংস যাচাই করুন।" to "Please verify your Firebase configuration, Google sign-in provider, and network settings.",
        "এডমিন এই ধরনের সাইন ইন সাময়িকভাবে বন্ধ রেখেছেন। পরে আবার চেষ্টা করুন অথবা এডমিনের সাথে যোগাযোগ করুন।" to "Admin has temporarily disabled sign-in for this role. Please try again later or contact an administrator.",
        "শুধু Firebase Firestore-এর admins কালেকশনে থাকা অনুমোদিত email দিয়ে এডমিন হিসেবে প্রবেশ করা যাবে।" to "Only authorized emails approved in the admins collection can sign in as Administrator.",
        "আপনার এডমিন অ্যাক্সেস রিকোয়েস্টটি পেন্ডিং রয়েছে। পূর্বে অনুমোদিত কোনো এডমিন আপনার ইমেইলটি অনুমোদন (Approve) করলে আপনি এডমিন হিসেবে প্রবেশ করতে পারবেন।" to "Your admin access request is pending approval. Once an existing administrator approves your email, you will be able to sign in.",
        "আপনার এডমিন অ্যাক্সেস রিকোয়েস্টটি এডমিন কর্তৃক বাতিল বা প্রত্যাখ্যান (Decline) করা হয়েছে।" to "Your admin access request was declined by an administrator.",
        "আপনার ডিভাইসের Wi-Fi অথবা মোবাইল ডাটা চালু আছে কিনা চেক করুন।" to "Check that Wi-Fi or mobile data is turned on.",
        "ইন্টারনেট সংযোগ সক্রিয় করে পুনরায় চেষ্টা করুন।" to "Reconnect to the internet and try again.",
        "আপনার জিমেইল অ্যাকাউন্টটি সঠিক এবং নিবন্ধিত কিনা নিশ্চিত করুন।" to "Ensure your Google account is valid and active.",
        "নেটওয়ার্ক সংযোগ পরীক্ষা করে কিছুক্ষণ পর পুনরায় চেষ্টা করুন।" to "Check your network connection and retry shortly.",
        "এডমিন এই রোলের সাইন ইন অনুমতি বন্ধ রেখেছেন।" to "Administrator disabled new sign-ins for this role.",
        "এডমিনের সাথে যোগাযোগ করুন এবং সাইন ইন চালু করতে অনুরোধ করুন।" to "Contact your institute administrator to enable sign-in.",
        "বর্তমান অনুমোদিত এডমিনের সাথে যোগাযোগ করুন।" to "Contact a current authorized administrator.",
        "এডমিনকে অ্যাপের মেনু স্ক্রিনের 'এডমিন রিকোয়েস্ট অনুমোদন' প্যানেল থেকে আপনার ইমেইলটি Approve করতে বলুন।" to "Ask an admin to approve your email from the Admin Requests panel in the Menu.",
        "প্রয়োজনে বিদ্যমান অনুমোদিত এডমিনের সাথে কথা বলুন।" to "Contact an existing authorized administrator if needed.",
        "গুগল সাইন-ইন ব্যর্থ হয়েছে" to "Google Sign-In Failed",
        "সাইন-ইন শুরু করা যায়নি" to "Could not start sign-in",
        "ইমেইল সাইন-ইন ব্যর্থ হয়েছে" to "Email Sign-In Failed",
        "Firebase কনফিগারেশন অনুপস্থিত" to "Firebase Configuration Missing",
        "জিপিএস লোকেশন" to "GPS Location",
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
                val missing = appBengaliCatalog.filter {
                    val k = it.trim()
                    !staticEnglishDict.containsKey(k) && !existingKeys.contains(k)
                }
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
     * Translates any Bengali string cleanly to English (instant offline dictionary first, dynamic cache fallback).
     */
    fun translate(text: String, lang: AppLanguage): String {
        if (lang == AppLanguage.BENGALI || text.isBlank()) return text
        if (!cacheLoaded) preloadCacheSync()

        val trimmed = text.trim()
        staticEnglishDict[trimmed]?.let { return convertBengaliDigitsToAscii(it) }

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
                lt.isBlank() || !containsBengali(lt) || staticEnglishDict.containsKey(lt) || !dynamicDict[lt].isNullOrBlank()
            }
            if (allLinesCached) {
                return lines.joinToString("\n") { line ->
                    val lt = line.trim()
                    if (lt.isBlank()) ""
                    else convertBengaliDigitsToAscii(staticEnglishDict[lt] ?: dynamicDict[lt] ?: lt)
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
        staticEnglishDict[trimmed]?.let { return convertBengaliDigitsToAscii(it) }
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
