package com.reown.sample.dapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.reown.appkit.client.AppKit
import com.reown.sample.dapp.R
import com.reown.sample.dapp.databinding.ActivityDappSampleBinding
import com.reown.sample.dapp.domain.DappDelegate
import com.reown.sample.dapp.ui.screens.ChainSelectionFragment
import com.reown.sample.dapp.ui.screens.SessionFragment

/**
 * Entry-point Activity for the WalletConnect sample dApp.
 *
 * Responsibilities:
 *  - Initialise [DappDelegate] so it starts listening to WalletConnect events
 *  - Show [ChainSelectionFragment] or [SessionFragment] based on whether a
 *    session is already active
 *  - Handle deep-link intents forwarded by the wallet app
 */
class DappSampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDappSampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDappSampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure the delegate is initialised (it's a singleton; safe to call multiple times)
        DappDelegate.apply { /* lazy initialisation via the init block */ }

        if (savedInstanceState == null) {
            showInitialScreen()
        }

        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun showInitialScreen() {
        val fragment = if (DappDelegate.selectedSessionTopic != null) {
            SessionFragment()
        } else {
            ChainSelectionFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: String = intent?.dataString ?: return
        if (data.contains("wc_ev")) {
            AppKit.handleDeepLink(data) { error ->
                Toast.makeText(
                    this,
                    "Error handling WalletConnect deep link: ${error.throwable.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
