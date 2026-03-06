package com.reown.sample.dapp.ui

/**
 * One-shot UI events emitted from ViewModels to screens.
 */
sealed class DappSampleEvent {
    object SessionApproved : DappSampleEvent()
    object SessionRejected : DappSampleEvent()
    object ProposalExpired : DappSampleEvent()
    data class SessionAuthenticateApproved(val message: String?) : DappSampleEvent()
    object SessionAuthenticateRejected : DappSampleEvent()
    data class PingSuccess(val topic: String) : DappSampleEvent()
    object PingError : DappSampleEvent()
    object PingLoading : DappSampleEvent()
    object Disconnect : DappSampleEvent()
    data class DisconnectError(val message: String) : DappSampleEvent()
    data class RequestSuccess(val result: String) : DappSampleEvent()
    data class RequestPeerError(val errorMsg: String) : DappSampleEvent()
    data class RequestError(val exceptionMsg: String) : DappSampleEvent()
    data class ConnectionEvent(val isAvailable: Boolean) : DappSampleEvent()
    object NoAction : DappSampleEvent()
}
