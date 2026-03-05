<script>
  import { onMount } from 'svelte';
  import { getNetworkInfo, providerLabel } from '../ethereum/provider.js';

  /** @type {'idle'|'loading'|'ok'|'error'} */
  let status = $state('idle');
  let networkName = $state('');
  let chainId = $state('');
  let blockNumber = $state('');
  let errorMessage = $state('');

  async function fetchStatus() {
    status = 'loading';
    errorMessage = '';
    try {
      const info = await getNetworkInfo();
      networkName = info.name;
      chainId = info.chainId.toString();
      blockNumber = info.blockNumber.toLocaleString();
      status = 'ok';
    } catch (err) {
      errorMessage = err?.message ?? 'Unknown error';
      status = 'error';
    }
  }

  onMount(() => {
    fetchStatus();
  });
</script>

<section class="eth-status" aria-label="Ethereum Network Status">
  <h2>⛓ Ethereum Status</h2>
  <p class="provider-label">Provider: <strong>{providerLabel}</strong></p>

  {#if status === 'loading'}
    <p class="info">Connecting to Ethereum node…</p>
  {:else if status === 'ok'}
    <ul class="stats">
      <li><span class="label">Network</span><span class="value">{networkName}</span></li>
      <li><span class="label">Chain ID</span><span class="value">{chainId}</span></li>
      <li><span class="label">Latest Block</span><span class="value">#{blockNumber}</span></li>
    </ul>
    <p class="ok">✅ Connected</p>
  {:else if status === 'error'}
    <p class="error">⚠ Connection failed: {errorMessage}</p>
    <button onclick={fetchStatus}>Retry</button>
  {:else}
    <p class="info">Idle</p>
  {/if}
</section>

<style>
  .eth-status {
    background: #16213e;
    border: 1px solid #0f3460;
    border-radius: 8px;
    padding: 1.25rem 1.5rem;
    color: #e0e0e0;
  }

  h2 {
    margin: 0 0 0.5rem;
    font-size: 1.1rem;
    color: #a8daff;
  }

  .provider-label {
    font-size: 0.85rem;
    color: #aaa;
    margin: 0 0 1rem;
  }

  .stats {
    list-style: none;
    padding: 0;
    margin: 0 0 0.75rem;
    display: grid;
    gap: 0.4rem;
  }

  .stats li {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
  }

  .label {
    color: #888;
  }

  .value {
    font-family: monospace;
    color: #e0e0e0;
  }

  .ok {
    color: #4ade80;
    font-size: 0.9rem;
    margin: 0;
  }

  .error {
    color: #f87171;
    font-size: 0.9rem;
    margin: 0 0 0.5rem;
  }

  .info {
    color: #aaa;
    font-size: 0.9rem;
    font-style: italic;
    margin: 0;
  }

  button {
    background: #0f3460;
    color: #e0e0e0;
    border: none;
    border-radius: 4px;
    padding: 0.4rem 0.9rem;
    cursor: pointer;
    font-size: 0.85rem;
  }

  button:hover {
    background: #1a4a80;
  }
</style>
