package com.reown.sample.dapp.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.reown.sample.dapp.databinding.FragmentSessionBinding
import com.reown.sample.dapp.ui.DappSampleEvent
import com.reown.sample.dapp.ui.routes.SessionViewModel
import kotlinx.coroutines.launch

/**
 * Screen shown after a WalletConnect session is established.
 *
 * Allows the user to:
 *  - See connected wallet details
 *  - Ping the wallet
 *  - Send a personal_sign request
 *  - Disconnect the session
 */
class SessionFragment : Fragment() {

    private var _binding: FragmentSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionViewModel by viewModels()

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeUiState()
        observeEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    private fun setupButtons() {
        binding.btnPing.setOnClickListener { viewModel.ping() }
        binding.btnPersonalSign.setOnClickListener {
            viewModel.sendPersonalSign("Kotlin dApp sample — please sign to verify ownership")
        }
        binding.btnDisconnect.setOnClickListener { viewModel.disconnect() }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.sessionState.collect { state ->
                        if (state != null) {
                            binding.tvPeerName.text = state.peerName
                            binding.tvPeerUrl.text = state.peerUrl
                            binding.tvAccounts.text = state.accounts.joinToString("\n")
                        } else {
                            // Session ended — go back to chain selection
                            navigateBack()
                        }
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                        binding.btnPing.isEnabled = !loading
                        binding.btnPersonalSign.isEnabled = !loading
                        binding.btnDisconnect.isEnabled = !loading
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event -> handleEvent(event) }
            }
        }
    }

    private fun handleEvent(event: DappSampleEvent) {
        when (event) {
            is DappSampleEvent.PingSuccess ->
                toast("Ping successful: ${event.topic}")
            is DappSampleEvent.PingError ->
                toast("Ping failed")
            is DappSampleEvent.PingLoading ->
                toast("Pinging wallet…")
            is DappSampleEvent.RequestSuccess ->
                toast("Signature: ${event.result}")
            is DappSampleEvent.RequestPeerError ->
                toast("Wallet error: ${event.errorMsg}")
            is DappSampleEvent.RequestError ->
                toast("Request failed: ${event.exceptionMsg}")
            is DappSampleEvent.Disconnect -> navigateBack()
            is DappSampleEvent.DisconnectError ->
                toast("Disconnect error: ${event.message}")
            else -> Unit
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun toast(msg: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateBack() {
        requireActivity().runOnUiThread {
            parentFragmentManager.popBackStack()
        }
    }
}
