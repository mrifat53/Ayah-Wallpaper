package com.rifat.ayahwall

object I18n {

    val bnSurahs = arrayOf(
        "আল-ফাতিহা", "আল-বাকারা", "আলে ইমরান", "আন-নিসা", "আল-মায়িদা", "আল-আনআম", "আল-আ'রাফ", "আল-আনফাল", "আত-তাওবা", "ইউনুস",
        "হূদ", "ইউসুফ", "আর-রা'দ", "ইবরাহীম", "আল-হিজর", "আন-নাহল", "আল-ইসরা", "আল-কাহফ", "মারইয়াম", "ত্বা-হা",
        "আল-আম্বিয়া", "আল-হাজ্জ", "আল-মু'মিনূন", "আন-নূর", "আল-ফুরকান", "আশ-শু'আরা", "আন-নামল", "আল-কাসাস", "আল-আনকাবূত", "আর-রূম",
        "লুকমান", "আস-সাজদাহ", "আল-আহযাব", "সাবা", "ফাতির", "ইয়া-সীন", "আস-সাফফাত", "সোয়াদ", "আয-যুমার", "গাফির",
        "ফুসসিলাত", "আশ-শূরা", "আয-যুখরুফ", "আদ-দুখান", "আল-জাসিয়াহ", "আল-আহকাফ", "মুহাম্মাদ", "আল-ফাতহ", "আল-হুজুরাত", "কাফ",
        "আয-যারিয়াত", "আত-তূর", "আন-নাজম", "আল-কামার", "আর-রহমান", "আল-ওয়াকিআহ", "আল-হাদীদ", "আল-মুজাদালাহ", "আল-হাশর", "আল-মুমতাহিনাহ",
        "আস-সাফ", "আল-জুমুআহ", "আল-মুনাফিকূন", "আত-তাগাবুন", "আত-তালাক", "আত-তাহরীম", "আল-মুলক", "আল-কলম", "আল-হাক্কাহ", "আল-মাআরিজ",
        "নূহ", "আল-জিন", "আল-মুযযাম্মিল", "আল-মুদ্দাসসির", "আল-কিয়ামাহ", "আল-ইনসান", "আল-মুরসালাত", "আন-নাবা", "আন-নাযিআত", "আবাসা",
        "আত-তাকবীর", "আল-ইনফিতার", "আল-মুতাফফিফীন", "আল-ইনশিকাক", "আল-বুরূজ", "আত-তারিক", "আল-আ'লা", "আল-গাশিয়াহ", "আল-ফাজর", "আল-বালাদ",
        "আশ-শামস", "আল-লাইল", "আদ-দুহা", "আশ-শারহ", "আত-তীন", "আল-আলাক", "আল-কদর", "আল-বাইয়্যিনাহ", "আয-যিলযাল", "আল-আদিয়াত",
        "আল-কারিআহ", "আত-তাকাসুর", "আল-আসর", "আল-হুমাযাহ", "আল-ফীল", "কুরাইশ", "আল-মাউন", "আল-কাউসার", "আল-কাফিরূন", "আন-নাসর",
        "আল-লাহাব", "আল-ইখলাস", "আল-ফালাক", "আন-নাস"
    )

    val enSurahs = arrayOf(
        "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah", "Yunus",
        "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra", "Al-Kahf", "Maryam", "Ta-Ha",
        "Al-Anbiya", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara", "An-Naml", "Al-Qasas", "Al-'Ankabut", "Ar-Rum",
        "Luqman", "As-Sajdah", "Al-Ahzab", "Saba", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", "Ghafir",
        "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", "Qaf",
        "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid", "Al-Mujadila", "Al-Hashr", "Al-Mumtahanah",
        "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij",
        "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", "An-Naba", "An-Nazi'at", "'Abasa",
        "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", "Al-A'la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad",
        "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", "Al-'Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-'Adiyat",
        "Al-Qari'ah", "At-Takathur", "Al-'Asr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr",
        "Al-Masad", "Al-Ikhlas", "Al-Falaq", "An-Nas"
    )

    val arSurahs = arrayOf(
        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
        "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
        "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
        "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
        "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
        "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
        "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
        "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
        "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
        "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
        "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
        "المسد", "الإخلاص", "الفلق", "الناس"
    )

    // Themes: references need review by a scholar before release.
    val themeKeys = listOf("patience", "gratitude", "hope", "dua", "tawakkul")

    val themeRefs: Map<String, List<Pair<Int, Int>>> = mapOf(
        "patience" to listOf(2 to 153, 2 to 155, 3 to 200, 39 to 10, 94 to 5, 94 to 6, 16 to 127, 11 to 115),
        "gratitude" to listOf(14 to 7, 2 to 152, 31 to 12, 16 to 18, 55 to 13),
        "hope" to listOf(39 to 53, 12 to 87, 15 to 56, 65 to 2, 65 to 3, 94 to 5, 94 to 6, 7 to 156),
        "dua" to listOf(2 to 186, 2 to 201, 3 to 8, 25 to 74, 14 to 40, 20 to 114, 23 to 118, 40 to 60, 21 to 87),
        "tawakkul" to listOf(65 to 3, 3 to 159, 8 to 2, 3 to 173, 9 to 51, 11 to 123, 25 to 58, 14 to 12)
    )

    fun langIndex(lang: String): Int = when (lang) {
        "bn" -> 0
        "en" -> 1
        else -> 2
    }

    private val strings: Map<String, Array<String>> = mapOf(
        "title" to arrayOf("আয়াত ওয়ালপেপার", "Ayah Wallpaper", "خلفية الآيات"),
        "subtitle" to arrayOf("প্রতি ঘণ্টায় লক স্ক্রিনে নতুন আয়াত", "A new ayah on your lock screen every hour", "آية جديدة على شاشة القفل كل ساعة"),
        "enable" to arrayOf("প্রতি ঘণ্টায় নতুন আয়াত চালু", "Change the ayah every hour", "تغيير الآية كل ساعة"),
        "ui_lang" to arrayOf("অ্যাপের ভাষা", "App language", "لغة التطبيق"),
        "content_lang" to arrayOf("আয়াতের ভাষা (এক বা একাধিক)", "Ayah languages (one or more)", "لغات الآية (واحدة أو أكثر)"),
        "arabic" to arrayOf("আরবি", "Arabic", "العربية"),
        "bangla" to arrayOf("বাংলা", "Bangla", "البنغالية"),
        "english" to arrayOf("ইংরেজি", "English", "الإنجليزية"),
        "category" to arrayOf("আয়াতের ধরন", "Ayah category", "فئة الآيات"),
        "cat_all" to arrayOf("সব আয়াত", "All ayahs", "كل الآيات"),
        "cat_surah" to arrayOf("সূরা অনুযায়ী", "By surah", "حسب السورة"),
        "cat_juz" to arrayOf("পারা অনুযায়ী", "By juz", "حسب الجزء"),
        "cat_theme" to arrayOf("বিষয় অনুযায়ী", "By theme", "حسب الموضوع"),
        "juz_word" to arrayOf("পারা", "Juz", "الجزء"),
        "theme_patience" to arrayOf("ধৈর্য", "Patience", "الصبر"),
        "theme_gratitude" to arrayOf("শুকরিয়া", "Gratitude", "الشكر"),
        "theme_hope" to arrayOf("আশা ও রহমত", "Hope and mercy", "الأمل والرحمة"),
        "theme_dua" to arrayOf("দোয়া", "Dua", "الدعاء"),
        "theme_tawakkul" to arrayOf("তাওয়াক্কুল", "Trust in Allah", "التوكل"),
        "style" to arrayOf("ব্যাকগ্রাউন্ডের স্টাইল", "Background style", "نمط الخلفية"),
        "style_random" to arrayOf("এলোমেলো", "Random", "عشوائي"),
        "style_night" to arrayOf("তারাভরা রাত", "Starry night", "ليلة مرصعة بالنجوم"),
        "style_pattern" to arrayOf("জ্যামিতিক নকশা", "Geometric pattern", "زخرفة هندسية"),
        "style_kaaba" to arrayOf("কাবার রাত", "Kaaba at night", "الكعبة ليلاً"),
        "style_dome" to arrayOf("সবুজ গম্বুজ", "Green dome", "القبة الخضراء"),
        "style_mihrab" to arrayOf("মিহরাব", "Mihrab arch", "محراب"),
        "style_aurora" to arrayOf("আলোর আকাশ", "Glowing sky", "سماء متوهجة"),
        "position" to arrayOf("লেখার অবস্থান (ঘড়ির নিচে ঠিক করুন)", "Text position (set it below the clock)", "موضع النص (اضبطه تحت الساعة)"),
        "apply" to arrayOf("এখনই নতুন আয়াত বসান", "Apply a new ayah now", "تطبيق آية جديدة الآن"),
        "today" to arrayOf("আজকের আয়াত", "Today's ayah", "آية اليوم"),
        "battery_note" to arrayOf(
            "কিছু ফোনে ব্যাটারি সেভার অ্যাপ বন্ধ করে দেয়। নিয়মিত বদলাতে ব্যাটারি সীমা বন্ধ রাখুন।",
            "Some phones stop apps to save battery. Turn off the battery limit so the ayah keeps changing.",
            "بعض الهواتف توقف التطبيقات لتوفير البطارية. أوقف قيود البطارية لتستمر الآية بالتغير."
        ),
        "battery_btn" to arrayOf("ব্যাটারি সেটিংস খুলুন", "Open battery settings", "فتح إعدادات البطارية"),
        "credits" to arrayOf(
            "আরবি: alquran.cloud (উসমানি)। বাংলা অনুবাদ: ড. আবু বকর মুহাম্মাদ যাকারিয়া, কিং ফাহাদ কমপ্লেক্স (QuranEnc.com)। ইংরেজি: Saheeh International (QuranEnc.com)।",
            "Arabic: alquran.cloud (Uthmani). Bangla: Dr. Abu Bakr Muhammad Zakaria, King Fahd Complex (QuranEnc.com). English: Saheeh International (QuranEnc.com).",
            "العربية: alquran.cloud (رسم عثماني). البنغالية: د. أبو بكر محمد زكريا، مجمع الملك فهد (QuranEnc.com). الإنجليزية: Saheeh International (QuranEnc.com)."
        ),
        "working" to arrayOf("ওয়ালপেপার বদলানো হচ্ছে...", "Updating wallpaper...", "جارٍ تحديث الخلفية..."),
        "enabled_toast" to arrayOf("চালু হয়েছে। লক স্ক্রিন দেখুন।", "Enabled. Check your lock screen.", "تم التفعيل. تحقق من شاشة القفل."),
        "keep_one" to arrayOf("অন্তত একটি ভাষা রাখুন", "Keep at least one language", "أبقِ لغة واحدة على الأقل"),
        "share" to arrayOf("ছবি হিসেবে শেয়ার", "Share as image", "مشاركة كصورة"),
        "fav_add" to arrayOf("পছন্দের তালিকায় রাখুন", "Save to favorites", "حفظ في المفضلة"),
        "fav_remove" to arrayOf("পছন্দের তালিকা থেকে সরান", "Remove from favorites", "إزالة من المفضلة"),
        "favorites" to arrayOf("পছন্দের আয়াত", "Favorite ayahs", "الآيات المفضلة"),
        "recent" to arrayOf("সাম্প্রতিক আয়াত", "Recent ayahs", "آيات حديثة"),
        "empty" to arrayOf("এখনো কিছু নেই", "Nothing here yet", "لا شيء بعد"),
        "about" to arrayOf("অনুবাদ সম্পর্কে", "About the translation", "حول الترجمة"),
        "about_text" to arrayOf(
            "কুরআনের কিছু আয়াতে আল্লাহ নিজের জন্য “আমরা” (We) শব্দ ব্যবহার করেছেন। আরবিতে এটি মহত্ত্ব ও সম্মানের বহুবচন। আল্লাহ এক ও অদ্বিতীয়। অনুবাদ অপরিবর্তিত রাখা হয়েছে।",
            "In some verses Allah refers to Himself as “We”. In Arabic this is the plural of majesty. Allah is One and Unique. Translations are shown unchanged.",
            "في بعض الآيات يتحدث الله عن نفسه بصيغة الجمع (نحن)، وهي في العربية للتعظيم. والله واحد أحد. وقد عُرضت الترجمات دون أي تغيير."
        ),
        "no_internet" to arrayOf("ইন্টারনেট নেই, আয়াত আনা যায়নি", "No internet, could not load an ayah", "لا يوجد اتصال، تعذر تحميل الآية"),
        "loading" to arrayOf("লোড হচ্ছে...", "Loading...", "جارٍ التحميل..."),
        "new_ayah" to arrayOf("নতুন আয়াত", "New ayah", "آية جديدة"),
        "close" to arrayOf("বন্ধ করুন", "Close", "إغلاق")
    )

    fun t(key: String, lang: String): String = strings[key]?.get(langIndex(lang)) ?: key

    fun digits(s: String, lang: String): String {
        val base: Char = when (lang) {
            "bn" -> '\u09E6'
            "ar" -> '\u0660'
            else -> return s
        }
        val sb = StringBuilder()
        for (ch in s) {
            if (ch in '0'..'9') sb.append(base + (ch - '0')) else sb.append(ch)
        }
        return sb.toString()
    }

    fun surahName(lang: String, surah: Int): String = when (lang) {
        "bn" -> bnSurahs[surah - 1]
        "ar" -> arSurahs[surah - 1]
        else -> enSurahs[surah - 1]
    }

    fun ref(lang: String, surah: Int, ayah: Int): String {
        val nums = digits("$surah:$ayah", lang)
        return when (lang) {
            "bn" -> "সূরা ${surahName("bn", surah)}, $nums"
            "ar" -> "سورة ${surahName("ar", surah)} $nums"
            else -> "${surahName("en", surah)} $nums"
        }
    }
}
