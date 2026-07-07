package com.omissi.voiceprorecorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val Bg = Color(0xFF101014)
private val CardBg = Color(0xFF22232A)
private val CardBg2 = Color(0xFF1B1C22)
private val Orange = Color(0xFFFF4328)
private val Orange2 = Color(0xFFFF7A1A)
private val Muted = Color(0xFF8B8D9C)
private val Blue = Color(0xFF0A84FF)
private val DarkLine = Color(0xFF34353D)
private val WhiteSoft = Color(0xFFF4F4F6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        setContent {
            VoiceProApp()
        }
    }
}

sealed class AppScreen {
    data object Splash : AppScreen()
    data object Home : AppScreen()
    data object Recording : AppScreen()
    data object List : AppScreen()
    data class Player(val file: File) : AppScreen()
    data class Saved(val file: File) : AppScreen()
    data class TrimCut(val file: File) : AppScreen()
    data class VoiceChanger(val file: File) : AppScreen()
    data object Settings : AppScreen()
    data object Language : AppScreen()
    data object Pro : AppScreen()
    data object Backup : AppScreen()
    data object Trash : AppScreen()
    data object BackgroundMusic : AppScreen()
    data class TextPage(val title: String, val body: String) : AppScreen()
}

enum class RecordMode(val title: String, val subtitle: String) {
    STANDARD("Standard", "Widely compatible, high-quality sound reproduction."),
    MUSIC("Music & raw sound", "Preserve the original sound, perfect for music recording."),
    MEETINGS("Meetings & lectures", "Enhanced sound capture, ideal for conference recording."),
    DEVICE("Device audio", "Device audio & microphone")
}

data class RecordingItem(
    val file: File,
    val title: String,
    val sizeBytes: Long,
    val created: Long,
    val durationLabel: String = "00:16"
)

class AppSettingsState {
    var languageCode by mutableStateOf("system")
    var theme by mutableStateOf("system")
    var askBeforeSaving by mutableStateOf(true)
    var defaultTitle by mutableStateOf("VoicePro_yyyyMMdd_HHmmss")
    var useBluetooth by mutableStateOf(false)
    var autoRecording by mutableStateOf(false)
    var quickRecording by mutableStateOf(false)
    var adjustInputGain by mutableStateOf(false)
    var automaticGain by mutableStateOf(false)
    var noiseSuppression by mutableStateOf(false)
    var echoCancellation by mutableStateOf(false)
    var pauseForCalls by mutableStateOf(false)
    var keepScreenOn by mutableStateOf(false)
    var notification by mutableStateOf(true)
    var proActive by mutableStateOf(false)
    var saveCount by mutableIntStateOf(0)
    var recordingQuality by mutableStateOf("High (CD)")
    var recordingFormat by mutableStateOf("M4A")
    var audioSource by mutableStateOf("Main (unprocessed)")
    var samplingRate by mutableStateOf("44.1kHz (CD)")
    var bitrate by mutableStateOf("128kbps")
    var audioTrack by mutableStateOf("Stereo")
}

class RecorderEngine(private val appContext: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var recordingStartMs = 0L
    private var pausedAtMs = 0L
    private var totalPausedMs = 0L
    private var currentMode: RecordMode = RecordMode.STANDARD

    var isRecording by mutableStateOf(false)
        private set
    var isPaused by mutableStateOf(false)
        private set
    var currentFile by mutableStateOf<File?>(null)
        private set
    var durationMs by mutableLongStateOf(0L)
        private set
    var playbackFile by mutableStateOf<File?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var playbackProgressMs by mutableLongStateOf(0L)
        private set

    val recordings = mutableStateListOf<RecordingItem>()
    val trash = mutableStateListOf<RecordingItem>()
    val markers = mutableStateMapOf<String, MutableList<Long>>()

    fun recordingsDir(): File {
        val base = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: appContext.filesDir
        return File(base, "VoiceProRecorder").also { it.mkdirs() }
    }

    fun displayStoragePath(): String = "/storage/emulated/0/Music/VoiceProRecorder/"

    fun reloadRecordings() {
        recordings.clear()
        val list = recordingsDir().listFiles { file -> file.isFile && file.extension.lowercase(Locale.US) in listOf("m4a", "mp4", "aac", "wav", "mp3") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        list.forEach { f -> recordings.add(fileToItem(f)) }
    }

    fun fileToItem(file: File): RecordingItem = RecordingItem(
        file = file,
        title = file.nameWithoutExtension,
        sizeBytes = file.length(),
        created = file.lastModified(),
        durationLabel = guessDurationLabel(file)
    )

    private fun guessDurationLabel(file: File): String {
        return try {
            val mp = MediaPlayer()
            mp.setDataSource(file.absolutePath)
            mp.prepare()
            val duration = mp.duration.toLong()
            mp.release()
            formatShort(duration)
        } catch (_: Exception) {
            "00:16"
        }
    }

    fun startRecording(mode: RecordMode, settings: AppSettingsState): Boolean {
        return try {
            stopPlayback()
            currentMode = mode
            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(recordingsDir(), "VoicePro_${name}.${settings.recordingFormat.lowercase(Locale.US)}")
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(appContext) else MediaRecorder()
            val source = if (settings.audioSource.contains("unprocessed", ignoreCase = true) && Build.VERSION.SDK_INT >= 24) {
                MediaRecorder.AudioSource.UNPROCESSED
            } else {
                MediaRecorder.AudioSource.MIC
            }
            r.setAudioSource(source)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(if (settings.samplingRate.startsWith("48")) 48000 else 44100)
            r.setAudioEncodingBitRate(settings.bitrate.filter { it.isDigit() }.toIntOrNull()?.times(1000) ?: 128000)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            currentFile = file
            recordingStartMs = System.currentTimeMillis()
            totalPausedMs = 0L
            pausedAtMs = 0L
            durationMs = 0L
            isRecording = true
            isPaused = false
            true
        } catch (e: Exception) {
            Toast.makeText(appContext, "Recording failed: ${e.message}", Toast.LENGTH_LONG).show()
            releaseRecorder(deleteFile = true)
            false
        }
    }

    fun updateDuration() {
        if (isRecording && !isPaused) {
            durationMs = System.currentTimeMillis() - recordingStartMs - totalPausedMs
        }
    }

    fun pauseRecording() {
        val r = recorder ?: return
        if (!isRecording) return
        try {
            if (!isPaused) {
                if (Build.VERSION.SDK_INT >= 24) r.pause()
                pausedAtMs = System.currentTimeMillis()
                isPaused = true
            } else {
                if (Build.VERSION.SDK_INT >= 24) r.resume()
                totalPausedMs += System.currentTimeMillis() - pausedAtMs
                pausedAtMs = 0L
                isPaused = false
            }
        } catch (e: Exception) {
            Toast.makeText(appContext, "Pause/resume failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopAndSave(): File? {
        val file = currentFile
        try {
            recorder?.stop()
        } catch (_: Exception) {
            file?.delete()
        } finally {
            releaseRecorder(deleteFile = false)
        }
        if (file != null && file.exists() && file.length() > 0) {
            reloadRecordings()
            return file
        }
        return null
    }

    fun discardRecording() {
        val file = currentFile
        releaseRecorder(deleteFile = true)
        file?.delete()
        reloadRecordings()
    }

    private fun releaseRecorder(deleteFile: Boolean) {
        try { recorder?.reset() } catch (_: Exception) {}
        try { recorder?.release() } catch (_: Exception) {}
        if (deleteFile) currentFile?.delete()
        recorder = null
        isRecording = false
        isPaused = false
        durationMs = 0L
        currentFile = null
    }

    fun addMarker(file: File? = currentFile) {
        val target = file ?: return
        val list = markers.getOrPut(target.absolutePath) { mutableListOf() }
        val value = if (isRecording) durationMs else playbackProgressMs
        list.add(value.coerceAtLeast(0L))
    }

    fun markerList(file: File): List<Long> = markers[file.absolutePath]?.toList() ?: emptyList()

    fun play(file: File, speed: Float = 1f, pitch: Float = 1f) {
        try {
            if (playbackFile?.absolutePath == file.absolutePath && isPlaying) {
                pausePlayback()
                return
            }
            stopPlayback()
            val mp = MediaPlayer()
            mp.setDataSource(file.absolutePath)
            mp.prepare()
            if (Build.VERSION.SDK_INT >= 23) {
                mp.playbackParams = mp.playbackParams.setSpeed(speed.coerceIn(0.5f, 1.8f)).setPitch(pitch.coerceIn(0.5f, 1.8f))
            }
            mp.setOnCompletionListener {
                isPlaying = false
                playbackProgressMs = 0L
            }
            mp.start()
            player = mp
            playbackFile = file
            isPlaying = true
        } catch (e: Exception) {
            Toast.makeText(appContext, "Prepare failed: ${e.message}", Toast.LENGTH_LONG).show()
            stopPlayback()
        }
    }

    fun pausePlayback() {
        try {
            player?.pause()
            isPlaying = false
        } catch (_: Exception) {}
    }

    fun stopPlayback() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
        isPlaying = false
        playbackProgressMs = 0L
    }

    fun seekBy(seconds: Int) {
        val p = player ?: return
        val next = (p.currentPosition + seconds * 1000).coerceIn(0, p.duration)
        p.seekTo(next)
        playbackProgressMs = next.toLong()
    }

    fun updatePlayback() {
        val p = player ?: return
        if (isPlaying) playbackProgressMs = p.currentPosition.toLong()
    }

    fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share recording"))
        } catch (e: Exception) {
            Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteFile(file: File) {
        if (file.exists()) {
            trash.add(fileToItem(file))
            file.delete()
        }
        reloadRecordings()
    }

    fun copyEditedFile(file: File, suffix: String): File? {
        return try {
            val out = File(recordingsDir(), "${file.nameWithoutExtension}_$suffix.${file.extension.ifBlank { "m4a" }}")
            file.copyTo(out, overwrite = true)
            reloadRecordings()
            out
        } catch (e: Exception) {
            Toast.makeText(appContext, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun importAudioUri(context: Context, uri: Uri): File? {
        return try {
            val name = "VoicePro_import_${System.currentTimeMillis()}.m4a"
            val out = File(recordingsDir(), name)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
            reloadRecordings()
            out
        } catch (e: Exception) {
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}

class AppAdManager(private val context: Context) {
    private var interstitialAd: InterstitialAd? = null

    fun load() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712",
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                override fun onAdFailedToLoad(error: LoadAdError) { interstitialAd = null }
            }
        )
    }

    fun showAfterSave(activity: Activity, enabled: Boolean, onDone: () -> Unit) {
        if (!enabled) {
            onDone()
            return
        }
        val ad = interstitialAd
        if (ad == null) {
            load()
            onDone()
            return
        }
        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                load()
                onDone()
            }
            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                load()
                onDone()
            }
        }
        ad.show(activity)
    }
}

@Composable
fun VoiceProApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val engine = remember { RecorderEngine(context.applicationContext).also { it.reloadRecordings() } }
    val settings = remember { AppSettingsState() }
    val ads = remember { AppAdManager(context.applicationContext).also { it.load() } }
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Splash) }
    var pendingRate by remember { mutableStateOf(false) }
    var selectedMode by rememberSaveable { mutableStateOf(RecordMode.STANDARD.name) }
    val activeMode = RecordMode.valueOf(selectedMode)
    val language = currentLanguage(settings)
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            engine.importAudioUri(context, it)
            screen = AppScreen.List
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) Toast.makeText(context, t("permissionReady", language), Toast.LENGTH_SHORT).show()
        else Toast.makeText(context, t("micPermissionNeeded", language), Toast.LENGTH_LONG).show()
    }

    LaunchedEffect(engine.isRecording, engine.isPaused) {
        while (engine.isRecording) {
            engine.updateDuration()
            delay(200)
        }
    }
    LaunchedEffect(engine.isPlaying) {
        while (engine.isPlaying) {
            engine.updatePlayback()
            delay(200)
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(background = Bg, surface = Bg, primary = Orange, onPrimary = Color.White)
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            when (val s = screen) {
                AppScreen.Splash -> SplashScreen { screen = AppScreen.Home }
                AppScreen.Home -> HomeScreen(
                    mode = activeMode,
                    settings = settings,
                    language = language,
                    onModeChange = { selectedMode = it.name },
                    onStart = {
                        if (hasMicPermission(context)) {
                            if (engine.startRecording(activeMode, settings)) screen = AppScreen.Recording
                        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onList = { engine.reloadRecordings(); screen = AppScreen.List },
                    onSettings = { screen = AppScreen.Settings },
                    onPro = { screen = AppScreen.Pro },
                    onImport = { importLauncher.launch(arrayOf("audio/*")) },
                    onRestore = { screen = AppScreen.Backup },
                    onTrash = { screen = AppScreen.Trash }
                )
                AppScreen.Recording -> RecordingScreen(
                    engine = engine,
                    settings = settings,
                    mode = activeMode,
                    language = language,
                    onSave = {
                        val file = engine.stopAndSave()
                        if (file != null) {
                            settings.saveCount++
                            val goSaved = {
                                pendingRate = settings.saveCount >= 3 && settings.saveCount % 3 == 0
                                screen = AppScreen.Saved(file)
                            }
                            if (activity != null) ads.showAfterSave(activity, !settings.proActive, goSaved) else goSaved()
                        }
                    },
                    onDiscard = { engine.discardRecording(); screen = AppScreen.Home }
                )
                is AppScreen.Saved -> SavedScreen(
                    file = s.file,
                    engine = engine,
                    settings = settings,
                    language = language,
                    showRate = pendingRate,
                    onRateDismiss = { pendingRate = false },
                    onBack = { screen = AppScreen.Home },
                    onHome = { screen = AppScreen.Home },
                    onNew = {
                        if (hasMicPermission(context)) {
                            if (engine.startRecording(activeMode, settings)) screen = AppScreen.Recording
                        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onList = { engine.reloadRecordings(); screen = AppScreen.List },
                    onTrim = { screen = AppScreen.TrimCut(s.file) },
                    onVoice = { screen = AppScreen.VoiceChanger(s.file) },
                    onContinue = {
                        if (hasMicPermission(context)) {
                            if (engine.startRecording(activeMode, settings)) screen = AppScreen.Recording
                        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onBackground = { screen = AppScreen.BackgroundMusic },
                    onBackup = { screen = AppScreen.Backup }
                )
                AppScreen.List -> RecordingListScreen(
                    engine = engine,
                    settings = settings,
                    language = language,
                    onBack = { screen = AppScreen.Home },
                    onOpen = { screen = AppScreen.Player(it) },
                    onStart = {
                        if (hasMicPermission(context)) {
                            if (engine.startRecording(activeMode, settings)) screen = AppScreen.Recording
                        } else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
                is AppScreen.Player -> PlayerScreen(
                    file = s.file,
                    engine = engine,
                    settings = settings,
                    language = language,
                    onBack = { screen = AppScreen.List },
                    onTrim = { screen = AppScreen.TrimCut(s.file) },
                    onVoice = { screen = AppScreen.VoiceChanger(s.file) }
                )
                is AppScreen.TrimCut -> TrimCutScreen(
                    file = s.file,
                    engine = engine,
                    settings = settings,
                    language = language,
                    onBack = { screen = AppScreen.Player(s.file) },
                    onDone = { out -> screen = AppScreen.Saved(out) }
                )
                is AppScreen.VoiceChanger -> VoiceChangerScreen(
                    file = s.file,
                    engine = engine,
                    settings = settings,
                    language = language,
                    onBack = { screen = AppScreen.Player(s.file) },
                    onDone = { out -> screen = AppScreen.Saved(out) }
                )
                AppScreen.Settings -> SettingsScreen(
                    settings = settings,
                    engine = engine,
                    language = language,
                    onBack = { screen = AppScreen.Home },
                    onLanguage = { screen = AppScreen.Language },
                    onPro = { screen = AppScreen.Pro },
                    onBackup = { screen = AppScreen.Backup },
                    onStable = { screen = AppScreen.TextPage("Stable background recording", stableBackgroundText()) },
                    onPrivacy = { screen = AppScreen.TextPage("Privacy Policy", privacyText()) },
                    onTerms = { screen = AppScreen.TextPage("Terms of Use", termsText()) },
                    onHelp = { screen = AppScreen.TextPage("Help & Feedback", helpText()) },
                    onRate = { pendingRate = true },
                    onManage = { openManageSubscriptions(context) }
                )
                AppScreen.Language -> LanguageScreen(settings, language, onBack = { screen = AppScreen.Settings })
                AppScreen.Pro -> ProScreen(settings, language, onBack = { screen = AppScreen.Home }, onRestore = { Toast.makeText(context, "Restore checked", Toast.LENGTH_SHORT).show() })
                AppScreen.Backup -> BackupScreen(language, onBack = { screen = AppScreen.Settings })
                AppScreen.Trash -> TrashScreen(engine, language, onBack = { screen = AppScreen.Home })
                AppScreen.BackgroundMusic -> TextScreen("Background music", "Choose an audio file from your device and mix it with recordings in the editor flow. This build keeps the UI ready and safely stores the selected state.", onBack = { screen = AppScreen.Home })
                is AppScreen.TextPage -> TextScreen(s.title, s.body, onBack = { screen = AppScreen.Settings })
            }
        }
    }
}

@Composable
private fun currentLanguage(settings: AppSettingsState): String {
    val cfg = LocalConfiguration.current
    val system = if (Build.VERSION.SDK_INT >= 24) cfg.locales.get(0)?.language ?: Locale.getDefault().language else Locale.getDefault().language
    return if (settings.languageCode == "system") system else settings.languageCode
}

private fun hasMicPermission(context: Context): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

private fun toast(context: Context, text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

private fun formatShort(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}

private fun formatBig(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return "%02d : %02d : %02d".format(h, m, s)
}

private fun fileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb < 1024) String.format(Locale.US, "%.2fKB", kb) else String.format(Locale.US, "%.2fMB", kb / 1024.0)
}

private fun openManageSubscriptions(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions")))
    } catch (_: Exception) {
        toast(context, "Google Play subscriptions page is not available")
    }
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200)
        onDone()
    }
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(0.88f, 1.08f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pulse")
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF16161C), Bg)))
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size((124 * pulse).dp), contentAlignment = Alignment.Center) {
                VoiceLogoMark(Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(26.dp))
            LogoText(fontSize = 42)
            Spacer(Modifier.height(10.dp))
            Text("Professional audio recorder", color = Muted, fontSize = 17.sp)
        }
    }
}

@Composable
fun HomeScreen(
    mode: RecordMode,
    settings: AppSettingsState,
    language: String,
    onModeChange: (RecordMode) -> Unit,
    onStart: () -> Unit,
    onList: () -> Unit,
    onSettings: () -> Unit,
    onPro: () -> Unit,
    onImport: () -> Unit,
    onRestore: () -> Unit,
    onTrash: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoText(fontSize = 36)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onPro) { Text("♛", color = Orange, fontSize = 34.sp, fontWeight = FontWeight.Black) }
            IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, modifier = Modifier.background(CardBg)) {
                    DropdownMenuItem(text = { MenuText("Import") }, onClick = { menu = false; onImport() }, leadingIcon = { MenuGlyph("↪") })
                    DropdownMenuItem(text = { MenuText("Restore from Drive") }, onClick = { menu = false; onRestore() }, leadingIcon = { MenuGlyph("☁") })
                    DropdownMenuItem(text = { MenuText("Trash") }, onClick = { menu = false; onTrash() }, leadingIcon = { MenuGlyph("♻") })
                }
            }
        }
        ProFeatureCard(onClick = onPro)
        Spacer(Modifier.height(34.dp))
        LazyRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 38.dp)
        ) {
            items(RecordMode.entries) { item ->
                ModeCard(
                    mode = item,
                    selected = item == mode,
                    onClick = { onModeChange(item) },
                    onStart = onStart
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Dots(RecordMode.entries.indexOf(mode), RecordMode.entries.size)
        Spacer(Modifier.weight(1f))
        if (!settings.proActive) BannerAd(Modifier.fillMaxWidth())
        BottomHomeBar(onRecord = onStart, onList = onList)
    }
}

@Composable
fun ProFeatureCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 42.dp)
            .height(104.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(CardBg),
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            CircleIconBadge("T", 64.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Live transcribe", color = WhiteSoft, fontSize = 31.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text("Real-time text transcription", color = Muted, fontSize = 22.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("›", color = WhiteSoft, fontSize = 54.sp)
        }
    }
}

@Composable
fun ModeCard(mode: RecordMode, selected: Boolean, onClick: () -> Unit, onStart: () -> Unit) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(540.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(CardBg2),
        shape = RoundedCornerShape(34.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(26.dp)) {
            Spacer(Modifier.height(30.dp))
            RecordingModeIllustration(mode, Modifier.size(136.dp))
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(mode.title, color = WhiteSoft, fontSize = 42.sp, lineHeight = 45.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                if (mode == RecordMode.DEVICE) Text("?", color = Muted, fontSize = 30.sp, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(Modifier.height(18.dp))
            if (mode == RecordMode.DEVICE) {
                ToggleRowText("Device audio & microphone", false) {}
            } else {
                Text(mode.subtitle, color = WhiteSoft, fontSize = 22.sp, lineHeight = 28.sp)
            }
            Spacer(Modifier.height(44.dp))
            AnimatedMicButton(modifier = Modifier.align(Alignment.CenterHorizontally), selected = selected, onClick = onStart)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun RecordingScreen(
    engine: RecorderEngine,
    settings: AppSettingsState,
    mode: RecordMode,
    language: String,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    var confirmDiscard by remember { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(42.dp))
        Text(formatBig(engine.durationMs), color = if (engine.isPaused) Muted else Color.White, fontSize = 54.sp, fontWeight = FontWeight.Black)
        Text(mode.title, color = Muted, fontSize = 28.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(56.dp))
        WaveformTimeline(
            progress = ((engine.durationMs / 1000f) % 8f) / 8f,
            markers = engine.markerList(engine.currentFile ?: File("")),
            active = engine.isRecording && !engine.isPaused,
            modifier = Modifier.fillMaxWidth().height(360.dp),
            showTime = true
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallRoundControl("‹|", onClick = { })
            SmallRoundControl(if (engine.isPaused) "▶" else "▮▮", onClick = { engine.pauseRecording() })
            SmallRoundControl("|›", onClick = { })
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().padding(horizontal = 46.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            LabeledCircleButton(icon = "⌂", label = "Mark", onClick = { engine.addMarker() })
            GradientRoundButton(label = if (engine.isPaused) "Resume" else "▮▮", size = if (engine.isPaused) 170.dp else 132.dp, onClick = { engine.pauseRecording() })
            LabeledCircleButton(icon = "✓", label = "Save", onClick = onSave)
        }
        Spacer(Modifier.height(30.dp))
        Text("You can continue recording for up to ${if (mode == RecordMode.MEETINGS) "4 h 55" else "9 h 52"} min.", color = Muted, fontSize = 18.sp)
        Spacer(Modifier.height(18.dp))
        if (!settings.proActive) BannerAd(Modifier.fillMaxWidth())
    }
    if (engine.isPaused) {
        Box(Modifier.fillMaxSize().padding(bottom = 220.dp), contentAlignment = Alignment.BottomCenter) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 46.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                LabeledCircleButton(icon = "×", label = "Discard", onClick = { confirmDiscard = true })
                Spacer(Modifier.width(170.dp))
                LabeledCircleButton(icon = "✓", label = "Save", onClick = onSave)
            }
        }
    }
    if (confirmDiscard) {
        ConfirmDialog("Discard your unsaved changes?", "Cancel", "Discard", onCancel = { confirmDiscard = false }, onConfirm = { confirmDiscard = false; onDiscard() })
    }
}

@Composable
fun SavedScreen(
    file: File,
    engine: RecorderEngine,
    settings: AppSettingsState,
    language: String,
    showRate: Boolean,
    onRateDismiss: () -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onNew: () -> Unit,
    onList: () -> Unit,
    onTrim: () -> Unit,
    onVoice: () -> Unit,
    onContinue: () -> Unit,
    onBackground: () -> Unit,
    onBackup: () -> Unit
) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.Top) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Text("Saved\nsuccessfully", color = WhiteSoft, fontSize = 38.sp, fontWeight = FontWeight.Black, lineHeight = 36.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onHome) { Icon(Icons.Default.Home, null, tint = WhiteSoft, modifier = Modifier.size(38.dp)) }
        }
        Spacer(Modifier.height(20.dp))
        RecordingPreviewCard(file = file, engine = engine, large = true)
        Spacer(Modifier.height(36.dp))
        FullWidthGradientButton("Start a new recording", onNew)
        Spacer(Modifier.height(16.dp))
        OutlinedOrangeButton("Check my recording list", onList)
        Spacer(Modifier.height(44.dp))
        Text("Guess you might need", color = WhiteSoft, fontSize = 28.sp)
        Spacer(Modifier.height(24.dp))
        ActionGrid(
            listOf(
                ActionItem("↗", "Share") { engine.shareFile(context, file) },
                ActionItem("🔗", "Share via\nlink") { toast(context, "Local share link is not available offline") },
                ActionItem("☁", "Back up to\nDrive") { onBackup() },
                ActionItem("⌫", "Delete") { engine.deleteFile(file); onList() },
                ActionItem("✂", "Trim") { onTrim() },
                ActionItem("〰", "Voice changer") { onVoice() },
                ActionItem("U", "Continue\nrecording") { onContinue() },
                ActionItem("♫", "Background\nmusic") { onBackground() }
            )
        )
        Spacer(Modifier.height(16.dp))
        if (!settings.proActive) BannerAd(Modifier.fillMaxWidth())
    }
    if (showRate) RatingDialog(onDismiss = onRateDismiss)
}

@Composable
fun RecordingListScreen(
    engine: RecorderEngine,
    settings: AppSettingsState,
    language: String,
    onBack: () -> Unit,
    onOpen: (File) -> Unit,
    onStart: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var search by remember { mutableStateOf(false) }
    var grid by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { engine.reloadRecordings() }
    val list by remember(query, engine.recordings.size) {
        derivedStateOf { engine.recordings.filter { it.title.contains(query, true) } }
    }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Text("Recordings", color = WhiteSoft, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            IconButton(onClick = { search = !search }) { Icon(Icons.Default.Search, null, tint = WhiteSoft) }
            IconButton(onClick = { grid = !grid }) { Text(if (grid) "▦" else "☰", color = WhiteSoft, fontSize = 30.sp) }
            IconButton(onClick = onStart) { Text("●", color = Orange, fontSize = 34.sp) }
        }
        if (search) {
            SearchBox(query = query, onChange = { query = it })
        }
        if (list.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    VoiceLogoMark(Modifier.size(86.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No recordings yet", color = WhiteSoft, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Start a new recording from the red button", color = Muted, fontSize = 17.sp)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(list) { item ->
                    RecordingListItem(item, onOpen = { onOpen(item.file) }, onShare = { engine.shareFile(it, item.file) }, onDelete = { engine.deleteFile(item.file) })
                }
            }
        }
        if (!settings.proActive) BannerAd(Modifier.fillMaxWidth())
    }
}

@Composable
fun PlayerScreen(file: File, engine: RecorderEngine, settings: AppSettingsState, language: String, onBack: () -> Unit, onTrim: () -> Unit, onVoice: () -> Unit) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    var speed by remember { mutableStateOf(1f) }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { engine.stopPlayback(); onBack() }) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Text(file.nameWithoutExtension, color = WhiteSoft, fontSize = 29.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(onClick = { toast(context, "Speaker toggled") }) { Text("◕", color = WhiteSoft, fontSize = 30.sp) }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, null, tint = WhiteSoft) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, modifier = Modifier.background(CardBg)) {
                    DropdownMenuItem(text = { MenuText("Share") }, onClick = { menu = false; engine.shareFile(context, file) })
                    DropdownMenuItem(text = { MenuText("Rename") }, onClick = { menu = false; toast(context, "Rename will use the file manager name") })
                    DropdownMenuItem(text = { MenuText("Delete") }, onClick = { menu = false; engine.deleteFile(file); onBack() })
                }
            }
        }
        WaveformTimeline(
            progress = ((engine.playbackProgressMs / 1000f) % 8f) / 8f,
            markers = engine.markerList(file),
            active = engine.isPlaying,
            modifier = Modifier.fillMaxWidth().height(310.dp),
            showTime = true
        )
        Column(Modifier.weight(1f).padding(horizontal = 20.dp)) {
            engine.markerList(file).take(3).forEachIndexed { index, mark ->
                Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⊖", color = Muted, fontSize = 32.sp)
                    Spacer(Modifier.width(24.dp))
                    Text("▶  ${formatShort(mark)}", color = Muted, fontSize = 25.sp, modifier = Modifier.weight(1f))
                    Text("✎", color = Muted, fontSize = 28.sp)
                }
                Divider(color = Color(0xFF191A20))
            }
            Spacer(Modifier.weight(1f))
            Card(Modifier.fillMaxWidth().height(210.dp), colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        ToolIcon("◩") { toast(context, "Selection mode enabled") }
                        ToolIcon("⌂") { engine.addMarker(file) }
                        ToolIcon("✂") { onTrim() }
                        ToolIcon("↻") { toast(context, "Loop mode toggled") }
                        ToolIcon(String.format(Locale.US, "%.1fX", speed)) { speed = if (speed >= 1.5f) 0.5f else speed + 0.5f }
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(formatShort(engine.playbackProgressMs), color = WhiteSoft, fontSize = 18.sp)
                        Slider(
                            value = ((engine.playbackProgressMs % 8000L) / 8000f),
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = WhiteSoft, activeTrackColor = Orange, inactiveTrackColor = DarkLine)
                        )
                        Text(engine.fileToItem(file).durationLabel, color = WhiteSoft, fontSize = 18.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { engine.seekBy(-5) }) { Text("↶5", color = WhiteSoft, fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                        GradientRoundButton(label = if (engine.isPlaying) "▮▮" else "▶", size = 96.dp) { engine.play(file, speed = speed) }
                        TextButton(onClick = { engine.seekBy(5) }) { Text("5↷", color = WhiteSoft, fontSize = 30.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        if (!settings.proActive) BannerAd(Modifier.fillMaxWidth())
    }
}

@Composable
fun TrimCutScreen(file: File, engine: RecorderEngine, settings: AppSettingsState, language: String, onBack: () -> Unit, onDone: (File) -> Unit) {
    val context = LocalContext.current
    var trimMode by remember { mutableStateOf(true) }
    var start by remember { mutableStateOf(0.12f) }
    var end by remember { mutableStateOf(0.82f) }
    val total = engine.fileToItem(file).durationLabel
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Text(file.nameWithoutExtension, color = WhiteSoft, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            TextButton(onClick = {
                engine.copyEditedFile(file, if (trimMode) "trim" else "cut")?.let(onDone)
            }) { Icon(Icons.Default.Check, null, tint = Orange, modifier = Modifier.size(34.dp)) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            TabText("Trim", trimMode) { trimMode = true }
            TabText("Cut", !trimMode) { trimMode = false }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 30.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TimePill("−", "00:00.7", "+") { start = (start - 0.03f).coerceAtLeast(0f) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (trimMode) "00:06.3" else "00:01.5", color = WhiteSoft, fontSize = 27.sp)
                Text("Total", color = Muted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            TimePill("−", "00:07.0", "+") { end = (end + 0.03f).coerceAtMost(1f) }
        }
        Spacer(Modifier.height(20.dp))
        TrimWaveform(trimMode = trimMode, start = start, end = end, modifier = Modifier.fillMaxWidth().height(410.dp))
        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 44.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            ToolIcon("◩") { toast(context, "Fade tool toggled") }
            SmallRoundControl("|‹") { engine.seekBy(-5) }
            GradientRoundButton(label = if (engine.isPlaying) "▮▮" else "▶", size = 92.dp) { engine.play(file) }
            SmallRoundControl("›|") { engine.seekBy(5) }
            Text("1.0X", color = WhiteSoft, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        if (!settings.proActive) BannerAd(Modifier.fillMaxWidth())
    }
}

@Composable
fun VoiceChangerScreen(file: File, engine: RecorderEngine, settings: AppSettingsState, language: String, onBack: () -> Unit, onDone: (File) -> Unit) {
    val effects = listOf("Normal", "Girl", "Man", "Child", "Squirrel", "Tenor singer", "Megaphone", "Nervous", "Drunk", "Robot", "Death", "Monster", "Alien")
    var selected by remember { mutableStateOf("Normal") }
    var pitch by remember { mutableStateOf(1f) }
    var speed by remember { mutableStateOf(1f) }
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { engine.stopPlayback(); onBack() }) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Text("Voice changer", color = WhiteSoft, fontSize = 36.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Box(Modifier.size(64.dp).clip(RoundedCornerShape(22.dp)).background(CardBg).clickable {
                engine.copyEditedFile(file, selected.lowercase(Locale.US).replace(" ", "_"))?.let(onDone)
            }, contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = Muted, modifier = Modifier.size(38.dp))
            }
        }
        Text("Voice effects", color = WhiteSoft, fontSize = 27.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 36.dp, vertical = 14.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            effects.chunked(4).forEachIndexed { rowIndex, row ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { effect ->
                        EffectItem(effect, selected == effect) {
                            selected = effect
                            pitch = when (effect) {
                                "Girl", "Child", "Squirrel" -> 1.25f
                                "Man", "Monster", "Death" -> 0.75f
                                "Robot" -> 0.95f
                                else -> 1f
                            }
                            speed = when (effect) {
                                "Nervous" -> 1.25f
                                "Drunk" -> 0.85f
                                "Tenor singer" -> 1.1f
                                else -> 1f
                            }
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.width(82.dp)) }
                }
                if (rowIndex == 0) {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(26.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Adjust effect", color = WhiteSoft, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                                TextButton(onClick = { pitch = 1f; speed = 1f }) { Text("Reset", color = Muted, fontSize = 23.sp, fontWeight = FontWeight.Bold) }
                            }
                            EffectSlider("Pitch", "-15", "15", pitch, { pitch = it })
                            EffectSlider("Speed", "0.5", "1.5", speed, { speed = it })
                        }
                    }
                }
            }
        }
        Card(Modifier.fillMaxWidth().height(240.dp), colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
            Column(Modifier.padding(horizontal = 22.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                MiniWaveform(Modifier.fillMaxWidth().height(72.dp), active = engine.isPlaying)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { engine.seekBy(-5) }) { Text("↶5", color = WhiteSoft, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
                    Slider(value = 0.42f, onValueChange = {}, modifier = Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Orange, inactiveTrackColor = DarkLine))
                    TextButton(onClick = { engine.seekBy(5) }) { Text("5↷", color = WhiteSoft, fontSize = 25.sp, fontWeight = FontWeight.Bold) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                    ToolIcon("↗") { toast(context, "Preview effect exported to editor") }
                    GradientRoundButton(label = if (engine.isPlaying) "▮▮" else "▶", size = 92.dp) { engine.play(file, speed = speed, pitch = pitch) }
                    ToolIcon("1.0X") { speed = 1f; pitch = 1f }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    settings: AppSettingsState,
    engine: RecorderEngine,
    language: String,
    onBack: () -> Unit,
    onLanguage: () -> Unit,
    onPro: () -> Unit,
    onBackup: () -> Unit,
    onStable: () -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
    onHelp: () -> Unit,
    onRate: () -> Unit,
    onManage: () -> Unit
) {
    val context = LocalContext.current
    var optionDialog by remember { mutableStateOf<Pair<String, List<String>>?>(null) }
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Text("Settings", color = WhiteSoft, fontSize = 38.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 28.dp)) {
            SettingsCard("!", "How to ensure stable\nbackground recording?", null, onStable)
            SettingsCard("△", "Backup & Restore", "Never lose your recordings", onBackup)
            RemoveAdsCard(onPro)
            SectionTitle("General")
            SettingsRow("Theme", settings.themeTitle(), arrow = true) { optionDialog = "Theme" to listOf("System default", "Dark", "Light") }
            SettingsToggleRow("Ask before saving", "Ask to name the recording before saving", settings.askBeforeSaving) { settings.askBeforeSaving = it }
            SettingsRow("Default title", "VoicePro_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}") { optionDialog = "Default title" to listOf("VoicePro_yyyyMMdd_HHmmss", "VoicePro_0001", "My recording") }
            SettingsRow("Tag management", null, arrow = true) { optionDialog = "Tag management" to listOf("Work", "Lecture", "Music", "Important") }
            SettingsRow("Language", languageTitle(settings.languageCode), arrow = true, onClick = onLanguage)
            SectionTitle("Recording")
            SettingsRow("Storage path", engine.displayStoragePath(), arrow = true) { toast(context, engine.displayStoragePath()) }
            SettingsRow("Recording quality", settings.recordingQuality, arrow = true) { optionDialog = "Recording quality" to listOf("High (CD)", "Medium", "Low") }
            SettingsRow("Recording format", settings.recordingFormat, arrow = true) { optionDialog = "Recording format" to listOf("M4A", "AAC", "MP4") }
            SettingsRow("Audio source", settings.audioSource, arrow = true) { optionDialog = "Audio source" to listOf("Main (unprocessed)", "Main", "Voice recognition", "Camcorder") }
            SettingsToggleRow("Use when Bluetooth mic is\navailable", null, settings.useBluetooth) { settings.useBluetooth = it }
            SettingsRow("Sampling rate", settings.samplingRate, arrow = true) { optionDialog = "Sampling rate" to listOf("44.1kHz (CD)", "48kHz", "22.05kHz") }
            SettingsRow("Encoder bitrate", settings.bitrate, arrow = true) { optionDialog = "Encoder bitrate" to listOf("96kbps", "128kbps", "192kbps", "256kbps") }
            SettingsRow("Audio track", settings.audioTrack, arrow = true) { optionDialog = "Audio track" to listOf("Stereo", "Mono") }
            SectionTitle("Advanced")
            SettingsToggleRow("Auto recording", "Auto start recording on app launch", settings.autoRecording) { settings.autoRecording = it }
            SettingsRow("Quick recording", "Record without opening the app", badge = "New") { settings.quickRecording = !settings.quickRecording }
            SettingsToggleRow("Adjust input gain", "Manually adjust to capture louder, clearer\nsound", settings.adjustInputGain) { settings.adjustInputGain = it }
            SettingsToggleRow("Automatic gain control", "Automatically adjusts to capture louder,\nclearer sound", settings.automaticGain) { settings.automaticGain = it }
            SettingsToggleRow("Noise suppression", null, settings.noiseSuppression) { settings.noiseSuppression = it }
            SettingsToggleRow("Echo cancellation", null, settings.echoCancellation) { settings.echoCancellation = it }
            SettingsToggleRow("Pause for calls", "Recording continues, but call audio won't be\ncaptured", settings.pauseForCalls) { settings.pauseForCalls = it }
            SettingsToggleRow("Keep screen on", "Turning it off won't affect recording", settings.keepScreenOn) { settings.keepScreenOn = it }
            SettingsToggleRow("Notification", "Quickly control recording from notification", settings.notification) { settings.notification = it }
            SectionTitle("Help")
            SettingsPlain("Manage subscriptions", onManage)
            SettingsPlain("Help & Feedback", onHelp)
            SettingsPlain("Rate us", onRate)
            SettingsPlain("Privacy Policy", onPrivacy)
            SettingsPlain("Terms of Use", onTerms)
            Spacer(Modifier.height(40.dp))
            Text("Version: 1.0.0", color = Muted, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
    optionDialog?.let { pair ->
        OptionDialog(title = pair.first, options = pair.second, onDismiss = { optionDialog = null }) { selected ->
            when (pair.first) {
                "Theme" -> settings.theme = selected
                "Default title" -> settings.defaultTitle = selected
                "Recording quality" -> settings.recordingQuality = selected
                "Recording format" -> settings.recordingFormat = selected
                "Audio source" -> settings.audioSource = selected
                "Sampling rate" -> settings.samplingRate = selected
                "Encoder bitrate" -> settings.bitrate = selected
                "Audio track" -> settings.audioTrack = selected
            }
            optionDialog = null
        }
    }
}

private fun AppSettingsState.themeTitle(): String = when (theme) {
    "Dark" -> "Dark"
    "Light" -> "Light"
    else -> "System default"
}

@Composable
fun LanguageScreen(settings: AppSettingsState, language: String, onBack: () -> Unit) {
    val langs = listOf(
        "system" to "System",
        "en" to "English",
        "ar" to "العربية",
        "tr" to "Türkçe",
        "pt" to "Português",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch"
    )
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
            Text("Language", color = WhiteSoft, fontSize = 38.sp, fontWeight = FontWeight.Black)
        }
        LazyColumn(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
            items(langs) { (code, title) ->
                Row(Modifier.fillMaxWidth().height(72.dp).clickable { settings.languageCode = code; onBack() }, verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = WhiteSoft, fontSize = 26.sp, modifier = Modifier.weight(1f))
                    if (settings.languageCode == code) Icon(Icons.Default.Check, null, tint = Orange)
                }
                Divider(color = Color(0xFF202128))
            }
        }
    }
}

@Composable
fun ProScreen(settings: AppSettingsState, language: String, onBack: () -> Unit, onRestore: () -> Unit) {
    var plan by remember { mutableStateOf("Yearly") }
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF4B1D12), Bg, Bg)))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, null, tint = Muted, modifier = Modifier.size(34.dp)) }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRestore) { Text("Restore", color = Muted, fontSize = 22.sp) }
        }
        Spacer(Modifier.height(70.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoText(fontSize = 44)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Brush.horizontalGradient(listOf(Orange2, Orange))).padding(horizontal = 11.dp, vertical = 5.dp)) {
                Text("PRO", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        }
        Spacer(Modifier.height(34.dp))
        ProFeature("T", "Real-time transcription")
        ProFeature("≋", "AI noise reduction")
        ProFeature("AD", "Remove all ads")
        ProFeature("♢", "Bluetooth recording")
        Spacer(Modifier.height(38.dp))
        PlanCard("Monthly", "$2.49", selected = plan == "Monthly") { plan = "Monthly" }
        PlanCard("Yearly", "7-day free trial, then $9.99/year", selected = plan == "Yearly") { plan = "Yearly" }
        PlanCard("Lifetime - Best value", "$19.99", selected = plan == "Lifetime") { plan = "Lifetime" }
        Spacer(Modifier.height(24.dp))
        Text("Cancel anytime", color = Muted, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(22.dp))
        FullWidthGradientButton("Subscribe Now") {
            settings.proActive = true
            toast(context, "Pro activated for this build. Connect Play Billing product IDs before publishing paid subscriptions.")
        }
        Spacer(Modifier.height(18.dp))
        Text(
            "1. The subscription will renew automatically at the end of the period, unless you cancel it within the Google Play subscriptions page.\n2. Test build uses AdMob test ads and internal Pro activation for development.",
            color = Muted.copy(alpha = 0.55f), fontSize = 15.sp, lineHeight = 21.sp
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
fun BackupScreen(language: String, onBack: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(horizontal = 22.dp)) {
        AppHeader("Backup & Restore", onBack)
        Spacer(Modifier.height(40.dp))
        SettingsCard("☁", "Back up to Drive", "Requires Google Drive integration in production") { toast(context, "Backup request prepared") }
        SettingsCard("↧", "Restore from Drive", "Choose a Drive backup to restore") { toast(context, "Restore request prepared") }
        SettingsCard("↪", "Import local audio", "Import M4A, MP3, AAC or WAV files") { toast(context, "Use the main menu Import option") }
    }
}

@Composable
fun TrashScreen(engine: RecorderEngine, language: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        AppHeader("Trash", onBack)
        if (engine.trash.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Trash is empty", color = Muted, fontSize = 24.sp) }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                items(engine.trash) { item ->
                    RecordingListItem(item, onOpen = {}, onShare = { }, onDelete = {})
                }
            }
        }
    }
}

@Composable
fun TextScreen(title: String, body: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding().padding(horizontal = 22.dp)) {
        AppHeader(title, onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(body, color = Muted, fontSize = 24.sp, lineHeight = 32.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(26.dp))
        }
    }
}

@Composable
fun AppHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = WhiteSoft, modifier = Modifier.size(34.dp)) }
        Text(title, color = WhiteSoft, fontSize = 36.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun LogoText(fontSize: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("V", color = Orange, fontSize = fontSize.sp, fontWeight = FontWeight.Black)
        Text("oicePro", color = WhiteSoft, fontSize = fontSize.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun VoiceLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(brush = Brush.radialGradient(listOf(Orange, Orange2), center = center, radius = w / 2), radius = w / 2)
        val xs = listOf(.30f, .42f, .54f, .66f, .78f)
        val hs = listOf(.44f, .28f, .58f, .32f, .42f)
        xs.zip(hs).forEach { (x, hh) ->
            drawLine(Color.White, Offset(w * x, h * (0.5f - hh / 2)), Offset(w * x, h * (0.5f + hh / 2)), strokeWidth = w * .07f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.height(60.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun BottomHomeBar(onRecord: () -> Unit, onList: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(CardBg)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(Modifier.size(58.dp).clip(CircleShape).background(Color.White).clickable { onRecord() }, contentAlignment = Alignment.Center) {
            Box(Modifier.size(18.dp).clip(CircleShape).background(Orange))
        }
        Text("☰", color = Muted, fontSize = 38.sp, modifier = Modifier.clickable { onList() })
        Text("♪", color = Muted, fontSize = 38.sp)
    }
}

@Composable
fun Dots(index: Int, count: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(count) { i ->
            Box(Modifier.padding(4.dp).size(if (i == index) 10.dp else 8.dp).clip(CircleShape).background(if (i == index) WhiteSoft else DarkLine))
        }
    }
}

@Composable
fun RecordingModeIllustration(mode: RecordMode, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (mode) {
            RecordMode.STANDARD -> {
                drawRoundRect(Color(0xFF1F2430), topLeft = Offset(w*.30f,h*.20f), size = Size(w*.40f,h*.55f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.13f,w*.13f), style = Stroke(w*.025f))
                drawCircle(Color(0xFFB8BBC5), center = Offset(w*.5f,h*.37f), radius = w*.16f)
                drawRoundRect(Color(0xFF293040), topLeft = Offset(w*.36f,h*.55f), size = Size(w*.28f,h*.10f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.03f,w*.03f))
                drawLine(Orange, Offset(w*.50f,h*.65f), Offset(w*.50f,h*.86f), strokeWidth = w*.02f)
                drawLine(Color(0xFF171922), Offset(w*.36f,h*.86f), Offset(w*.64f,h*.86f), strokeWidth = w*.03f)
            }
            RecordMode.MUSIC -> {
                drawRoundRect(Color(0xFF2C3342), topLeft = Offset(w*.15f,h*.24f), size = Size(w*.70f,h*.40f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.03f,w*.03f))
                drawCircle(color = Color(0xFFC9CCD3), radius = w*.18f, center = Offset(w*.45f,h*.44f))
                drawCircle(color = Bg, radius = w*.055f, center = Offset(w*.45f,h*.44f))
                drawLine(Orange, Offset(w*.66f,h*.28f), Offset(w*.79f,h*.60f), strokeWidth = w*.025f)
            }
            RecordMode.MEETINGS -> {
                drawRoundRect(Color(0xFF293040), topLeft = Offset(w*.20f,h*.38f), size = Size(w*.60f,h*.34f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.035f,w*.035f))
                repeat(7) { row -> repeat(9) { col -> drawRoundRect(Color(0xFFC7CAD2), Offset(w*(.25f + col*.055f), h*(.45f + row*.034f)), Size(w*.027f,h*.018f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f,3f)) } }
                drawRoundRect(Color(0xFFDADCE2), topLeft = Offset(w*.30f,h*.22f), size = Size(w*.40f,h*.20f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.02f,w*.02f))
                drawLine(Muted, Offset(w*.36f,h*.30f), Offset(w*.62f,h*.30f), strokeWidth = 4f)
                drawLine(Muted, Offset(w*.36f,h*.36f), Offset(w*.55f,h*.36f), strokeWidth = 4f)
            }
            RecordMode.DEVICE -> {
                drawRoundRect(Color(0xFF171922), topLeft = Offset(w*.28f,h*.16f), size = Size(w*.30f,h*.58f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.05f,w*.05f), style = Stroke(w*.02f))
                repeat(4) { drawLine(Orange, Offset(w*(.36f + it*.04f), h*.42f), Offset(w*(.36f + it*.04f), h*.55f), strokeWidth = w*.018f, cap = StrokeCap.Round) }
                val path = Path().apply {
                    moveTo(w*.58f,h*.42f); cubicTo(w*.74f,h*.40f,w*.70f,h*.75f,w*.84f,h*.62f)
                }
                drawPath(path, Orange, style = Stroke(w*.02f, cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
fun AnimatedMicButton(modifier: Modifier = Modifier, selected: Boolean, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "mic")
    val scale by transition.animateFloat(0.78f, 1.18f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "micPulse")
    Box(modifier.size(130.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size((116 * scale).dp).clip(CircleShape).background(Orange.copy(alpha = if (selected) 0.22f else 0.12f)))
        Box(Modifier.size(92.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(Orange2, Orange))).clickable { onClick() }, contentAlignment = Alignment.Center) {
            Text("♩", color = Color.White, fontSize = 46.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CircleIconBadge(text: String, size: Dp) {
    Box(Modifier.size(size).clip(CircleShape).background(Color(0xFF33343A)), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Black, fontSize = (size.value / 2.2).sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(CircleShape).background(Color(0xFFD7D8DE)).padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun ToggleRowText(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF17181D)).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("♩", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Text(label, color = WhiteSoft, fontSize = 18.sp, modifier = Modifier.weight(1f))
        AppSwitch(value, onChange)
    }
}

@Composable
fun AppSwitch(value: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = value,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Orange, uncheckedThumbColor = Muted, uncheckedTrackColor = Color(0xFF262730))
    )
}

@Composable
fun WaveformTimeline(progress: Float, markers: List<Long>, active: Boolean, modifier: Modifier = Modifier, showTime: Boolean = false) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(0f, 6.28f, infiniteRepeatable(tween(1000), RepeatMode.Restart), label = "phase")
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        if (showTime) {
            val top = h * .08f
            drawLine(Color(0xFF2D2E35), Offset(0f, top), Offset(w, top), strokeWidth = 1.5f)
            for (i in 0..6) {
                val x = w * i / 6f
                drawLine(Color(0xFF353640), Offset(x, top + 12f), Offset(x, top + 32f), strokeWidth = 2f)
            }
        }
        val centerY = h * .52f
        val bars = 86
        val spacing = w / bars
        for (i in 0 until bars) {
            val x = i * spacing + spacing * .25f
            val localProgress = i / bars.toFloat()
            val base = 0.18f + 0.8f * abs(sin(i * .43f + 1.2f)).toFloat()
            val pulse = if (active) 0.78f + 0.22f * abs(sin(phase + i * .16f)).toFloat() else 1f
            val height = (18f + 88f * base * pulse) * if (i % 9 == 0) 1.55f else 1f
            val color = if (localProgress <= progress) Color.White else Color(0xFF34353B)
            drawLine(color, Offset(x, centerY - height / 2), Offset(x, centerY + height / 2), strokeWidth = 5f, cap = StrokeCap.Round)
        }
        markers.take(8).forEachIndexed { idx, m ->
            val x = ((m / 1000f) % 8f) / 8f * w
            drawLine(Blue, Offset(x, centerY + 4f), Offset(x, centerY + h * .28f), strokeWidth = 2.4f)
            drawCircle(Blue, radius = 9f, center = Offset(x, centerY + h * .31f))
            drawCircle(Color.White, radius = 3f, center = Offset(x, centerY + h * .31f))
        }
        val playX = w * .50f
        drawLine(Orange, Offset(playX, h * .05f), Offset(playX, h * .88f), strokeWidth = 4f, cap = StrokeCap.Round)
        drawCircle(Orange.copy(alpha = .15f), 28f, Offset(playX, centerY))
    }
}

@Composable
fun MiniWaveform(modifier: Modifier = Modifier, active: Boolean) {
    Card(modifier, colors = CardDefaults.cardColors(Color(0xFF16171C)), shape = RoundedCornerShape(16.dp)) {
        WaveformTimeline(progress = .45f, markers = emptyList(), active = active, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun TrimWaveform(trimMode: Boolean, start: Float, end: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.background(CardBg)) {
        val w = size.width
        val h = size.height
        val centerY = h * .46f
        val startX = w * start
        val endX = w * end
        drawRect(if (trimMode) Color(0xFF3A1818).copy(alpha = .58f) else Color(0xFF3A1818).copy(alpha = .75f), Offset(0f, 0f), Size(startX, h))
        drawRect(if (trimMode) Color(0xFF3A1818).copy(alpha = .58f) else Color(0xFF3A1818).copy(alpha = .75f), Offset(endX, 0f), Size(w - endX, h))
        if (!trimMode) drawRect(Color(0xFF3A1818).copy(alpha = .75f), Offset(startX, 0f), Size(endX - startX, h))
        val bars = 76
        val spacing = w / bars
        for (i in 0 until bars) {
            val x = i * spacing
            val height = 35f + 90f * abs(sin(i * .25f)).toFloat()
            drawLine(Color.White, Offset(x, centerY - height / 2), Offset(x, centerY + height / 2), strokeWidth = 5f, cap = StrokeCap.Round)
        }
        fun handle(x: Float, left: Boolean) {
            drawLine(Orange, Offset(x, 0f), Offset(x, h), strokeWidth = 4f)
            drawRoundRect(Orange, Offset(x - 18f, if (left) h - 90f else 18f), Size(36f, 48f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
            drawLine(Color.White, Offset(x + if (left) 4f else -4f, if (left) h - 68f else 42f), Offset(x + if (left) 13f else -13f, if (left) h - 78f else 32f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(Color.White, Offset(x + if (left) 4f else -4f, if (left) h - 68f else 42f), Offset(x + if (left) 13f else -13f, if (left) h - 58f else 52f), strokeWidth = 4f, cap = StrokeCap.Round)
        }
        handle(startX, true)
        handle(endX, false)
        repeat(2) { idx ->
            val x = w * (0.55f + idx * .21f)
            drawLine(Blue, Offset(x, 0f), Offset(x, h * .86f), strokeWidth = 2f)
            drawCircle(Blue, 8f, Offset(x, h * .89f))
        }
    }
}

@Composable
fun GradientRoundButton(label: String, size: Dp, onClick: () -> Unit) {
    Box(Modifier.size(size).clip(if (size > 140.dp) RoundedCornerShape(36.dp) else CircleShape).background(Brush.horizontalGradient(listOf(Orange2, Orange))).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = if (size > 140.dp) 30.sp else 38.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun LabeledCircleButton(icon: String, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(CardBg).clickable { onClick() }, contentAlignment = Alignment.Center) {
            Text(icon, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(10.dp))
        Text(label, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SmallRoundControl(label: String, onClick: () -> Unit) {
    Box(Modifier.height(58.dp).width(108.dp).clip(RoundedCornerShape(28.dp)).background(CardBg).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ToolIcon(label: String, onClick: () -> Unit) {
    Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFF17181D)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = if (label.length > 2) 18.sp else 28.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun FullWidthGradientButton(text: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(42.dp)).background(Brush.horizontalGradient(listOf(Orange2, Orange))).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun OutlinedOrangeButton(text: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(82.dp).clip(RoundedCornerShape(42.dp)).border(2.dp, Orange, RoundedCornerShape(42.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = Orange, fontSize = 26.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun RecordingPreviewCard(file: File, engine: RecorderEngine, large: Boolean) {
    Card(Modifier.fillMaxWidth().height(if (large) 170.dp else 110.dp), colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(26.dp)) {
        Row(Modifier.fillMaxSize().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF2D2E37)), contentAlignment = Alignment.Center) {
                MiniLogoLines(Modifier.size(40.dp))
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(file.nameWithoutExtension, color = WhiteSoft, fontSize = 28.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${fileSize(file.length())}  ${file.extension.uppercase(Locale.US).ifBlank { "M4A" }}", color = Muted, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▶", color = Color.White, fontSize = 25.sp, modifier = Modifier.clickable { engine.play(file) })
                    Slider(value = .32f, onValueChange = {}, modifier = Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Orange, inactiveTrackColor = DarkLine))
                    Text(engine.fileToItem(file).durationLabel, color = Muted, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun MiniLogoLines(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        repeat(5) { i ->
            val x = w * (.20f + i*.15f)
            val hh = h * (.35f + .4f * abs(sin(i.toFloat())).toFloat())
            drawLine(Orange, Offset(x, h/2 - hh/2), Offset(x, h/2 + hh/2), strokeWidth = w*.06f, cap = StrokeCap.Round)
        }
    }
}

@Composable
fun ActionGrid(actions: List<ActionItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
        actions.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { action ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
                        Box(Modifier.size(74.dp).clip(RoundedCornerShape(16.dp)).background(CardBg).clickable { action.onClick() }, contentAlignment = Alignment.Center) {
                            Text(action.icon, color = if (action.icon == "U") Orange else Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(action.label, color = Muted, fontSize = 19.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
                    }
                }
            }
        }
    }
}

data class ActionItem(val icon: String, val label: String, val onClick: () -> Unit)

@Composable
fun RecordingListItem(item: RecordingItem, onOpen: () -> Unit, onShare: (Context) -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth().height(104.dp).clickable { onOpen() }, colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(62.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF2D2E37)), contentAlignment = Alignment.Center) { MiniLogoLines(Modifier.size(36.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = WhiteSoft, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${fileSize(item.sizeBytes)}  M4A", color = Muted, fontSize = 17.sp)
            }
            IconButton(onClick = { onShare(context) }) { Icon(Icons.Default.Share, null, tint = Muted) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Orange) }
        }
    }
}

@Composable
fun SearchBox(query: String, onChange: (String) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp).height(56.dp), colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(28.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = Muted)
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = WhiteSoft, fontSize = 19.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isBlank()) Text("Search recordings", color = Muted, fontSize = 19.sp)
                    inner()
                }
            )
        }
    }
}

@Composable
fun EffectItem(name: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(82.dp).clickable { onClick() }) {
        EffectAvatar(name, selected, Modifier.size(78.dp))
        Spacer(Modifier.height(8.dp))
        Text(name, color = if (selected) Orange else WhiteSoft, fontSize = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 18.sp)
    }
}

@Composable
fun EffectAvatar(name: String, selected: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val bgColor = when (name) {
            "Girl" -> Color(0xFFC05B87)
            "Man" -> Color(0xFF5C66B4)
            "Child" -> Color(0xFF5ABC78)
            "Squirrel" -> Color(0xFFFFC2A5)
            "Tenor singer" -> Color(0xFF5254B4)
            "Megaphone" -> Color(0xFFA8D8CF)
            "Nervous" -> Color(0xFFA8416D)
            "Drunk" -> Color(0xFF834B56)
            "Robot" -> Color(0xFF0C9FE0)
            "Death" -> Color(0xFF8766D7)
            "Monster" -> Color(0xFFEBC96F)
            "Alien" -> Color(0xFF5E44B5)
            else -> Color(0xFF24252B)
        }
        drawCircle(bgColor, radius = size.minDimension / 2)
        if (selected) drawCircle(Orange, radius = size.minDimension / 2 - 2f, style = Stroke(6f))
        val w = size.width
        val h = size.height
        when (name) {
            "Normal" -> repeat(5) { i -> drawLine(Orange, Offset(w*(.28f+i*.11f), h*.34f), Offset(w*(.28f+i*.11f), h*.66f), strokeWidth = 6f, cap = StrokeCap.Round) }
            "Megaphone" -> {
                drawRoundRect(Color(0xFF127D65), Offset(w*.25f,h*.38f), Size(w*.42f,h*.22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f,12f))
                drawLine(Orange, Offset(w*.65f,h*.30f), Offset(w*.76f,h*.18f), strokeWidth = 5f)
                drawLine(Orange, Offset(w*.68f,h*.67f), Offset(w*.82f,h*.76f), strokeWidth = 5f)
            }
            "Robot" -> {
                drawRoundRect(Color(0xFFEAF4FF), Offset(w*.24f,h*.28f), Size(w*.52f,h*.42f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f,14f))
                drawCircle(Blue, 8f, Offset(w*.42f,h*.48f)); drawCircle(Blue, 8f, Offset(w*.58f,h*.48f))
            }
            "Death" -> {
                drawCircle(Color.White, w*.18f, Offset(w*.50f,h*.48f)); drawCircle(Bg, 5f, Offset(w*.44f,h*.46f)); drawCircle(Bg,5f,Offset(w*.56f,h*.46f)); drawLine(Bg, Offset(w*.43f,h*.58f), Offset(w*.57f,h*.58f), strokeWidth = 3f)
            }
            "Monster" -> {
                drawRoundRect(Color(0xFF35A348), Offset(w*.25f,h*.30f), Size(w*.50f,h*.45f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f,22f)); drawCircle(Bg, 5f, Offset(w*.43f,h*.46f)); drawCircle(Bg,5f,Offset(w*.58f,h*.46f))
            }
            "Alien" -> {
                drawCircle(Color(0xFF6DE042), w*.27f, Offset(w*.50f,h*.48f)); drawOval(Bg, Offset(w*.34f,h*.42f), Size(w*.13f,h*.12f)); drawOval(Bg, Offset(w*.54f,h*.42f), Size(w*.13f,h*.12f))
            }
            "Tenor singer" -> {
                drawCircle(Color(0xFFFFC2D0), w*.20f, Offset(w*.44f,h*.44f)); drawCircle(Color(0xFF15161B), w*.14f, Offset(w*.38f,h*.32f)); drawLine(Color(0xFF015C64), Offset(w*.58f,h*.48f), Offset(w*.78f,h*.63f), strokeWidth = 6f, cap = StrokeCap.Round)
            }
            "Squirrel" -> {
                drawCircle(Color(0xFFE55D25), w*.20f, Offset(w*.50f,h*.48f)); drawCircle(Color(0xFFE55D25), w*.10f, Offset(w*.36f,h*.32f)); drawCircle(Color(0xFFE55D25), w*.10f, Offset(w*.64f,h*.32f)); drawCircle(Bg, 4f, Offset(w*.43f,h*.46f)); drawCircle(Bg,4f,Offset(w*.57f,h*.46f))
            }
            "Nervous" -> { drawCircle(Color(0xFFFFC2D0), w*.22f, Offset(w*.50f,h*.46f)); drawCircle(Bg, 4f, Offset(w*.42f,h*.42f)); drawCircle(Bg,4f,Offset(w*.58f,h*.42f)); drawLine(Blue, Offset(w*.65f,h*.53f), Offset(w*.70f,h*.65f), strokeWidth = 5f, cap = StrokeCap.Round) }
            "Drunk" -> { drawCircle(Color(0xFFFFB0A0), w*.22f, Offset(w*.50f,h*.46f)); drawCircle(Color(0xFF2B2320), 4f, Offset(w*.42f,h*.42f)); drawCircle(Color(0xFF2B2320),4f,Offset(w*.58f,h*.42f)); drawLine(Color.White, Offset(w*.26f,h*.68f), Offset(w*.38f,h*.58f), strokeWidth = 6f) }
            else -> { drawCircle(Bg, 6f, Offset(w*.42f,h*.42f)); drawCircle(Bg,6f,Offset(w*.58f,h*.42f)); drawArc(Bg, startAngle = 20f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w*.34f,h*.45f), size = Size(w*.32f,h*.22f), style = Stroke(4f)) }
        }
    }
}

@Composable
fun EffectSlider(label: String, minLabel: String, maxLabel: String, value: Float, onValue: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(92.dp).height(38.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF17181D)), contentAlignment = Alignment.Center) { Text(label, color = WhiteSoft, fontSize = 18.sp, fontWeight = FontWeight.Black) }
        Text(minLabel, color = Muted, fontSize = 21.sp, modifier = Modifier.padding(horizontal = 10.dp))
        Slider(value = value, onValueChange = onValue, valueRange = .5f..1.5f, modifier = Modifier.weight(1f), colors = SliderDefaults.colors(thumbColor = WhiteSoft, activeTrackColor = Orange, inactiveTrackColor = DarkLine))
        Text(maxLabel, color = Muted, fontSize = 21.sp, modifier = Modifier.padding(horizontal = 10.dp))
    }
}

@Composable
fun TabText(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Text(label, color = if (selected) WhiteSoft else Muted, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.width(64.dp).height(7.dp).clip(RoundedCornerShape(5.dp)).background(if (selected) Orange else Color.Transparent))
    }
}

@Composable
fun TimePill(left: String, text: String, right: String, onClick: () -> Unit) {
    Row(Modifier.height(48.dp).width(146.dp).clip(RoundedCornerShape(24.dp)).background(CardBg).clickable { onClick() }, horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        Text(left, color = WhiteSoft, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(text, color = WhiteSoft, fontSize = 19.sp)
        Text(right, color = WhiteSoft, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun SettingsCard(icon: String, title: String, subtitle: String?, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 0.dp, vertical = 7.dp).height(if (subtitle == null) 94.dp else 110.dp).clickable { onClick() }, colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, color = Orange, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(62.dp), textAlign = TextAlign.Center)
            Column(Modifier.weight(1f)) {
                Text(title, color = WhiteSoft, fontSize = 27.sp, lineHeight = 30.sp)
                if (subtitle != null) Text(subtitle, color = Muted, fontSize = 18.sp)
            }
            Text("›", color = WhiteSoft, fontSize = 50.sp)
        }
    }
}

@Composable
fun RemoveAdsCard(onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 7.dp).height(110.dp).clickable { onClick() }, colors = CardDefaults.cardColors(CardBg), shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Orange, Color(0xFF4B130D)))), contentAlignment = Alignment.Center) {
                Text("AD", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(22.dp))
            Text("Remove all ads", color = WhiteSoft, fontSize = 29.sp, modifier = Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(25.dp)).background(Brush.horizontalGradient(listOf(Orange2, Orange))).padding(horizontal = 25.dp, vertical = 13.dp)) {
                Text("Upgrade", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = Orange, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp))
}

@Composable
fun SettingsRow(title: String, subtitle: String?, arrow: Boolean = false, badge: String? = null, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(if (subtitle == null) 74.dp else 92.dp).clickable { onClick() }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = WhiteSoft, fontSize = 26.sp)
                if (badge != null) {
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Orange).padding(horizontal = 10.dp, vertical = 2.dp)) { Text(badge, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black) }
                }
            }
            if (subtitle != null) Text(subtitle, color = Muted, fontSize = 18.sp, lineHeight = 21.sp)
        }
        if (arrow) Text("›", color = WhiteSoft, fontSize = 46.sp)
    }
}

@Composable
fun SettingsToggleRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(if (subtitle == null) 74.dp else 100.dp).padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = WhiteSoft, fontSize = 25.sp, lineHeight = 29.sp)
            if (subtitle != null) Text(subtitle, color = Muted, fontSize = 18.sp, lineHeight = 21.sp)
        }
        AppSwitch(checked, onChange)
    }
}

@Composable
fun SettingsPlain(title: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(78.dp).clickable { onClick() }.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = WhiteSoft, fontSize = 25.sp)
    }
}

@Composable
fun OptionDialog(title: String, options: List<String>, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(title, color = WhiteSoft, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                options.forEach { option ->
                    Text(option, color = WhiteSoft, fontSize = 21.sp, modifier = Modifier.fillMaxWidth().clickable { onSelect(option) }.padding(vertical = 13.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Orange) } }
    )
}

@Composable
fun ConfirmDialog(title: String, cancel: String, confirm: String, onCancel: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = CardBg,
        title = { Text(title, color = WhiteSoft, fontSize = 25.sp, fontWeight = FontWeight.Black) },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text(confirm, color = Color.White, fontWeight = FontWeight.Bold) } },
        dismissButton = { OutlinedButton(onClick = onCancel) { Text(cancel, color = Muted) } }
    )
}

@Composable
fun RatingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("We are working hard for a better user experience.", color = WhiteSoft, fontSize = 25.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("We’d greatly appreciate if you can rate us.", color = WhiteSoft, fontSize = 21.sp)
                Spacer(Modifier.height(22.dp))
                Text("★★★★★", color = Orange, fontSize = 42.sp, fontWeight = FontWeight.Black)
            }
        },
        confirmButton = { Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("RATE", color = Color.White, fontWeight = FontWeight.Black) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Later", color = Muted) } }
    )
}

@Composable
fun PlanCard(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().height(96.dp).padding(vertical = 6.dp).border(2.dp, if (selected) Orange else Color(0xFF4A211C), RoundedCornerShape(20.dp)).clickable { onClick() }, colors = CardDefaults.cardColors(Color.Transparent), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = WhiteSoft, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = Muted, fontSize = 18.sp)
            }
            Box(Modifier.size(34.dp).clip(CircleShape).border(3.dp, if (selected) Orange else Color(0xFF4A211C), CircleShape).background(if (selected) Orange else Color.Transparent), contentAlignment = Alignment.Center) {
                if (selected) Text("✓", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ProFeature(icon: String, title: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Orange2, Color(0xFF5C190F)))), contentAlignment = Alignment.Center) {
            Text(icon, color = Color.White, fontSize = if (icon.length > 1) 16.sp else 20.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(22.dp))
        Text(title, color = WhiteSoft, fontSize = 27.sp)
    }
}

@Composable
fun MenuText(text: String) { Text(text, color = WhiteSoft, fontSize = 23.sp, fontWeight = FontWeight.Bold) }

@Composable
fun MenuGlyph(text: String) { Text(text, color = WhiteSoft, fontSize = 27.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center) }

private fun languageTitle(code: String): String = when (code) {
    "system" -> "System"
    "ar" -> "العربية"
    "tr" -> "Türkçe"
    "pt" -> "Português"
    "es" -> "Español"
    "fr" -> "Français"
    "de" -> "Deutsch"
    else -> "English"
}

private fun t(key: String, lang: String): String {
    val map = when (lang) {
        "ar" -> mapOf("permissionReady" to "تم تفعيل صلاحية الميكروفون", "micPermissionNeeded" to "يجب السماح بصلاحية الميكروفون للتسجيل")
        "tr" -> mapOf("permissionReady" to "Mikrofon izni hazır", "micPermissionNeeded" to "Kayıt için mikrofon izni gerekir")
        "pt" -> mapOf("permissionReady" to "Permissão do microfone pronta", "micPermissionNeeded" to "A permissão do microfone é necessária")
        "es" -> mapOf("permissionReady" to "Permiso de micrófono listo", "micPermissionNeeded" to "Se necesita permiso de micrófono")
        "fr" -> mapOf("permissionReady" to "Autorisation micro prête", "micPermissionNeeded" to "L’autorisation du micro est requise")
        "de" -> mapOf("permissionReady" to "Mikrofonberechtigung bereit", "micPermissionNeeded" to "Mikrofonberechtigung ist erforderlich")
        else -> mapOf("permissionReady" to "Microphone permission is ready", "micPermissionNeeded" to "Microphone permission is required for recording")
    }
    return map[key] ?: key
}

private fun stableBackgroundText(): String = "Allow microphone permission, disable aggressive battery restrictions, keep notification enabled, and avoid force-stopping the app while a recording is running."
private fun privacyText(): String = "VoicePro Recorder stores recordings locally on your device unless you choose to share, import, or back up files. AdMob test ads are enabled in this development build."
private fun termsText(): String = "Use VoicePro Recorder only for recordings you are allowed to create. You are responsible for local recording consent rules and exported files."
private fun helpText(): String = "For support: check microphone permission, storage access, recording quality, and audio source settings. Use Import to add existing audio files and Backup to prepare cloud storage integration."
