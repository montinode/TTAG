package com.reown.sample.dapp.ui.screens

import android.content.Intent
import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reown.sample.dapp.databinding.FragmentChainSelectionBinding
import com.reown.sample.dapp.databinding.ItemChainBinding
import com.reown.sample.dapp.ui.ChainItem
import com.reown.sample.dapp.ui.DappSampleEvent
import com.reown.sample.dapp.ui.routes.ChainSelectionViewModel
import kotlinx.coroutines.launch

/**
 * Screen that lets the user pick which chain(s) to connect with and
 * initiates a WalletConnect pairing.
 */
class ChainSelectionFragment : Fragment() {

    private var _binding: FragmentChainSelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChainSelectionViewModel by viewModels()
    private lateinit var chainAdapter: ChainAdapter

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChainSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupConnectButton()
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

    private fun setupRecyclerView() {
        chainAdapter = ChainAdapter { position, isSelected ->
            viewModel.toggleChain(position)
        }
        binding.rvChains.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chainAdapter
        }
    }

    private fun setupConnectButton() {
        binding.btnConnect.setOnClickListener {
            if (!viewModel.isAnyChainSelected) {
                Toast.makeText(requireContext(), "Select at least one chain", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            viewModel.connectToWallet(
                onPairingUri = { uri -> showPairingUri(uri) },
                onError = { msg ->
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.chainsUiState.collect { chains ->
                        chainAdapter.submitList(chains.map { it.copy() })
                        binding.btnConnect.isEnabled = chains.any { it.isSelected }
                    }
                }
                launch {
                    viewModel.isAwaiting.collect { awaiting ->
                        binding.progressBar.visibility = if (awaiting) View.VISIBLE else View.GONE
                        binding.btnConnect.isEnabled = !awaiting && viewModel.isAnyChainSelected
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    private fun handleEvent(event: DappSampleEvent) {
        when (event) {
            is DappSampleEvent.SessionApproved -> {
                Toast.makeText(requireContext(), "Session approved!", Toast.LENGTH_SHORT).show()
                navigateToSession()
            }
            is DappSampleEvent.SessionRejected ->
                Toast.makeText(requireContext(), "Session rejected", Toast.LENGTH_SHORT).show()
            is DappSampleEvent.ProposalExpired ->
                Toast.makeText(requireContext(), "Proposal expired", Toast.LENGTH_SHORT).show()
            is DappSampleEvent.ConnectionEvent -> {
                val msg = if (event.isAvailable) "Connected to relay" else "Disconnected from relay"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    // -------------------------------------------------------------------------
    // Navigation / dialogs
    // -------------------------------------------------------------------------

    private fun showPairingUri(uri: String) {
        requireActivity().runOnUiThread {
            // Show QR dialog or try to open installed wallet
            val dialog = PairingUriDialogFragment.newInstance(uri)
            dialog.show(parentFragmentManager, PairingUriDialogFragment.TAG)
        }
    }

    private fun navigateToSession() {
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, SessionFragment())
            .addToBackStack(null)
            .commit()
    }

    // =========================================================================
    // RecyclerView Adapter
    // =========================================================================

    private inner class ChainAdapter(
        private val onToggle: (Int, Boolean) -> Unit
    ) : RecyclerView.Adapter<ChainAdapter.ChainViewHolder>() {

        private var items: List<ChainItem> = emptyList()

        fun submitList(list: List<ChainItem>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChainViewHolder {
            val binding = ItemChainBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ChainViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ChainViewHolder, position: Int) {
            holder.bind(items[position], position)
        }

        override fun getItemCount() = items.size

        inner class ChainViewHolder(private val binding: ItemChainBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(chain: ChainItem, position: Int) {
                binding.tvChainName.text = chain.chainName
                binding.tvChainId.text = chain.chainId
                binding.viewColor.setBackgroundColor(chain.displayColor)
                binding.cbSelected.isChecked = chain.isSelected
                binding.root.setOnClickListener { onToggle(position, chain.isSelected) }
            }
        }
    }
}
