package com.reown.sample.dapp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.reown.sample.dapp.databinding.DialogPairingUriBinding

/**
 * Dialog that displays the WalletConnect pairing URI as a QR code placeholder
 * and allows the user to copy it or open a wallet app.
 */
class PairingUriDialogFragment : DialogFragment() {

    private var _binding: DialogPairingUriBinding? = null
    private val binding get() = _binding!!

    private val pairingUri: String by lazy {
        requireArguments().getString(ARG_URI, "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPairingUriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvPairingUri.text = pairingUri

        binding.btnCopyUri.setOnClickListener {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("WC URI", pairingUri))
            Toast.makeText(requireContext(), "URI copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.btnOpenWallet.setOnClickListener {
            openWithExternalWallet()
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun openWithExternalWallet() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(pairingUri))
            startActivity(intent)
            dismiss()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "No wallet app found. Copy the URI and paste it in your wallet.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    companion object {
        const val TAG = "PairingUriDialog"
        private const val ARG_URI = "arg_uri"

        fun newInstance(uri: String): PairingUriDialogFragment =
            PairingUriDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_URI, uri) }
            }
    }
}
