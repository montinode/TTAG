/**
 * Ethereum provider configuration.
 *
 * The provider URL is read from the VITE_ETHEREUM_RPC_URL environment variable.
 * Set this to your private Ethereum node endpoint (e.g. the node hosted at
 * johncharlesmonti.com) in .env or at build time.
 *
 * Example .env:
 *   VITE_ETHEREUM_RPC_URL=https://eth.johncharlesmonti.com
 *
 * If the variable is not set, the provider falls back to the public Ethereum
 * mainnet endpoint provided by PublicNode.
 */

import { ethers } from 'ethers';

/** URL of the Ethereum JSON-RPC endpoint (private server or public fallback). */
const RPC_URL =
  import.meta.env.VITE_ETHEREUM_RPC_URL || 'https://ethereum.publicnode.com';

/** Human-readable label shown in the UI. */
export const providerLabel = import.meta.env.VITE_ETHEREUM_RPC_URL
  ? 'Private Node (johncharlesmonti.com)'
  : 'Public Fallback (publicnode.com)';

/**
 * Create and return a new ethers.js JsonRpcProvider connected to the
 * configured Ethereum endpoint.
 *
 * @returns {ethers.JsonRpcProvider}
 */
export function getProvider() {
  return new ethers.JsonRpcProvider(RPC_URL);
}

/**
 * Fetch basic network information from the configured Ethereum node.
 *
 * @returns {Promise<{name: string, chainId: bigint, blockNumber: number}>}
 */
export async function getNetworkInfo() {
  const provider = getProvider();
  const [network, blockNumber] = await Promise.all([
    provider.getNetwork(),
    provider.getBlockNumber(),
  ]);
  return {
    name: network.name,
    chainId: network.chainId,
    blockNumber,
  };
}
