package info.bigwizardmedia.wizardclip

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    private val viewModel: WizardClipViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleShareIntent(intent)
        setContent {
            WizardClipTheme {
                WizardClipApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShareIntent(intent)
    }

    /**
     * ACTION_SEND (text/plain) receiver: another app shares text to
     * WizardClip while WizardClip is in the foreground. No background
     * clipboard reading is involved.
     */
    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (intent.type?.startsWith("text/") != true) return
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        viewModel.handleSharedText(sharedText)
    }
}
