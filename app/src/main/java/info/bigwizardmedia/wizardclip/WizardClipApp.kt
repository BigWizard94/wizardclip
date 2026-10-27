package info.bigwizardmedia.wizardclip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private enum class Tab { HISTORY, SNIPPETS, DEVICES, SETTINGS }

@Composable
fun WizardClipApp(viewModel: WizardClipViewModel) {
    var tab by remember { mutableStateOf(Tab.HISTORY) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("WizardClip", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.HISTORY,
                    onClick = { tab = Tab.HISTORY },
                    icon = { Icon(Icons.Filled.ContentPaste, contentDescription = null) },
                    label = { Text("History") },
                )
                NavigationBarItem(
                    selected = tab == Tab.SNIPPETS,
                    onClick = { tab = Tab.SNIPPETS },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    label = { Text("Snippets") },
                )
                NavigationBarItem(
                    selected = tab == Tab.DEVICES,
                    onClick = { tab = Tab.DEVICES },
                    icon = { Icon(Icons.Filled.Devices, contentDescription = null) },
                    label = { Text("Devices") },
                )
                NavigationBarItem(
                    selected = tab == Tab.SETTINGS,
                    onClick = { tab = Tab.SETTINGS },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.HISTORY -> HistoryScreen(viewModel)
                Tab.SNIPPETS -> SnippetsScreen(viewModel)
                Tab.DEVICES -> DevicesScreen()
                Tab.SETTINGS -> SettingsScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryScreen(viewModel: WizardClipViewModel) {
    val context = LocalContext.current
    val clips by viewModel.clips.collectAsState()
    val snippets by viewModel.snippets.collectAsState()
    val query by viewModel.query.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val now by viewModel.now.collectAsState()
    val paused by viewModel.settings.capturePaused.collectAsState()
    var composerText by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showSaveSnippetDialog by remember { mutableStateOf(false) }
    val revealed = remember { mutableStateMapOf<Long, Boolean>() }

    val sensitiveKind = SensitiveClipDetector.detect(composerText)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "Local-only clipboard history. Nothing leaves this device in v1.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedTextField(
                value = composerText,
                onValueChange = { composerText = it },
                label = { Text("Type or paste a new clip") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5,
            )
        }
        if (sensitiveKind != null) {
            item {
                SensitiveWarningCard(
                    "Looks like a ${sensitiveKind.label.lowercase()} — " +
                        "it will be masked and auto-deleted 5 minutes after saving."
                )
            }
        }
        item {
            // FlowRow (not Row): on a narrow phone the three buttons wrap
            // onto a second line instead of squeezing "Save as snippet"
            // into a narrow vertical column.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        val pasted = readClipboardText(context)
                        if (pasted.isNullOrBlank()) {
                            viewModel.showMessage("Clipboard is empty or could not be read")
                        } else {
                            composerText = pasted
                        }
                    },
                    enabled = !paused,
                ) {
                    Icon(Icons.Filled.ContentPaste, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Paste from Clipboard")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.addClip(composerText)
                        composerText = ""
                    },
                    enabled = composerText.isNotBlank() && !paused,
                ) {
                    Text("Save clip")
                }
                OutlinedButton(
                    onClick = { showSaveSnippetDialog = true },
                    enabled = composerText.isNotBlank(),
                ) {
                    Text("Save as snippet")
                }
            }
            if (paused) {
                Text(
                    "Capture is paused. Resume it in Settings to save clips.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search clips") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter == ClipFilter.ALL,
                    onClick = { viewModel.setFilter(ClipFilter.ALL) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = filter == ClipFilter.PINNED,
                    onClick = { viewModel.setFilter(ClipFilter.PINNED) },
                    label = { Text("Pinned") },
                )
                FilterChip(
                    selected = filter == ClipFilter.SNIPPETS,
                    onClick = { viewModel.setFilter(ClipFilter.SNIPPETS) },
                    label = { Text("Snippets") },
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { showClearConfirm = true }) {
                    Text("Clear history")
                }
            }
        }
        if (filter == ClipFilter.SNIPPETS) {
            if (snippets.isEmpty()) {
                item { EmptyText("No snippets yet. Save one from a clip above, or add one in Snippets.") }
            } else {
                items(snippets, key = { it.id }) { snippet ->
                    SnippetCard(
                        snippet = snippet,
                        onCopy = {
                            copyToClipboard(context, snippet.text)
                            viewModel.showMessage("Snippet copied to clipboard")
                        },
                        onEdit = null,
                        onDelete = { viewModel.deleteSnippet(snippet) },
                    )
                }
            }
        } else {
            if (clips.isEmpty()) {
                item {
                    EmptyText(
                        if (query.isBlank()) "No clips yet. Paste or type one above."
                        else "No clips match \"$query\"."
                    )
                }
            } else {
                items(clips, key = { it.id }) { clip ->
                    ClipCard(
                        clip = clip,
                        nowMs = now,
                        revealed = revealed[clip.id] == true,
                        onToggleReveal = { revealed[clip.id] = !(revealed[clip.id] == true) },
                        onCopy = {
                            copyToClipboard(context, clip.text)
                            viewModel.showMessage("Copied to clipboard")
                        },
                        onTogglePin = { viewModel.togglePin(clip) },
                        onDelete = { viewModel.deleteClip(clip) },
                    )
                }
            }
        }
        item {
            Text(
                "Why capture is manual: since Android 10, an app can only read " +
                    "the clipboard while it is in the foreground. WizardClip does " +
                    "not read the clipboard in the background. Use Paste from " +
                    "Clipboard here, or share text to WizardClip from another app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear history?") },
            text = { Text("This deletes every clip in history. Your snippets will be kept.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirm = false
                }) { Text("Clear history") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            },
        )
    }

    if (showSaveSnippetDialog) {
        var title by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveSnippetDialog = false },
            title = { Text("Save as snippet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Snippet title") },
                        singleLine = true,
                    )
                    Text(composerText, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addSnippet(title, composerText)
                    showSaveSnippetDialog = false
                    composerText = ""
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveSnippetDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun ClipCard(
    clip: ClipEntity,
    nowMs: Long,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            if (clip.isSensitive && !revealed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        "Sensitive clip — ${clip.sensitiveKind ?: "masked"}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text("•".repeat(24), style = MaterialTheme.typography.bodyLarge)
            } else {
                SelectionContainer {
                    Text(clip.text, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${clip.source} · ${DateUtils.getRelativeTimeSpanString(clip.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (clip.expiresAt != null) {
                    val remaining = ((clip.expiresAt - nowMs) / 1000).coerceAtLeast(0)
                    Text(
                        " · Deletes in ${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Row {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Copy")
                }
                if (clip.isSensitive) {
                    TextButton(onClick = onToggleReveal) {
                        Icon(
                            if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(if (revealed) "Hide" else "Reveal")
                    }
                }
                IconButton(onClick = onTogglePin) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = if (clip.pinned) "Unpin" else "Pin",
                        tint = if (clip.pinned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete clip")
                }
            }
        }
    }
}

@Composable
private fun SnippetCard(
    snippet: SnippetEntity,
    onCopy: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(snippet.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            SelectionContainer {
                Text(snippet.text, style = MaterialTheme.typography.bodyLarge)
            }
            Row {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Copy")
                }
                if (onEdit != null) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Edit")
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete snippet")
                }
            }
        }
    }
}

@Composable
private fun SnippetsScreen(viewModel: WizardClipViewModel) {
    val context = LocalContext.current
    val snippets by viewModel.snippets.collectAsState()
    var editing by remember { mutableStateOf<SnippetEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add snippet")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Reusable templates. Tap Copy to put one on the clipboard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (snippets.isEmpty()) {
                item { EmptyText("No snippets yet. Tap + to add one.") }
            } else {
                items(snippets, key = { it.id }) { snippet ->
                    SnippetCard(
                        snippet = snippet,
                        onCopy = {
                            copyToClipboard(context, snippet.text)
                            viewModel.showMessage("Snippet copied to clipboard")
                        },
                        onEdit = { editing = snippet },
                        onDelete = { viewModel.deleteSnippet(snippet) },
                    )
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }

    if (showAddDialog) {
        SnippetEditorDialog(
            title = "Add snippet",
            onDismiss = { showAddDialog = false },
            onSave = { title, text ->
                viewModel.addSnippet(title, text)
                showAddDialog = false
            },
        )
    }
    editing?.let { snippet ->
        SnippetEditorDialog(
            title = "Edit snippet",
            initialTitle = snippet.title,
            initialText = snippet.text,
            onDismiss = { editing = null },
            onSave = { title, text ->
                viewModel.updateSnippet(snippet, title, text)
                editing = null
            },
        )
    }
}

@Composable
private fun SnippetEditorDialog(
    title: String,
    initialTitle: String = "",
    initialText: String = "",
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var snippetTitle by remember { mutableStateOf(initialTitle) }
    var snippetText by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = snippetTitle,
                    onValueChange = { snippetTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = snippetText,
                    onValueChange = { snippetText = it },
                    label = { Text("Text") },
                    minLines = 3,
                    maxLines = 6,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(snippetTitle, snippetText) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun DevicesScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Sync is coming in a later update.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "This version is local-only on purpose: your clips are stored " +
                "in a database on this device and nothing is sent anywhere. " +
                "There is no account, no server, and no simulated sync in this app.",
            style = MaterialTheme.typography.bodyMedium,
        )
        DeviceOptionCard(
            title = "LAN / peer-to-peer sync",
            body = "Phone and PC on the same Wi-Fi, syncing directly with " +
                "end-to-end encryption. Free forever. Coming in a later update.",
        )
        DeviceOptionCard(
            title = "Self-hosted relay",
            body = "Run a tiny relay server of your own and sync away from home. " +
                "Free forever. Coming in a later update.",
        )
        DeviceOptionCard(
            title = "Managed relay (optional, paid)",
            body = "A hosted relay for people who do not want to self-host. " +
                "Privacy and encryption will be identical to the free options — " +
                "you would only ever pay for hosting, never for privacy. " +
                "Coming in a later update.",
        )
        Text(
            "Planned design: clips are encrypted on your device before they " +
                "leave it, so a relay only ever sees ciphertext. The encryption " +
                "key never leaves your devices.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeviceOptionCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SettingsScreen(viewModel: WizardClipViewModel) {
    val paused by viewModel.settings.capturePaused.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Pause capture", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "When paused, Paste from Clipboard and shared text are not saved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = paused, onCheckedChange = viewModel::setCapturePaused)
            }
        }

        OutlinedButton(
            onClick = { showClearConfirm = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Clear history (snippets survive)")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("About clipboard capture", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Since Android 10, an app can only read the clipboard while " +
                        "it is in the foreground. Since Android 12, Android shows " +
                        "a toast whenever an app reads the clipboard, and since " +
                        "Android 13 the system may clear the clipboard after about " +
                        "an hour. WizardClip works with those limits instead of " +
                        "working around them: it never reads the clipboard in the " +
                        "background, never polls it, and has no accessibility " +
                        "service or foreground service. Capture here is " +
                        "foreground-only — Paste from Clipboard while this app is " +
                        "open, typing a clip, or sharing text to WizardClip.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                    Spacer(Modifier.size(6.dp))
                    Text("Privacy", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "WizardClip v1 is local-only. Clips and snippets are stored " +
                        "in a database on this device. There is no account, no " +
                        "login, no ads, no tracking, and no analytics. This app " +
                        "declares no network permission. Sensitive clips — OTP " +
                        "codes, password-like text, and card numbers — are masked " +
                        "and auto-deleted after 5 minutes.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "WizardClip 1.0.1 · Bigwizard Media · Open source (MIT)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear history?") },
            text = { Text("This deletes every clip in history. Your snippets will be kept.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearConfirm = false
                }) { Text("Clear history") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun SensitiveWarningCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.size(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("WizardClip", text))
}

private fun readClipboardText(context: Context): String? {
    return try {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (!manager.hasPrimaryClip()) return null
        manager.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString()
    } catch (_: Exception) {
        null
    }
}
