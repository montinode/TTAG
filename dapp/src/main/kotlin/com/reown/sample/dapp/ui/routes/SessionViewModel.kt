package com.reown.sample.dapp.ui.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import com.reown.appkit.client.models.Session
import com.reown.appkit.client.models.request.Request
import com.reown.sample.dapp.domain.DappDelegate
import com.reown.sample.dapp.ui.DappSampleEvent
import com.reown.sample.dapp.util.encodeToHex
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the active-session screen.
 *
 * Exposes:
 *  - [sessionState]   – current session data (null if no active session)
 *  - [isLoading]      – true while a network operation is in flight
 *  - [events]         – one-shot events (ping result, disconnect, request result …)
 */
class SessionViewModel : ViewModel() {

    data class SessionUiState(
        val peerName: String,
        val peerUrl: String,
        val accounts: List<String>
    )

    private val _sessionState = MutableStateFlow<SessionUiState?>(null)
    val sessionState: StateFlow<SessionUiState?> = _sessionState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<DappSampleEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<DappSampleEvent> = _events.asSharedFlow()

    init {
        loadActiveSession()
        observeWalletEvents()
    }

    // -------------------------------------------------------------------------
    // Public actions
    // -------------------------------------------------------------------------

    fun ping() {
        viewModelScope.launch {
            _isLoading.emit(true)
            _events.emit(DappSampleEvent.PingLoading)
        }

        try {
            AppKit.ping(object : Modal.Listeners.SessionPing {
                override fun onSuccess(pingSuccess: Modal.Model.Ping.Success) {
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        _events.emit(DappSampleEvent.PingSuccess(pingSuccess.topic))
                    }
                }

                override fun onError(pingError: Modal.Model.Ping.Error) {
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        _events.emit(DappSampleEvent.PingError)
                    }
                }
            })
        } catch (e: Exception) {
            viewModelScope.launch {
                _isLoading.emit(false)
                _events.emit(DappSampleEvent.PingError)
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch { _isLoading.emit(true) }

        try {
            AppKit.disconnect(
                onSuccess = {
                    DappDelegate.clearSessionTopic()
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        _sessionState.emit(null)
                        _events.emit(DappSampleEvent.Disconnect)
                    }
                },
                onError = { throwable ->
                    Timber.e("Disconnect error: ${throwable.message}")
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        _events.emit(
                            DappSampleEvent.DisconnectError(
                                throwable.message ?: "Disconnect failed"
                            )
                        )
                    }
                }
            )
        } catch (e: Exception) {
            viewModelScope.launch {
                _isLoading.emit(false)
                _events.emit(
                    DappSampleEvent.DisconnectError(e.message ?: "Unknown error")
                )
            }
        }
    }

    /**
     * Sends a personal_sign request to the connected wallet.
     */
    fun sendPersonalSign(message: String) {
        val session = (AppKit.getSession() as? Session.WalletConnectSession) ?: return
        val accounts = session.namespaces.values.flatMap { it.accounts }
        val evmAccount = accounts.firstOrNull { it.startsWith("eip155") } ?: return

        // CAIP-10 format: namespace:chainReference:address
        val parts = evmAccount.split(":")
        if (parts.size < 3) return
        val chainId = "${parts[0]}:${parts[1]}"
        val address = parts[2]
        val hexMessage = message.encodeToHex()
        val params = "[\"$hexMessage\", \"$address\"]"

        viewModelScope.launch { _isLoading.emit(true) }

        try {
            AppKit.request(
                request = Request(
                    method = "personal_sign",
                    params = params,
                    chainId = chainId
                ),
                onSuccess = {
                    viewModelScope.launch { _isLoading.emit(false) }
                },
                onError = { error ->
                    Timber.e("Request error: ${error.localizedMessage}")
                    viewModelScope.launch {
                        _isLoading.emit(false)
                        _events.emit(
                            DappSampleEvent.RequestError(
                                error.localizedMessage ?: "Request failed"
                            )
                        )
                    }
                }
            )
        } catch (e: Exception) {
            viewModelScope.launch {
                _isLoading.emit(false)
                _events.emit(DappSampleEvent.RequestError(e.message ?: "Unknown error"))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun loadActiveSession() {
        val session = (AppKit.getSession() as? Session.WalletConnectSession) ?: return
        val accounts = session.namespaces.values.flatMap { it.accounts }
        viewModelScope.launch {
            _sessionState.emit(
                SessionUiState(
                    peerName = session.metaData?.name ?: "Unknown Wallet",
                    peerUrl = session.metaData?.url ?: "",
                    accounts = accounts
                )
            )
        }
    }

    private fun observeWalletEvents() {
        DappDelegate.wcEventModels
            .filterNotNull()
            .onEach { model ->
                when (model) {
                    is Modal.Model.SessionRequestResponse -> {
                        when (val result = model.result) {
                            is Modal.Model.JsonRpcResponse.JsonRpcResult ->
                                _events.emit(DappSampleEvent.RequestSuccess(result.result))
                            is Modal.Model.JsonRpcResponse.JsonRpcError ->
                                _events.emit(
                                    DappSampleEvent.RequestPeerError(
                                        "Error ${result.code}: ${result.message}"
                                    )
                                )
                        }
                    }
                    is Modal.Model.DeletedSession -> {
                        _sessionState.emit(null)
                        _events.emit(DappSampleEvent.Disconnect)
                    }
                    is Modal.Model.UpdatedSession -> loadActiveSession()
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    // -------------------------------------------------------------------------
    // Extension helpers — hex encoding shared via util/StringExtensions.kt
    // -------------------------------------------------------------------------
}
