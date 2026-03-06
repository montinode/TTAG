package com.reown.sample.dapp.ui

import android.graphics.Color

/**
 * Represents a blockchain chain that the user can select to connect via WalletConnect.
 */
data class ChainItem(
    val chainName: String,
    val chainNamespace: String,
    val chainReference: String,
    val colorHex: String,
    val methods: List<String>,
    val events: List<String>,
    var isSelected: Boolean = false
) {
    val chainId: String get() = "$chainNamespace:$chainReference"

    val displayColor: Int
        get() = try {
            Color.parseColor(colorHex)
        } catch (e: IllegalArgumentException) {
            Color.GRAY
        }
}

/**
 * Pre-configured set of supported EVM and non-EVM chains.
 */
object SupportedChains {

    private val ETH_METHODS = listOf(
        "eth_sendTransaction",
        "personal_sign",
        "eth_signTypedData",
        "eth_signTypedData_v4"
    )
    private val ETH_EVENTS = listOf("chainChanged", "accountsChanged")

    private val SOLANA_METHODS = listOf(
        "solana_signTransaction",
        "solana_signMessage"
    )
    private val SOLANA_EVENTS = listOf<String>()

    val all: List<ChainItem> = listOf(
        ChainItem("Ethereum", "eip155", "1", "#627EEA", ETH_METHODS, ETH_EVENTS),
        ChainItem("Polygon", "eip155", "137", "#8247E5", ETH_METHODS, ETH_EVENTS),
        ChainItem("Avalanche", "eip155", "43114", "#E84142", ETH_METHODS, ETH_EVENTS),
        ChainItem("Optimism", "eip155", "10", "#FF0420", ETH_METHODS, ETH_EVENTS),
        ChainItem("Arbitrum", "eip155", "42161", "#28A0F0", ETH_METHODS, ETH_EVENTS),
        ChainItem("Base", "eip155", "8453", "#0052FF", ETH_METHODS, ETH_EVENTS),
        ChainItem("BNB Smart Chain", "eip155", "56", "#F0B90B", ETH_METHODS, ETH_EVENTS),
        ChainItem("Solana", "solana", "5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp", "#9945FF",
            SOLANA_METHODS, SOLANA_EVENTS)
    )
}
