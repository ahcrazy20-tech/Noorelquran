package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocalAdhkarLogEntity
import com.example.data.local.LocalBookmarkEntity
import com.example.data.local.LocalKhatmahEntity
import com.example.data.local.LocalRecitationEntity
import com.example.data.local.NoorLocalDao
import com.example.data.local.QuranStaticCatalog
import com.example.data.model.AdhkarLog
import com.example.data.model.AyahItem
import com.example.data.model.DownloadedSurahAudio
import com.example.data.model.KhatmahPlan
import com.example.data.model.PrayerTimings
import com.example.data.model.QuranBookmark
import com.example.data.model.RecitationSession
import com.example.data.model.ReciterInfo
import com.example.data.model.SurahMeta
import com.example.data.remote.QuranAndPrayerService
import com.example.data.repository.NoorFirestoreRepository
import com.example.util.LocationAndQiblaManager
import com.example.util.PrayerNotificationHelper
import com.example.util.QuranAudioAndSpeechManager
import com.example.util.UserGeoLocation
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class MushafDisplayMode {
    AYAH_BY_AYAH,
    PAGE_BY_PAGE
}

enum class TafsirSource {
    MUYASSAR,
    JALALAYN
}

sealed interface FirestoreListState<out T> {
    data object Loading : FirestoreListState<Nothing>
    data class Success<T>(val items: List<T>) : FirestoreListState<T>
    data class Error(val message: String) : FirestoreListState<Nothing>
}

class NoorMainViewModel(
    private val localDao: NoorLocalDao,
    private val cloudRepository: NoorFirestoreRepository,
    private val quranService: QuranAndPrayerService,
    private val audioSpeechManager: QuranAudioAndSpeechManager,
    private val locationManager: LocationAndQiblaManager
) : ViewModel() {

    // --- MUSHAF & TAFSIR STATE ---
    private val _selectedSurah = MutableStateFlow(QuranStaticCatalog.allSurahs.first())
    val selectedSurah: StateFlow<SurahMeta> = _selectedSurah.asStateFlow()

    private val _ayahs = MutableStateFlow<List<AyahItem>>(QuranStaticCatalog.getOfflineFallbackAyahs(1))
    val ayahs: StateFlow<List<AyahItem>> = _ayahs.asStateFlow()

    private val _isLoadingSurah = MutableStateFlow(false)
    val isLoadingSurah: StateFlow<Boolean> = _isLoadingSurah.asStateFlow()

    private val _mushafDisplayMode = MutableStateFlow(MushafDisplayMode.AYAH_BY_AYAH)
    val mushafDisplayMode: StateFlow<MushafDisplayMode> = _mushafDisplayMode.asStateFlow()

    private val _selectedTafsirSource = MutableStateFlow(TafsirSource.MUYASSAR)
    val selectedTafsirSource: StateFlow<TafsirSource> = _selectedTafsirSource.asStateFlow()

    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    // --- RECITER & AUDIO PLAYBACK STATE ---
    private val _selectedReciter = MutableStateFlow(QuranStaticCatalog.worldReciters.first())
    val selectedReciter: StateFlow<ReciterInfo> = _selectedReciter.asStateFlow()

    private val _playingAyahNumber = MutableStateFlow<Int?>(null)
    val playingAyahNumber: StateFlow<Int?> = _playingAyahNumber.asStateFlow()

    private val _isPlayingFullSurah = MutableStateFlow(false)
    val isPlayingFullSurah: StateFlow<Boolean> = _isPlayingFullSurah.asStateFlow()

    private val _shadowingModeEnabled = MutableStateFlow(false)
    val shadowingModeEnabled: StateFlow<Boolean> = _shadowingModeEnabled.asStateFlow()

    private val _awaitingUserShadowRepeat = MutableStateFlow(false)
    val awaitingUserShadowRepeat: StateFlow<Boolean> = _awaitingUserShadowRepeat.asStateFlow()

    // --- OFFLINE DOWNLOADS STATE ---
    private val _downloadedSurahs = MutableStateFlow<List<DownloadedSurahAudio>>(emptyList())
    val downloadedSurahs: StateFlow<List<DownloadedSurahAudio>> = _downloadedSurahs.asStateFlow()

    private val _downloadProgressMap = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, Int>> = _downloadProgressMap.asStateFlow()

    // --- LIVE TARTEEL VOICE & MEMORIZATION STATE ---
    private val _hideWordsForMemorization = MutableStateFlow(false)
    val hideWordsForMemorization: StateFlow<Boolean> = _hideWordsForMemorization.asStateFlow()

    private val _isListeningMic = MutableStateFlow(false)
    val isListeningMic: StateFlow<Boolean> = _isListeningMic.asStateFlow()

    private val _liveSpokenTranscript = MutableStateFlow("")
    val liveSpokenTranscript: StateFlow<String> = _liveSpokenTranscript.asStateFlow()

    private val _lastRecitationAccuracy = MutableStateFlow<Int?>(null)
    val lastRecitationAccuracy: StateFlow<Int?> = _lastRecitationAccuracy.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // --- GPS LOCATION, QIBLA COMPASS & PRAYER STATE ---
    private val _userGeoLocation = MutableStateFlow(locationManager.getSavedLocation())
    val userGeoLocation: StateFlow<UserGeoLocation?> = _userGeoLocation.asStateFlow()

    private val _isUpdatingLocation = MutableStateFlow(false)
    val isUpdatingLocation: StateFlow<Boolean> = _isUpdatingLocation.asStateFlow()

    val compassHeadingDegrees: StateFlow<Float> = locationManager.observeCompassHeadingDegrees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0f)

    private val _selectedCityPair = MutableStateFlow(QuranStaticCatalog.supportedCities.first())
    val selectedCityPair: StateFlow<Pair<String, String>> = _selectedCityPair.asStateFlow()

    private val _prayerTimings = MutableStateFlow(QuranStaticCatalog.defaultPrayerTimings())
    val prayerTimings: StateFlow<PrayerTimings> = _prayerTimings.asStateFlow()

    private val _prayerNotificationsEnabled = MutableStateFlow(true)
    val prayerNotificationsEnabled: StateFlow<Boolean> = _prayerNotificationsEnabled.asStateFlow()

    private val _adhkarNotificationsEnabled = MutableStateFlow(true)
    val adhkarNotificationsEnabled: StateFlow<Boolean> = _adhkarNotificationsEnabled.asStateFlow()

    // --- LOCAL-FIRST REACTIVE STREAMS (Works immediately without login) ---
    val khatmahPlansState: StateFlow<FirestoreListState<KhatmahPlan>> =
        localDao.observeKhatmahPlans()
            .map<List<LocalKhatmahEntity>, FirestoreListState<KhatmahPlan>> { list ->
                FirestoreListState.Success(list.map { it.toDomain() })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = FirestoreListState.Loading
            )

    val bookmarksState: StateFlow<FirestoreListState<QuranBookmark>> =
        localDao.observeBookmarks()
            .map<List<LocalBookmarkEntity>, FirestoreListState<QuranBookmark>> { list ->
                FirestoreListState.Success(list.map { it.toDomain() })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = FirestoreListState.Loading
            )

    val recitationSessionsState: StateFlow<FirestoreListState<RecitationSession>> =
        localDao.observeRecitationSessions()
            .map<List<LocalRecitationEntity>, FirestoreListState<RecitationSession>> { list ->
                FirestoreListState.Success(list.map { it.toDomain() })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = FirestoreListState.Loading
            )

    val adhkarLogsState: StateFlow<FirestoreListState<AdhkarLog>> =
        localDao.observeAdhkarLogs()
            .map<List<LocalAdhkarLogEntity>, FirestoreListState<AdhkarLog>> { list ->
                FirestoreListState.Success(list.map { it.toDomain() })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = FirestoreListState.Loading
            )

    init {
        selectSurah(QuranStaticCatalog.allSurahs.first())
        refreshDownloadedSurahs()
        val savedLoc = _userGeoLocation.value
        if (savedLoc != null) {
            refreshPrayerTimingsFromGps(savedLoc)
        } else {
            refreshPrayerTimings()
        }
    }

    fun hasRequestedLocationBefore(): Boolean = locationManager.hasRequestedLocationBefore()

    fun markLocationPromptShown() {
        locationManager.markLocationRequested()
    }

    fun updateDeviceLocationFromGps() {
        if (_isUpdatingLocation.value) return
        viewModelScope.launch {
            _isUpdatingLocation.value = true
            locationManager.fetchCurrentDeviceLocation()
                .onSuccess { geoLoc ->
                    _userGeoLocation.value = geoLoc
                    val timings = quranService.fetchPrayerTimingsByCoordinates(
                        latitude = geoLoc.latitude,
                        longitude = geoLoc.longitude,
                        areaLabelArabic = geoLoc.areaNameArabic
                    )
                    _prayerTimings.value = timings
                    _statusBannerMessage.value = "تم تحديث موقعك (${geoLoc.areaNameArabic}) ومواقيت الصلاة واتجاه القبلة (${geoLoc.qiblaBearingDegrees.toInt()}°)"
                }
                .onFailure {
                    _statusBannerMessage.value = "تعذر تحديد الموقع تلقائياً — يمكنك المحاولة مرة أخرى أو اختيار مدينتك"
                }
            _isUpdatingLocation.value = false
        }
    }

    private fun refreshPrayerTimingsFromGps(geoLoc: UserGeoLocation) {
        viewModelScope.launch {
            _prayerTimings.value = quranService.fetchPrayerTimingsByCoordinates(
                latitude = geoLoc.latitude,
                longitude = geoLoc.longitude,
                areaLabelArabic = geoLoc.areaNameArabic
            )
        }
    }

    fun clearBannerMessage() {
        _statusBannerMessage.value = null
    }

    fun selectSurah(surah: SurahMeta) {
        stopAudioPlayback()
        _selectedSurah.value = surah
        _currentPageIndex.value = 0
        _ayahs.value = QuranStaticCatalog.getOfflineFallbackAyahs(surah.number)
        viewModelScope.launch {
            _isLoadingSurah.value = true
            try {
                val fetched = quranService.fetchSurahWithTafsir(surah.number)
                if (fetched.isNotEmpty()) {
                    _ayahs.value = fetched
                }
            } finally {
                _isLoadingSurah.value = false
            }
        }
    }

    fun setMushafDisplayMode(mode: MushafDisplayMode) {
        _mushafDisplayMode.value = mode
    }

    fun setTafsirSource(source: TafsirSource) {
        _selectedTafsirSource.value = source
    }

    fun setCurrentPageIndex(index: Int) {
        _currentPageIndex.value = index.coerceAtLeast(0)
    }

    fun selectReciter(reciter: ReciterInfo) {
        stopAudioPlayback()
        _selectedReciter.value = reciter
        _statusBannerMessage.value = "تم اختيار القارئ الشيخ ${reciter.nameArabic}"
    }

    fun toggleShadowingMode() {
        _shadowingModeEnabled.value = !_shadowingModeEnabled.value
        _awaitingUserShadowRepeat.value = false
        val stateText = if (_shadowingModeEnabled.value) "مفعّل: سيتوقف الصوت بعد كل آية لتردد خلف الشيخ" else "متوقف"
        _statusBannerMessage.value = "وضع الترديد خلف الشيخ: $stateText"
    }

    fun toggleHideWordsForMemorization() {
        _hideWordsForMemorization.value = !_hideWordsForMemorization.value
    }

    fun playAyah(ayah: AyahItem) {
        _isPlayingFullSurah.value = false
        _awaitingUserShadowRepeat.value = false
        _playingAyahNumber.value = ayah.numberInSurah
        audioSpeechManager.playAyahAudio(
            reciter = _selectedReciter.value,
            ayah = ayah,
            onPrepared = {},
            onCompletion = {
                if (_shadowingModeEnabled.value) {
                    _awaitingUserShadowRepeat.value = true
                    _statusBannerMessage.value = "دورك الآن! ردد الآية (${ayah.numberInSurah}) ثم اضغط متابعة أو تحدث بالميكروفون"
                } else {
                    val list = _ayahs.value
                    val nextIndex = list.indexOfFirst { it.numberInSurah == ayah.numberInSurah } + 1
                    if (nextIndex in list.indices) {
                        playAyah(list[nextIndex])
                    } else {
                        _playingAyahNumber.value = null
                    }
                }
            },
            onError = { err ->
                _playingAyahNumber.value = null
                _statusBannerMessage.value = err
            }
        )
    }

    fun completeShadowRepeatAndAdvance() {
        val currentNum = _playingAyahNumber.value ?: return
        _awaitingUserShadowRepeat.value = false
        val list = _ayahs.value
        val nextIndex = list.indexOfFirst { it.numberInSurah == currentNum } + 1
        if (nextIndex in list.indices) {
            playAyah(list[nextIndex])
        } else {
            _playingAyahNumber.value = null
            _statusBannerMessage.value = "ما شاء الله! أتممت ترديد السورة كاملة خلف الشيخ ${_selectedReciter.value.nameArabic}"
        }
    }

    fun playFullSurah(surahNumber: Int = _selectedSurah.value.number) {
        val reciter = _selectedReciter.value
        val localFile = quranService.getLocalSurahFile(reciter.id, surahNumber)
        _playingAyahNumber.value = null
        _isPlayingFullSurah.value = true
        audioSpeechManager.playSurahAudio(
            reciter = reciter,
            surahNumber = surahNumber,
            localFile = if (localFile.exists()) localFile else null,
            onPrepared = {
                val modeStr = if (localFile.exists() && localFile.length() > 10_000L) "بدون إنترنت (محمّلة)" else "بث مباشر"
                _statusBannerMessage.value = "تشغيل سورة ${_selectedSurah.value.nameArabic} بصوت ${reciter.nameArabic} ($modeStr)"
            },
            onCompletion = {
                _isPlayingFullSurah.value = false
            },
            onError = { err ->
                _isPlayingFullSurah.value = false
                _statusBannerMessage.value = err
            }
        )
    }

    fun stopAudioPlayback() {
        audioSpeechManager.stopAudio()
        _playingAyahNumber.value = null
        _isPlayingFullSurah.value = false
        _awaitingUserShadowRepeat.value = false
    }

    // --- OFFLINE DOWNLOADS ---
    fun refreshDownloadedSurahs() {
        _downloadedSurahs.value = quranService.getDownloadedSurahs()
    }

    fun downloadSurahForOffline(reciter: ReciterInfo, surah: SurahMeta) {
        val key = "${reciter.id}_${surah.number}"
        if (_downloadProgressMap.value.containsKey(key)) return
        viewModelScope.launch {
            _downloadProgressMap.value = _downloadProgressMap.value + (key to 1)
            val result = quranService.downloadSurahAudio(reciter, surah.number) { pct ->
                _downloadProgressMap.value = _downloadProgressMap.value + (key to pct)
            }
            _downloadProgressMap.value = _downloadProgressMap.value - key
            result.onSuccess {
                refreshDownloadedSurahs()
                _statusBannerMessage.value = "تم تنزيل سورة ${surah.nameArabic} بصوت ${reciter.nameArabic} للاستماع بدون إنترنت"
            }.onFailure { e ->
                _statusBannerMessage.value = e.localizedMessage ?: "تعذر تنزيل السورة"
            }
        }
    }

    fun deleteOfflineSurah(reciterId: String, surahNumber: Int) {
        if (quranService.deleteDownloadedSurah(reciterId, surahNumber)) {
            refreshDownloadedSurahs()
            _statusBannerMessage.value = "تم حذف الملف الصوتي المحمّل"
        }
    }

    // --- VOICE RECITATION & TARTEEL TRACKING ---
    fun startLiveVoiceRecitation(targetAyah: AyahItem) {
        _isListeningMic.value = true
        _liveSpokenTranscript.value = "جارٍ الاستماع لتلاوتك..."
        audioSpeechManager.startArabicSpeechRecognition(
            onPartialTranscript = { partial ->
                _liveSpokenTranscript.value = partial
                val acc = QuranAudioAndSpeechManager.calculateRecitationAccuracy(targetAyah.textUthmani, partial)
                _lastRecitationAccuracy.value = acc
            },
            onFinalTranscript = { finalStr ->
                _isListeningMic.value = false
                _liveSpokenTranscript.value = finalStr
                val acc = QuranAudioAndSpeechManager.calculateRecitationAccuracy(targetAyah.textUthmani, finalStr)
                _lastRecitationAccuracy.value = acc
                logVoiceSession(
                    fromAyah = targetAyah.numberInSurah,
                    toAyah = targetAyah.numberInSurah,
                    accuracyScore = acc,
                    mode = if (_shadowingModeEnabled.value) "ترديد خلف الشيخ" else "تسميع صوتي مباشر"
                )
            },
            onError = { err ->
                _isListeningMic.value = false
                _statusBannerMessage.value = err
            }
        )
    }

    fun stopLiveVoiceRecitation() {
        audioSpeechManager.stopSpeechRecognition()
        _isListeningMic.value = false
    }

    fun evaluateManualOrSimulatedRecitation(targetAyah: AyahItem, spokenText: String) {
        val textToEvaluate = spokenText.ifBlank { targetAyah.textUthmani }
        _liveSpokenTranscript.value = textToEvaluate
        val accuracy = QuranAudioAndSpeechManager.calculateRecitationAccuracy(targetAyah.textUthmani, textToEvaluate)
        _lastRecitationAccuracy.value = accuracy
        logVoiceSession(
            fromAyah = targetAyah.numberInSurah,
            toAyah = targetAyah.numberInSurah,
            accuracyScore = accuracy,
            mode = if (_shadowingModeEnabled.value) "ترديد خلف الشيخ" else "تسميع وترتيل"
        )
    }

    fun logVoiceSession(fromAyah: Int, toAyah: Int, accuracyScore: Int, mode: String) {
        val surah = _selectedSurah.value
        val reciter = _selectedReciter.value
        viewModelScope.launch {
            localDao.insertRecitationSession(
                LocalRecitationEntity(
                    id = UUID.randomUUID().toString(),
                    surahNumber = surah.number.toLong(),
                    surahName = surah.nameArabic,
                    fromAyah = fromAyah.toLong(),
                    toAyah = toAyah.toLong(),
                    accuracyScore = accuracyScore.toLong(),
                    mode = mode,
                    reciterName = reciter.nameArabic
                )
            )
            if (Firebase.auth.currentUser != null) {
                cloudRepository.logRecitationSession(
                    surahNumber = surah.number.toLong(),
                    surahName = surah.nameArabic,
                    fromAyah = fromAyah.toLong(),
                    toAyah = toAyah.toLong(),
                    accuracyScore = accuracyScore.toLong(),
                    mode = mode,
                    reciterName = reciter.nameArabic
                )
            }
            _statusBannerMessage.value = "تم تسجيل جلسة التلاوة (${accuracyScore}% إتقان) في سجل ختمتك!"
        }
    }

    // --- KHATMAH & BOOKMARK OPERATIONS ---
    fun createNewKhatmahPlan(title: String, targetDays: Int, recitationMode: String) {
        viewModelScope.launch {
            val cleanTitle = title.trim().take(100).ifEmpty { "ختمة مباركة" }
            val safeDays = targetDays.toLong().coerceIn(1L, 365L)
            val safeMode = if (recitationMode in listOf("reading", "listening", "shadowing")) recitationMode else "shadowing"
            localDao.upsertKhatmahPlan(
                LocalKhatmahEntity(
                    id = UUID.randomUUID().toString(),
                    title = cleanTitle,
                    targetDays = safeDays,
                    completedPages = 0L,
                    currentSurahNumber = 1L,
                    currentAyahNumber = 1L,
                    recitationMode = safeMode
                )
            )
            if (Firebase.auth.currentUser != null) {
                cloudRepository.createKhatmahPlan(
                    title = cleanTitle,
                    targetDays = safeDays,
                    recitationMode = safeMode
                )
            }
            _statusBannerMessage.value = "تم إنشاء خطة الختمة \"$cleanTitle\" بنجاح!"
        }
    }

    fun advanceKhatmahProgress(plan: KhatmahPlan, pagesToAdd: Int) {
        val newPages = (plan.completedPages + pagesToAdd).coerceIn(0L, 604L)
        val surah = _selectedSurah.value
        val currentAyah = _playingAyahNumber.value ?: 1
        viewModelScope.launch {
            localDao.updateKhatmahProgress(
                id = plan.id,
                pages = newPages,
                surah = surah.number.toLong(),
                ayah = currentAyah.toLong()
            )
            if (Firebase.auth.currentUser != null) {
                cloudRepository.updateKhatmahProgress(
                    planId = plan.id,
                    completedPages = newPages,
                    currentSurahNumber = surah.number.toLong(),
                    currentAyahNumber = currentAyah.toLong()
                )
            }
            _statusBannerMessage.value = "تقبل الله طاعتك! تم تحديث الختمة إلى $newPages / 604 صفحة"
        }
    }

    fun deleteKhatmahPlan(planId: String) {
        viewModelScope.launch {
            localDao.deleteKhatmahPlan(planId)
            if (Firebase.auth.currentUser != null) {
                cloudRepository.deleteKhatmahPlan(planId)
            }
        }
    }

    fun saveAyahBookmark(ayah: AyahItem, note: String) {
        val surah = _selectedSurah.value
        viewModelScope.launch {
            localDao.upsertBookmark(
                LocalBookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    surahNumber = surah.number.toLong(),
                    surahName = surah.nameArabic,
                    ayahNumber = ayah.numberInSurah.toLong(),
                    pageNumber = ayah.page.toLong(),
                    ayahSnippet = ayah.textUthmani,
                    note = note.trim().take(500)
                )
            )
            if (Firebase.auth.currentUser != null) {
                cloudRepository.addBookmark(
                    surahNumber = surah.number.toLong(),
                    surahName = surah.nameArabic,
                    ayahNumber = ayah.numberInSurah.toLong(),
                    pageNumber = ayah.page.toLong(),
                    ayahSnippet = ayah.textUthmani,
                    note = note
                )
            }
            _statusBannerMessage.value = "تم حفظ الآية (${ayah.numberInSurah}) من سورة ${surah.nameArabic} في الفواصل المرجعية"
        }
    }

    fun removeBookmark(bookmarkId: String) {
        viewModelScope.launch {
            localDao.deleteBookmark(bookmarkId)
            if (Firebase.auth.currentUser != null) {
                cloudRepository.deleteBookmark(bookmarkId)
            }
        }
    }

    // --- PRAYER & ADHKAR ---
    fun selectCityForPrayer(cityPair: Pair<String, String>) {
        _selectedCityPair.value = cityPair
        refreshPrayerTimings()
    }

    fun refreshPrayerTimings() {
        val pair = _selectedCityPair.value
        viewModelScope.launch {
            _prayerTimings.value = quranService.fetchPrayerTimings(pair.first, pair.second)
        }
    }

    fun togglePrayerNotifications(enabled: Boolean) {
        _prayerNotificationsEnabled.value = enabled
    }

    fun toggleAdhkarNotifications(enabled: Boolean) {
        _adhkarNotificationsEnabled.value = enabled
    }

    fun sendInstantNotificationTest(context: Context, isPrayer: Boolean) {
        val timings = _prayerTimings.value
        val sent = if (isPrayer) {
            PrayerNotificationHelper.sendNotification(
                context = context,
                title = "حي على الصلاة — صلاة ${timings.nextPrayerName} (${timings.city})",
                body = "موعد صلاة ${timings.nextPrayerName} الساعة ${timings.nextPrayerTime}. اللهم رب هذه الدعوة التامة والصلوات القائمة آت محمداً الوسيلة والفضيلة.",
                isPrayerAlert = true
            )
        } else {
            PrayerNotificationHelper.sendNotification(
                context = context,
                title = "تذكير الورد والأذكار اليومية — نور المرتل",
                body = "«أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ» — لا تنسَ أذكارك ووردك القرآني اليومي.",
                isPrayerAlert = false
            )
        }
        _statusBannerMessage.value = if (sent) {
            "تم إرسال الإشعار بنجاح إلى شريط الإشعارات!"
        } else {
            "يرجى السماح بصلاحية الإشعارات لتلقي تنبيهات الصلاة والأذكار"
        }
    }

    fun recordAdhkarCompleted(category: String, zekrTitle: String, count: Int, target: Int) {
        viewModelScope.launch {
            localDao.insertAdhkarLog(
                LocalAdhkarLogEntity(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    zekrTitle = zekrTitle,
                    completedCount = count.toLong(),
                    targetCount = target.toLong()
                )
            )
            if (Firebase.auth.currentUser != null) {
                cloudRepository.logAdhkarCompletion(
                    category = category,
                    zekrTitle = zekrTitle,
                    completedCount = count.toLong(),
                    targetCount = target.toLong()
                )
            }
            _statusBannerMessage.value = "تقبل الله ذكرك: تم تسجيل \"$zekrTitle\" ($count مرة)"
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioSpeechManager.releaseAll()
    }
}
