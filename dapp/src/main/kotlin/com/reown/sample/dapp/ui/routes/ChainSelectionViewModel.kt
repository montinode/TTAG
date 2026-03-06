package com.reown.sample.dapp.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.sample.dapp.domain.DappDelegate
import com.reown.sample.dapp.ui.ChainItem
import com.reown.sample.dapp.ui.DappSampleEvent
import com.reown.sample.dapp.ui.SupportedChains
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * ViewModel for the chain-selection screen.
 *
 * Exposes:
 *  - [chainsUiState]        – list of chains with their selection state
 *  - [isAwaiting]           – true while we wait for the wallet to respond
 *  - [events]               – one-shot navigation/feedback events
 */
class ChainSelectionViewModel : ViewModel() {

    private val _chainsUiState = MutableStateFlow(SupportedChains.all.map { it.copy() })
    val chainsUiState: StateFlow<List<ChainItem>> = _chainsUiState.asStateFlow()

    private val _isAwaiting = MutableStateFlow(false)
    val isAwaiting: StateFlow<Boolean> = _isAwaiting.asStateFlow()

    private val _events = MutableSharedFlow<DappSampleEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<DappSampleEvent> = _events.asSharedFlow()

    val isAnyChainSelected: Boolean
        get() = _chainsUiState.value.any { it.isSelected }

    init {
        observeWalletEvents()
    }

    // -------------------------------------------------------------------------
    // Public actions
    // -------------------------------------------------------------------------

    fun toggleChain(position: Int) {
        _chainsUiState.update { chains ->
            chains.toMutableList().also { list ->
                list[position] = list[position].copy(isSelected = !list[position].isSelected)
            }
        }
    }

    /**
     * Open a WalletConnect session.  Emits a pairing URI on success (shown as QR code or
     * forwarded to an installed wallet app).
     */
    fun connectToWallet(
        onPairingUri: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!isAnyChainSelected) {
            onError("Please select at least one chain.")
            return
        }

        viewModelScope.launch { _isAwaiting.emit(true) }

        try {
            val pairing = CoreClient.Pairing.create { error ->
                viewModelScope.launch { _isAwaiting.emit(false) }
                Timber.e("Pairing creation failed: ${error.throwable.message}")
                onError(error.throwable.message ?: "Failed to create pairing")
            } ?: return

            val connectParams = Modal.Params.ConnectParams(
                sessionNamespaces = buildOptionalNamespaces(),
                properties = buildSessionProperties(),
                pairing = pairing
            )

            AppKit.connect(
                connectParams = connectParams,
                onSuccess = { uri ->
                    viewModelScope.launch { _isAwaiting.emit(false) }
                    onPairingUri(uri)
                },
                onError = { error ->
                    viewModelScope.launch { _isAwaiting.emit(false) }
                    Timber.e("AppKit connect error: ${error.throwable.message}")
                    onError(error.throwable.message ?: "Connection failed")
                }
            )
        } catch (e: Exception) {
            viewModelScope.launch { _isAwaiting.emit(false) }
            Timber.e(e, "connectToWallet exception")
            onError(e.message ?: "Unknown error")
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun buildOptionalNamespaces(): Map<String, Modal.Model.Namespace.Proposal> =
        _chainsUiState.value
            .filter { it.isSelected }
            .groupBy { it.chainId }
            .mapValues { (_, chains) ->
                Modal.Model.Namespace.Proposal(
                    methods = chains.flatMap { it.methods }.distinct(),
                    events = chains.flatMap { it.events }.distinct()
                )
            }

    private fun buildSessionProperties(): Map<String, String> {
        val expiry = (System.currentTimeMillis() / 1000) +
                TimeUnit.SECONDS.convert(7, TimeUnit.DAYS)
        return mapOf("sessionExpiry" to "$expiry")
    }

    private fun observeWalletEvents() {
        DappDelegate.wcEventModels
            .map { model ->
                when (model) {
                    is Modal.Model.ApprovedSession -> DappSampleEvent.SessionApproved
                    is Modal.Model.RejectedSession -> DappSampleEvent.SessionRejected
                    is Modal.Model.ExpiredProposal -> DappSampleEvent.ProposalExpired
                    is Modal.Model.SessionAuthenticateResponse -> {
                        if (model is Modal.Model.SessionAuthenticateResponse.Result)
                            DappSampleEvent.SessionAuthenticateApproved(null)
                        else
                            DappSampleEvent.SessionAuthenticateRejected
                    }
                    else -> DappSampleEvent.NoAction
                }
            }
            .onEach { event ->
                if (event !is DappSampleEvent.NoAction) {
                    _events.emit(event)
                }
            }
            .launchIn(viewModelScope)

        DappDelegate.connectionState
            .onEach { state ->
                if (state != null) {
                    _events.emit(DappSampleEvent.ConnectionEvent(state.isAvailable))
                }
            }
            .launchIn(viewModelScope)
    }
}
