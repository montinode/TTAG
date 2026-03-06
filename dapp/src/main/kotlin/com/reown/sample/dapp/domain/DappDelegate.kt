package com.reown.sample.dapp.domain

import com.reown.android.Core
import com.reown.android.CoreClient
import com.reown.appkit.client.AppKit
import com.reown.appkit.client.Modal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Singleton delegate that bridges WalletConnect AppKit events to Kotlin Flows.
 * Activities and Fragments observe these flows to update their UI state.
 */
object DappDelegate : AppKit.ModalDelegate, CoreClient.CoreDelegate {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _wcEventModels = MutableSharedFlow<Modal.Model?>(extraBufferCapacity = 8)
    val wcEventModels: SharedFlow<Modal.Model?> = _wcEventModels.asSharedFlow()

    private val _connectionState = MutableStateFlow<Modal.Model.ConnectionState?>(null)
    val connectionState: StateFlow<Modal.Model.ConnectionState?> = _connectionState.asStateFlow()

    /** Currently active session topic (null when no session is active). */
    var selectedSessionTopic: String? = null
        private set

    init {
        AppKit.setDelegate(this)
        CoreClient.setDelegate(this)
    }

    // -------------------------------------------------------------------------
    // AppKit.ModalDelegate
    // -------------------------------------------------------------------------

    override fun onConnectionStateChange(state: Modal.Model.ConnectionState) {
        Timber.d("onConnectionStateChange: $state")
        scope.launch { _connectionState.emit(state) }
    }

    override fun onSessionApproved(approvedSession: Modal.Model.ApprovedSession) {
        if (approvedSession is Modal.Model.ApprovedSession.WalletConnectSession) {
            selectedSessionTopic = approvedSession.topic
        }
        scope.launch { _wcEventModels.emit(approvedSession) }
    }

    override fun onSessionRejected(rejectedSession: Modal.Model.RejectedSession) {
        scope.launch { _wcEventModels.emit(rejectedSession) }
    }

    override fun onSessionUpdate(updatedSession: Modal.Model.UpdatedSession) {
        scope.launch { _wcEventModels.emit(updatedSession) }
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.SessionEvent) {
        scope.launch { _wcEventModels.emit(sessionEvent) }
    }

    override fun onSessionEvent(sessionEvent: Modal.Model.Event) {
        scope.launch { _wcEventModels.emit(sessionEvent) }
    }

    override fun onSessionDelete(deletedSession: Modal.Model.DeletedSession) {
        clearSessionTopic()
        scope.launch { _wcEventModels.emit(deletedSession) }
    }

    override fun onSessionExtend(session: Modal.Model.Session) {
        scope.launch { _wcEventModels.emit(session) }
    }

    override fun onSessionRequestResponse(response: Modal.Model.SessionRequestResponse) {
        scope.launch { _wcEventModels.emit(response) }
    }

    override fun onSessionAuthenticateResponse(response: Modal.Model.SessionAuthenticateResponse) {
        if (response is Modal.Model.SessionAuthenticateResponse.Result) {
            selectedSessionTopic = response.session?.topic
        }
        scope.launch { _wcEventModels.emit(response) }
    }

    override fun onProposalExpired(proposal: Modal.Model.ExpiredProposal) {
        scope.launch { _wcEventModels.emit(proposal) }
    }

    override fun onRequestExpired(request: Modal.Model.ExpiredRequest) {
        scope.launch { _wcEventModels.emit(request) }
    }

    override fun onError(error: Modal.Model.Error) {
        Timber.e("DappDelegate error: ${error.throwable.message}")
        scope.launch { _wcEventModels.emit(error) }
    }

    // -------------------------------------------------------------------------
    // CoreClient.CoreDelegate
    // -------------------------------------------------------------------------

    override fun onPairingDelete(deletedPairing: Core.Model.DeletedPairing) {
        // Pairings are automatically managed by the SDK.
    }

    override fun onPairingExpired(expiredPairing: Core.Model.ExpiredPairing) {
        // Pairings are automatically managed by the SDK.
    }

    override fun onPairingState(pairingState: Core.Model.PairingState) {
        Timber.d("Pairing state: ${pairingState.isPairingState}")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    fun clearSessionTopic() {
        selectedSessionTopic = null
    }
}
