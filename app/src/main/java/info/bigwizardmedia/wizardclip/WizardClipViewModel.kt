package info.bigwizardmedia.wizardclip

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ClipFilter { ALL, PINNED, SNIPPETS }

class WizardClipViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WizardClipDatabase.create(application)
    private val clipDao = database.clipDao()
    private val snippetDao = database.snippetDao()
    val settings = SettingsStore(application)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(ClipFilter.ALL)
    val filter: StateFlow<ClipFilter> = _filter.asStateFlow()

    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> = _now.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages

    private val allClips = clipDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val clips: StateFlow<List<ClipEntity>> = combine(allClips, _query, _filter) { clips, q, f ->
        val searched = if (q.isBlank()) clips else clips.filter {
            it.text.contains(q, ignoreCase = true)
        }
        when (f) {
            ClipFilter.ALL -> searched
            ClipFilter.PINNED -> searched.filter { it.pinned }
            // Snippets are shown from the snippets flow; the history list
            // hides itself when the Snippets filter is active.
            ClipFilter.SNIPPETS -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val snippets: StateFlow<List<SnippetEntity>> = snippetDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var lastSharedText: String? = null
    private var lastSharedAt: Long = 0

    init {
        seedExamplesIfFirstRun()
        viewModelScope.launch {
            while (true) {
                val current = System.currentTimeMillis()
                _now.value = current
                clipDao.deleteExpired(current)
                delay(1_000)
            }
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setFilter(value: ClipFilter) {
        _filter.value = value
    }

    fun setCapturePaused(paused: Boolean) {
        settings.setCapturePaused(paused)
        showMessage(if (paused) "Capture paused" else "Capture resumed")
    }

    fun showMessage(text: String) {
        _messages.tryEmit(text)
    }

    /** Adds a manually typed/pasted clip. Returns false when nothing was added. */
    fun addClip(text: String, source: String = "Manual") {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            showMessage("Nothing to save — the clip is empty")
            return
        }
        if (settings.isCapturePaused()) {
            showMessage("Capture is paused — resume it in Settings to save clips")
            return
        }
        viewModelScope.launch {
            insertClip(trimmed, source)
            showMessage("Clip saved")
        }
    }

    /**
     * Handles text shared into the app via ACTION_SEND. The caller is the
     * foreground activity, so this is foreground-only capture — no
     * background clipboard reading is attempted anywhere in the app.
     */
    fun handleSharedText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val nowMs = System.currentTimeMillis()
        if (trimmed == lastSharedText && nowMs - lastSharedAt < 5_000) return
        lastSharedText = trimmed
        lastSharedAt = nowMs
        if (settings.isCapturePaused()) {
            showMessage("Capture is paused — shared text was not saved")
            return
        }
        viewModelScope.launch {
            insertClip(trimmed, "Shared")
            showMessage("Shared text saved as a clip")
        }
    }

    private suspend fun insertClip(text: String, source: String) {
        val nowMs = System.currentTimeMillis()
        val kind = SensitiveClipDetector.detect(text)
        clipDao.insert(
            ClipEntity(
                text = text,
                createdAt = nowMs,
                isSensitive = kind != null,
                sensitiveKind = kind?.label,
                expiresAt = if (kind != null) nowMs + SensitiveClipDetector.SENSITIVE_TTL_MS else null,
                source = source,
            )
        )
    }

    fun togglePin(clip: ClipEntity) {
        viewModelScope.launch { clipDao.setPinned(clip.id, !clip.pinned) }
    }

    fun deleteClip(clip: ClipEntity) {
        viewModelScope.launch {
            clipDao.deleteById(clip.id)
            showMessage("Clip deleted")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            clipDao.deleteAll()
            showMessage("History cleared — snippets kept")
        }
    }

    fun addSnippet(title: String, text: String) {
        val cleanTitle = title.trim()
        val cleanText = text.trim()
        if (cleanTitle.isEmpty() || cleanText.isEmpty()) {
            showMessage("A snippet needs both a title and text")
            return
        }
        viewModelScope.launch {
            snippetDao.insert(
                SnippetEntity(title = cleanTitle, text = cleanText, createdAt = System.currentTimeMillis())
            )
            showMessage("Snippet saved")
        }
    }

    fun updateSnippet(snippet: SnippetEntity, title: String, text: String) {
        val cleanTitle = title.trim()
        val cleanText = text.trim()
        if (cleanTitle.isEmpty() || cleanText.isEmpty()) {
            showMessage("A snippet needs both a title and text")
            return
        }
        viewModelScope.launch {
            snippetDao.update(snippet.copy(title = cleanTitle, text = cleanText))
            showMessage("Snippet updated")
        }
    }

    fun deleteSnippet(snippet: SnippetEntity) {
        viewModelScope.launch {
            snippetDao.deleteById(snippet.id)
            showMessage("Snippet deleted")
        }
    }

    private fun seedExamplesIfFirstRun() {
        viewModelScope.launch {
            if (settings.isSeedDone()) return@launch
            if (clipDao.count() == 0 && snippetDao.count() == 0) {
                val nowMs = System.currentTimeMillis()
                clipDao.insert(
                    ClipEntity(
                        text = "Example — deletable: https://bigwizardmedia.biz — Bigwizard Media",
                        createdAt = nowMs,
                        source = "Example",
                    )
                )
                clipDao.insert(
                    ClipEntity(
                        text = "Example — deletable: Thanks for reaching out! I'll get back to you shortly. — Chase, Bigwizard Media",
                        createdAt = nowMs + 1,
                        source = "Example",
                    )
                )
                clipDao.insert(
                    ClipEntity(
                        text = "Example — deletable: Meeting notes: follow up on the clipboard sync design.",
                        createdAt = nowMs + 2,
                        source = "Example",
                    )
                )
                snippetDao.insert(
                    SnippetEntity(
                        title = "Example greeting",
                        text = "Hi! Thanks for contacting Bigwizard Media — how can I help?",
                        createdAt = nowMs,
                    )
                )
            }
            settings.setSeedDone()
        }
    }
}
