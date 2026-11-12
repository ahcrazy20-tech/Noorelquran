package com.example.data.local

import com.example.data.model.AyahItem
import com.example.data.model.PrayerTimings
import com.example.data.model.ReciterInfo
import com.example.data.model.SurahMeta
import com.example.data.model.ZekrItem

object QuranStaticCatalog {

    val worldReciters: List<ReciterInfo> = listOf(
        ReciterInfo(
            id = "ar.alafasy",
            nameArabic = "مشاري راشد العفاسي",
            titleArabic = "إمام المسجد الكبير بالكويت",
            style = "مرتل خاشع",
            country = "الكويت",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/128/ar.alafasy"
        ),
        ReciterInfo(
            id = "ar.abdulbasitmurattal",
            nameArabic = "عبد الباسط عبد الصمد",
            titleArabic = "صوت مكة الذهبي",
            style = "مرتل أصيل",
            country = "مصر",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.abdulbasitmurattal",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/192/ar.abdulbasitmurattal"
        ),
        ReciterInfo(
            id = "ar.husary",
            nameArabic = "محمود خليل الحصري",
            titleArabic = "شيخ عموم المقارئ المصرية",
            style = "المصحف المرتل المتقن",
            country = "مصر",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.husary",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/128/ar.husary"
        ),
        ReciterInfo(
            id = "ar.minshawi",
            nameArabic = "محمد صديق المنشاوي",
            titleArabic = "الصوت الباكي الخاشع",
            style = "مرتل روحاني",
            country = "مصر",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.minshawi",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/128/ar.minshawi"
        ),
        ReciterInfo(
            id = "ar.mahermuaiqly",
            nameArabic = "ماهر المعيقلي",
            titleArabic = "إمام وخطيب المسجد الحرام",
            style = "مرتل الحرم المكي",
            country = "السعودية",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.mahermuaiqly",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/128/ar.mahermuaiqly"
        ),
        ReciterInfo(
            id = "ar.abdurrahmaansudais",
            nameArabic = "عبد الرحمن السديس",
            titleArabic = "إمام وخطيب المسجد الحرام",
            style = "مرتل الحرم المكي",
            country = "السعودية",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.abdurrahmaansudais",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/192/ar.abdurrahmaansudais"
        ),
        ReciterInfo(
            id = "ar.saoodshuraym",
            nameArabic = "سعود الشريم",
            titleArabic = "إمام المسجد الحرام سابقاً",
            style = "مرتل متقن",
            country = "السعودية",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.saoodshuraym",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/64/ar.saoodshuraym"
        ),
        ReciterInfo(
            id = "ar.ahmedajamy",
            nameArabic = "أحمد بن علي العجمي",
            titleArabic = "تلاوة ندية مؤثرة",
            style = "مرتل ندي",
            country = "السعودية",
            surahAudioBaseUrl = "https://cdn.islamic.network/quran/audio-surah/128/ar.ahmedajamy",
            ayahAudioBaseUrl = "https://cdn.islamic.network/quran/audio/128/ar.ahmedajamy"
        )
    )

    val allSurahs: List<SurahMeta> = listOf(
        SurahMeta(1, "الفاتحة", "Al-Fatihah", "مكية", 7, 1, 1),
        SurahMeta(2, "البقرة", "Al-Baqarah", "مدنية", 286, 2, 1),
        SurahMeta(3, "آل عمران", "Ali 'Imran", "مدنية", 200, 50, 3),
        SurahMeta(4, "النساء", "An-Nisa", "مدنية", 176, 77, 4),
        SurahMeta(5, "المائدة", "Al-Ma'idah", "مدنية", 120, 106, 6),
        SurahMeta(6, "الأنعام", "Al-An'am", "مكية", 165, 128, 7),
        SurahMeta(7, "الأعراف", "Al-A'raf", "مكية", 206, 151, 8),
        SurahMeta(8, "الأنفال", "Al-Anfal", "مدنية", 75, 177, 9),
        SurahMeta(9, "التوبة", "At-Tawbah", "مدنية", 129, 187, 10),
        SurahMeta(10, "يونس", "Yunus", "مكية", 109, 208, 11),
        SurahMeta(11, "هود", "Hud", "مكية", 123, 221, 11),
        SurahMeta(12, "يوسف", "Yusuf", "مكية", 111, 235, 12),
        SurahMeta(13, "الرعد", "Ar-Ra'd", "مدنية", 43, 249, 13),
        SurahMeta(14, "إبراهيم", "Ibrahim", "مكية", 52, 255, 13),
        SurahMeta(15, "الحجر", "Al-Hijr", "مكية", 99, 262, 14),
        SurahMeta(16, "النحل", "An-Nahl", "مكية", 128, 267, 14),
        SurahMeta(17, "الإسراء", "Al-Isra", "مكية", 111, 282, 15),
        SurahMeta(18, "الكهف", "Al-Kahf", "مكية", 110, 293, 15),
        SurahMeta(19, "مريم", "Maryam", "مكية", 98, 305, 16),
        SurahMeta(20, "طه", "Taha", "مكية", 135, 312, 16),
        SurahMeta(21, "الأنبياء", "Al-Anbiya", "مكية", 112, 322, 17),
        SurahMeta(22, "الحج", "Al-Hajj", "مدنية", 78, 332, 17),
        SurahMeta(23, "المؤمنون", "Al-Mu'minun", "مكية", 118, 342, 18),
        SurahMeta(24, "النور", "An-Nur", "مدنية", 64, 350, 18),
        SurahMeta(25, "الفرقان", "Al-Furqan", "مكية", 77, 359, 18),
        SurahMeta(26, "الشعراء", "Ash-Shu'ara", "مكية", 227, 367, 19),
        SurahMeta(27, "النمل", "An-Naml", "مكية", 93, 377, 19),
        SurahMeta(28, "القصص", "Al-Qasas", "مكية", 88, 385, 20),
        SurahMeta(29, "العنكبوت", "Al-'Ankabut", "مكية", 69, 396, 20),
        SurahMeta(30, "الروم", "Ar-Rum", "مكية", 60, 404, 21),
        SurahMeta(31, "لقمان", "Luqman", "مكية", 34, 411, 21),
        SurahMeta(32, "السجدة", "As-Sajdah", "مكية", 30, 415, 21),
        SurahMeta(33, "الأحزاب", "Al-Ahzab", "مدنية", 73, 418, 21),
        SurahMeta(34, "سبأ", "Saba", "مكية", 54, 428, 22),
        SurahMeta(35, "فاطر", "Fatir", "مكية", 45, 434, 22),
        SurahMeta(36, "يس", "Ya-Sin", "مكية", 83, 440, 22),
        SurahMeta(37, "الصافات", "As-Saffat", "مكية", 182, 446, 23),
        SurahMeta(38, "ص", "Sad", "مكية", 88, 453, 23),
        SurahMeta(39, "الزمر", "Az-Zumar", "مكية", 75, 458, 23),
        SurahMeta(40, "غافر", "Ghafir", "مكية", 85, 467, 24),
        SurahMeta(41, "فصلت", "Fussilat", "مكية", 54, 477, 24),
        SurahMeta(42, "الشورى", "Ash-Shuraa", "مكية", 53, 483, 25),
        SurahMeta(43, "الزخرف", "Az-Zukhruf", "مكية", 89, 489, 25),
        SurahMeta(44, "الدخان", "Ad-Dukhan", "مكية", 59, 496, 25),
        SurahMeta(45, "الجاثية", "Al-Jathiyah", "مكية", 37, 499, 25),
        SurahMeta(46, "الأحقاف", "Al-Ahqaf", "مكية", 35, 502, 26),
        SurahMeta(47, "محمد", "Muhammad", "مدنية", 38, 507, 26),
        SurahMeta(48, "الفتح", "Al-Fath", "مدنية", 29, 511, 26),
        SurahMeta(49, "الحجرات", "Al-Hujurat", "مدنية", 18, 515, 26),
        SurahMeta(50, "ق", "Qaf", "مكية", 45, 518, 26),
        SurahMeta(51, "الذاريات", "Adh-Dhariyat", "مكية", 60, 520, 26),
        SurahMeta(52, "الطور", "At-Tur", "مكية", 49, 523, 27),
        SurahMeta(53, "النجم", "An-Najm", "مكية", 62, 526, 27),
        SurahMeta(54, "القمر", "Al-Qamar", "مكية", 55, 528, 27),
        SurahMeta(55, "الرحمن", "Ar-Rahman", "مدنية", 78, 531, 27),
        SurahMeta(56, "الواقعة", "Al-Waqi'ah", "مكية", 96, 534, 27),
        SurahMeta(57, "الحديد", "Al-Hadid", "مدنية", 29, 537, 27),
        SurahMeta(58, "المجادلة", "Al-Mujadila", "مدنية", 22, 542, 28),
        SurahMeta(59, "الحشر", "Al-Hashr", "مدنية", 24, 545, 28),
        SurahMeta(60, "الممتحنة", "Al-Mumtahanah", "مدنية", 13, 549, 28),
        SurahMeta(61, "الصف", "As-Saf", "مدنية", 14, 551, 28),
        SurahMeta(62, "الجمعة", "Al-Jumu'ah", "مدنية", 11, 553, 28),
        SurahMeta(63, "المنافقون", "Al-Munafiqun", "مدنية", 11, 554, 28),
        SurahMeta(64, "التغابن", "At-Taghabun", "مدنية", 18, 556, 28),
        SurahMeta(65, "الطلاق", "At-Talaq", "مدنية", 12, 558, 28),
        SurahMeta(66, "التحريم", "At-Tahrim", "مدنية", 12, 560, 28),
        SurahMeta(67, "الملك", "Al-Mulk", "مكية", 30, 562, 29),
        SurahMeta(68, "القلم", "Al-Qalam", "مكية", 52, 564, 29),
        SurahMeta(69, "الحاقة", "Al-Haqqah", "مكية", 52, 566, 29),
        SurahMeta(70, "المعارج", "Al-Ma'arij", "مكية", 44, 568, 29),
        SurahMeta(71, "نوح", "Nuh", "مكية", 28, 570, 29),
        SurahMeta(72, "الجن", "Al-Jinn", "مكية", 28, 572, 29),
        SurahMeta(73, "المزمل", "Al-Muzzammil", "مكية", 20, 574, 29),
        SurahMeta(74, "المدثر", "Al-Muddaththir", "مكية", 56, 575, 29),
        SurahMeta(75, "القيامة", "Al-Qiyamah", "مكية", 40, 577, 29),
        SurahMeta(76, "الإنسان", "Al-Insan", "مدنية", 31, 578, 29),
        SurahMeta(77, "المرسلات", "Al-Mursalat", "مكية", 50, 580, 29),
        SurahMeta(78, "النبأ", "An-Naba", "مكية", 40, 582, 30),
        SurahMeta(79, "النازعات", "An-Nazi'at", "مكية", 46, 583, 30),
        SurahMeta(80, "عبس", "'Abasa", "مكية", 42, 585, 30),
        SurahMeta(81, "التكوير", "At-Takwir", "مكية", 29, 586, 30),
        SurahMeta(82, "الانفطار", "Al-Infitar", "مكية", 19, 587, 30),
        SurahMeta(83, "المطففين", "Al-Mutaffifin", "مكية", 36, 587, 30),
        SurahMeta(84, "الانشقاق", "Al-Inshiqaq", "مكية", 25, 589, 30),
        SurahMeta(85, "البروج", "Al-Buruj", "مكية", 22, 590, 30),
        SurahMeta(86, "الطارق", "At-Tariq", "مكية", 17, 591, 30),
        SurahMeta(87, "الأعلى", "Al-A'la", "مكية", 19, 591, 30),
        SurahMeta(88, "الغاشية", "Al-Ghashiyah", "مكية", 26, 592, 30),
        SurahMeta(89, "الفجر", "Al-Fajr", "مكية", 30, 593, 30),
        SurahMeta(90, "البلد", "Al-Balad", "مكية", 20, 594, 30),
        SurahMeta(91, "الشمس", "Ash-Shams", "مكية", 15, 595, 30),
        SurahMeta(92, "الليل", "Al-Layl", "مكية", 21, 595, 30),
        SurahMeta(93, "الضحى", "Ad-Duhaa", "مكية", 11, 596, 30),
        SurahMeta(94, "الشرح", "Ash-Sharh", "مكية", 8, 596, 30),
        SurahMeta(95, "التين", "At-Tin", "مكية", 8, 597, 30),
        SurahMeta(96, "العلق", "Al-'Alaq", "مكية", 19, 597, 30),
        SurahMeta(97, "القدر", "Al-Qadr", "مكية", 5, 598, 30),
        SurahMeta(98, "البينة", "Al-Bayyinah", "مدنية", 8, 598, 30),
        SurahMeta(99, "الزلزلة", "Az-Zalzalah", "مدنية", 8, 599, 30),
        SurahMeta(100, "العاديات", "Al-'Adiyat", "مكية", 11, 599, 30),
        SurahMeta(101, "القارعة", "Al-Qari'ah", "مكية", 11, 600, 30),
        SurahMeta(102, "التكاثر", "At-Takathur", "مكية", 8, 600, 30),
        SurahMeta(103, "العصر", "Al-'Asr", "مكية", 3, 601, 30),
        SurahMeta(104, "الهمزة", "Al-Humazah", "مكية", 9, 601, 30),
        SurahMeta(105, "الفيل", "Al-Fil", "مكية", 5, 601, 30),
        SurahMeta(106, "قريش", "Quraysh", "مكية", 4, 602, 30),
        SurahMeta(107, "الماعون", "Al-Ma'un", "مكية", 7, 602, 30),
        SurahMeta(108, "الكوثر", "Al-Kawthar", "مكية", 3, 602, 30),
        SurahMeta(109, "الكافرون", "Al-Kafirun", "مكية", 6, 603, 30),
        SurahMeta(110, "النصر", "An-Nasr", "مدنية", 3, 603, 30),
        SurahMeta(111, "المسد", "Al-Masad", "مكية", 5, 603, 30),
        SurahMeta(112, "الإخلاص", "Al-Ikhlas", "مكية", 4, 604, 30),
        SurahMeta(113, "الفلق", "Al-Falaq", "مكية", 5, 604, 30),
        SurahMeta(114, "الناس", "An-Nas", "مكية", 6, 604, 30)
    )

    private val offlineSurahMap: Map<Int, List<AyahItem>> = mapOf(
        1 to listOf(
            AyahItem(1, 1, "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", 1, 1, "أبدأ قراءتي مستعيناً بالله تعالى المتصف بالرحمة الواسعة والرحمة الواصلة للمؤمنين.", "ابتدئ باسم الله الرحمن الرحيم."),
            AyahItem(2, 2, "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", 1, 1, "الثناء الكامل وجميع المحامد لله وحده خالق الخلق ومالكهم ومدبر شؤونهم.", "جملة خبرية أريد بها الثناء على الله بمضمونها."),
            AyahItem(3, 3, "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", 1, 1, "الرحمن الذي وسعت رحمته جميع الخلق، الرحيم بالمؤمنين في الدنيا والآخرة.", "ذو الرحمة وهي إرادة الخير لأهله."),
            AyahItem(4, 4, "مَٰلِكِ يَوْمِ ٱلدِّينِ", 1, 1, "المتصرف وحده في يوم الجزاء والحساب، لا يملك أحد معه حكماً ولا شفاعة إلا بإذنه.", "يوم الجزاء وهو يوم القيامة."),
            AyahItem(5, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 1, 1, "نخصك وحدك يا الله بالعبادة والطاعة، ونستعين بك وحدك في جميع أمورنا.", "نخصك بالعبادة من توحيد وغيره ونطلب المعونة على العبادة وغيرها."),
            AyahItem(6, 6, "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", 1, 1, "دلّنا وأرشدنا وثبتنا على الطريق الواضح المستقيم الذي لا اعوجاج فيه وهو الإسلام.", "أرشدنا إلى الطريق الحق وثبتنا عليه."),
            AyahItem(7, 7, "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", 1, 1, "طريق النبيين والصديقين والشهداء والصالحين، غير طريق من غضبت عليهم لتركهم العمل بالعلم ولا طريق التائهين عن الحق.", "طريق المنعم عليهم بالهداية والرحمة.")
        ),
        36 to listOf(
            AyahItem(1, 3706, "يسٓ", 440, 22, "حروف مقطعة للدلالة على إعجاز القرآن الكريم، والله أعلم بمراده به.", "الله أعلم بمراده به."),
            AyahItem(2, 3707, "وَٱلْقُرْءَانِ ٱلْحَكِيمِ", 440, 22, "يقسم الله تعالى بالقرآن المحكم في نظمه ومعانيه وأحكامه.", "المحكم العجيب النظم والبيان."),
            AyahItem(3, 3708, "إِنَّكَ لَمِنَ ٱلْمُرْسَلِينَ", 440, 22, "إنك يا محمد لمن المرسلين من عند الله بالوحي والهدى.", "جواب القسم: إنك يا محمد لمن المرسلين."),
            AyahItem(4, 3709, "عَلَىٰ صِرَٰطٍ مُّسْتَقِيمٍ", 440, 22, "على دين قويم وطريق معتدل لا عوج فيه وهو الإسلام.", "على طريق الأنبياء قبلك من التوحيد والهدى."),
            AyahItem(5, 3710, "تَنزِيلَ ٱلْعَزِيزِ ٱلرَّحِيمِ", 440, 22, "هذا القرآن تنزيل الله العزيز في ملكه، الرحيم بعباده المؤمنين.", "خبر مبتدأ محذوف أي هذا القرآن تنزيل العزيز الرحيم.")
        ),
        67 to listOf(
            AyahItem(1, 5242, "تَبَٰرَكَ ٱلَّذِى بِيَدِهِ ٱلْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَىْءٍ قَدِيرٌ", 562, 29, "تعاظم وكثر خير الله الذي بيده ملك الدنيا والآخرة وسلطانهما، وهو على كل شيء قدير.", "تعالى وتعاظم عن صفات المخلوقين وكثر خيره."),
            AyahItem(2, 5243, "ٱلَّذِى خَلَقَ ٱلْمَوْتَ وَٱلْحَيَوٰةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ ٱلْعَزِيزُ ٱلْغَفُورُ", 562, 29, "الذي أوجد الموت والحياة ليختبركم أيها الناس أيكم أخلص عملاً وأصوب على السنة.", "ليختبركم في الحياة أيكم أطوع لله وأورع عن محارمه."),
            AyahItem(3, 5244, "ٱلَّذِى خَلَقَ سَبْعَ سَمَٰوَٰتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِى خَلْقِ ٱلرَّحْمَٰنِ مِن تَفَٰوُتٍ ۖ فَٱرْجِعِ ٱلْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ", 562, 29, "الذي خلق سبع سماوات بعضها فوق بعض في غاية الإحكام بلا خلل ولا اضطراب.", "بعضها فوق بعض من غير مماسة ولا تفاوت.")
        ),
        112 to listOf(
            AyahItem(1, 6222, "قُلْ هُوَ ٱللَّهُ أَحَدٌ", 604, 30, "قل يا أيها الرسول: هو الله المتفرد بالألوهية والكمال، لا شريك له.", "قل هو الله الواحد الأحد الذي لا نظير له."),
            AyahItem(2, 6223, "ٱللَّهُ ٱلصَّمَدُ", 604, 30, "الله وحده المقصود في قضاء الحوائج على الدوام، الكامل في صفاته.", "المقصود في الحوائج على الدوام."),
            AyahItem(3, 6224, "لَمْ يَلِدْ وَلَمْ يُولَدْ", 604, 30, "ليس له ولد ولا والد ولا صاحبة، لكمال غناه وقيوميته.", "لانتفاء المجانسة والحدوث عنه سبحانه."),
            AyahItem(4, 6225, "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌ", 604, 30, "ولم يكن له مماثلاً ولا مكافئاً أحد من خلقه.", "ولم يكن له مكافئاً ومماثلاً أحد.")
        ),
        113 to listOf(
            AyahItem(1, 6226, "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ", 604, 30, "قل: أعتصم وأستجير برب الصبح وخالق الخلق جميعاً.", "أستجير برب الصبح."),
            AyahItem(2, 6227, "مِن شَرِّ مَا خَلَقَ", 604, 30, "من أذى وشر كل مخلوق ذي شر من الإنس والجن والهوام.", "من شر جميع المخلوقات."),
            AyahItem(3, 6228, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", 604, 30, "ومن شر الليل الشديد الظلمة إذا دخل وتغلغل وما يخرج فيه من الآفات.", "الليل إذا أظلم ودخل."),
            AyahItem(4, 6229, "وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِى ٱلْعُقَدِ", 604, 30, "ومن شر السواحر اللاتي ينفخن في عقد الخيوط حين يسحرن.", "السواحر اللاتي يعقدن في الخيوط وينفثن عليها."),
            AyahItem(5, 6230, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", 604, 30, "ومن شر الحاسد الكاره لنعم الله على عباده إذا أظهر حسده وعمل بمقتضاه.", "إذا أظهر ما في نفسه من الحسد وعمل بمقتضاه.")
        ),
        114 to listOf(
            AyahItem(1, 6231, "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ", 604, 30, "قل: أعتصم برب الناس ومربيهم ومدبر أمورهم.", "خالقهم ومالكهم."),
            AyahItem(2, 6232, "مَلِكِ ٱلنَّاسِ", 604, 30, "المتصرف في شؤون الناس، الغني عنهم.", "المتصرف في شؤونهم."),
            AyahItem(3, 6233, "إِلَٰهِ ٱلنَّاسِ", 604, 30, "معبود الناس الحق الذي لا معبود لهم سواه.", "معبودهم بالحق."),
            AyahItem(4, 6234, "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ", 604, 30, "من شر الشيطان الذي يلقي الوساوس في الغفلة ويخنس عند ذكر الله.", "الشيطان الملازم للوسوسة المتأخر عند ذكر الله."),
            AyahItem(5, 6235, "ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ", 604, 30, "الذي يبث الشكوك والشرور في قلوب الناس.", "في قلوبهم إذا غفلوا عن ذكر الله."),
            AyahItem(6, 6236, "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ", 604, 30, "سواء أكان ذلك الموسوس من شياطين الجن أم من شياطين الإنس.", "بيان للموسوس أنه يكون من الجن ومن الإنس.")
        )
    )

    fun getOfflineFallbackAyahs(surahNumber: Int): List<AyahItem> {
        offlineSurahMap[surahNumber]?.let { return it }
        val meta = allSurahs.find { it.number == surahNumber } ?: allSurahs.first()
        return listOf(
            AyahItem(
                numberInSurah = 1,
                globalNumber = 1,
                textUthmani = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ﴿١﴾ سُورَةُ ${meta.nameArabic} (${meta.ayahCount} آية)",
                page = meta.startPage,
                juz = meta.juzNumber,
                tafsirMuyassar = "جارٍ تحميل النص القرآني الكامل بالرسم العثماني مع التفسير الميسر والجلالين لسورة ${meta.nameArabic}...",
                tafsirJalalayn = "سورة ${meta.nameArabic} (${meta.revelationType}) — عدد آياتها ${meta.ayahCount}."
            )
        )
    }

    val adhkarCatalog: List<ZekrItem> = listOf(
        ZekrItem(
            id = "m_1",
            category = "أذكار الصباح",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            virtue = "من قالها حين يصبح سأل الله خير هذا اليوم واستعاذ من شره.",
            targetCount = 1
        ),
        ZekrItem(
            id = "m_2",
            category = "أذكار الصباح",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ.",
            virtue = "سيد الاستغفار: من قالها من النهار موقناً بها فمات من يومه فهو من أهل الجنة.",
            targetCount = 1
        ),
        ZekrItem(
            id = "m_3",
            category = "أذكار الصباح",
            arabicText = "بِسْمِ اللهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
            virtue = "لم يضره من الله شيء (ثلاث مرات).",
            targetCount = 3
        ),
        ZekrItem(
            id = "m_4",
            category = "أذكار الصباح",
            arabicText = "حَسْبِيَ اللهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ.",
            virtue = "من قالها حين يصبح وحين يمسي سبع مرات كفاه الله ما أهمه من أمر الدنيا والآخرة.",
            targetCount = 7
        ),
        ZekrItem(
            id = "e_1",
            category = "أذكار المساء",
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
            virtue = "حفظ ووقاية وبركة في الليلة.",
            targetCount = 1
        ),
        ZekrItem(
            id = "e_2",
            category = "أذكار المساء",
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
            virtue = "من قالها حين يمسي ثلاث مرات لم تضره حُمَة تلك الليلة.",
            targetCount = 3
        ),
        ZekrItem(
            id = "p_1",
            category = "بعد الصلاة",
            arabicText = "أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ، أَسْتَغْفِرُ اللهَ. اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ.",
            virtue = "سنة النبي ﷺ عقب السلام من الصلاة المفروضة مباشرة.",
            targetCount = 1
        ),
        ZekrItem(
            id = "p_2",
            category = "بعد الصلاة",
            arabicText = "سُبْحَانَ اللهِ وَبِحَمْدِهِ، سُبْحَانَ اللهِ الْعَظِيمِ.",
            virtue = "كلمتان خفيفتان على اللسان، ثقيلتان في الميزان، حبيبتان إلى الرحمن.",
            targetCount = 33
        ),
        ZekrItem(
            id = "t_1",
            category = "تسبيح واستغفار",
            arabicText = "سُبْحَانَ اللهِ وَبِحَمْدِهِ",
            virtue = "من قالها في يوم مائة مرة حُطَّت خطاياه وإن كانت مثل زبد البحر.",
            targetCount = 100
        ),
        ZekrItem(
            id = "t_2",
            category = "تسبيح واستغفار",
            arabicText = "اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            virtue = "من صلى عليّ صلاة صلى الله عليه بها عشراً.",
            targetCount = 10
        ),
        ZekrItem(
            id = "t_3",
            category = "تسبيح واستغفار",
            arabicText = "أَسْتَغْفِرُ اللهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيَّ الْقَيُّومَ وَأَتُوبُ إِلَيْهِ",
            virtue = "غفران الذنوب وتفريج الكروب وسعة الرزق.",
            targetCount = 100
        ),
        ZekrItem(
            id = "t_4",
            category = "تسبيح واستغفار",
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللهِ الْعَلِيِّ الْعَظِيمِ",
            virtue = "كنز من كنوز الجنة ودواء من تسعة وتسعين داءً أيسرها الهم.",
            targetCount = 33
        ),
        ZekrItem(
            id = "s_1",
            category = "أذكار النوم والاستيقاظ",
            arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.",
            virtue = "يقال عند وضع الجنب على الفراش للنوم.",
            targetCount = 1
        ),
        ZekrItem(
            id = "s_2",
            category = "أذكار النوم والاستيقاظ",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ.",
            virtue = "ذكر الاستيقاظ من النوم شكراً لله على نعمة الحياة.",
            targetCount = 1
        )
    )

    val dailyDhikrChecklist: List<Pair<String, String>> = listOf(
        "wird_morning" to "أذكار الصباح كاملة",
        "wird_evening" to "أذكار المساء كاملة",
        "wird_prayer" to "الأذكار عقب الصلوات الخمس",
        "wird_istighfar" to "الاستغفار (١٠٠ مرة يومياً)",
        "wird_salawat" to "الصلاة على النبي ﷺ (١٠٠ مرة)",
        "wird_kursi" to "قراءة آية الكرسي بعد كل صلاة وعند النوم",
        "wird_mulk" to "قراءة سورة الملك قبل النوم (المنجية)",
        "wird_dua_creator" to "الدعاء لصاحب الفكرة والعمل (AhmedSoliman)"
    )

    val supportedCities: List<Pair<String, String>> = listOf(
        "القاهرة" to "Cairo,Egypt",
        "مكة المكرمة" to "Makkah,Saudi Arabia",
        "المدينة المنورة" to "Madinah,Saudi Arabia",
        "الرياض" to "Riyadh,Saudi Arabia",
        "الإسكندرية" to "Alexandria,Egypt",
        "دبي" to "Dubai,United Arab Emirates",
        "عمّان" to "Amman,Jordan",
        "الرباط" to "Rabat,Morocco",
        "الجزائر" to "Algiers,Algeria",
        "إسطنبول" to "Istanbul,Turkey"
    )

    fun defaultPrayerTimings(cityArabic: String = "القاهرة"): PrayerTimings {
        return PrayerTimings(
            city = cityArabic,
            country = "التوقيت المحلي",
            hijriDate = "١٢ جمادى الأولى ١٤٤٨ هـ",
            fajr = "04:52",
            sunrise = "06:19",
            dhuhr = "11:48",
            asr = "14:51",
            maghrib = "17:16",
            isha = "18:36",
            nextPrayerName = "المغرب",
            nextPrayerTime = "17:16",
            remainingTimeText = "متبقٍ ٠١:٢٤ ساعة"
        )
    }
}
