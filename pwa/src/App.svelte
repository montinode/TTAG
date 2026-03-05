<script>
  import EthereumStatus from './lib/components/EthereumStatus.svelte';

  /** @type {'dashboard'|'ethereum'|'dns'} */
  let activeTab = $state('dashboard');

  const tabs = [
    { id: 'dashboard', label: '🛡 Dashboard' },
    { id: 'ethereum', label: '⛓ Ethereum' },
    { id: 'dns', label: '🌐 DNS' },
  ];

  /** DNS record data derived from dns/records.json structure – displayed as reference. */
  const dnsRecords = [
    { type: 'A', name: '@', value: '(set in dns/records.json)', ttl: 300 },
    { type: 'AAAA', name: '@', value: '(set in dns/records.json)', ttl: 300 },
    {
      type: 'TXT',
      name: '@',
      value: 'v=spf1 include:johncharlesmonti.com ~all',
      ttl: 3600,
    },
    {
      type: 'TLSA',
      name: '_443._tcp.pwa',
      value: '3 1 1 <cert-sha256>',
      ttl: 300,
    },
    { type: 'NS', name: '@', value: 'ns1.johncharlesmonti.com', ttl: 86400 },
  ];
</script>

<main>
  <header>
    <h1>🛡 TTAG Security Monitor</h1>
    <p class="subtitle">TelemetricTelephonyAutomationGeospatialAdministrationTektronicTracer</p>
  </header>

  <nav aria-label="Main navigation">
    {#each tabs as tab}
      <button
        class="tab-btn"
        class:active={activeTab === tab.id}
        onclick={() => (activeTab = tab.id)}
        aria-current={activeTab === tab.id ? 'page' : undefined}
      >
        {tab.label}
      </button>
    {/each}
  </nav>

  <section class="content">
    {#if activeTab === 'dashboard'}
      <div class="dashboard-grid">
        <div class="card">
          <h2>System Overview</h2>
          <ul class="stat-list">
            <li><span>Status</span><span class="badge ok">Operational</span></li>
            <li><span>PWA Mode</span><span class="badge info">Active</span></li>
            <li><span>Anti-Spoofing</span><span class="badge ok">Enabled</span></li>
            <li><span>MAGP Integration</span><span class="badge warn">Optional</span></li>
          </ul>
        </div>
        <div class="card">
          <h2>Quick Links</h2>
          <ul class="link-list">
            <li>
              <a href="/?tab=ethereum" onclick={(e) => { e.preventDefault(); activeTab = 'ethereum'; }}>
                Ethereum Network Status →
              </a>
            </li>
            <li>
              <a href="/?tab=dns" onclick={(e) => { e.preventDefault(); activeTab = 'dns'; }}>
                DNS Record Templates →
              </a>
            </li>
          </ul>
        </div>
      </div>
    {:else if activeTab === 'ethereum'}
      <EthereumStatus />
      <div class="card mt">
        <h2>Configuration</h2>
        <p>
          Set <code>VITE_ETHEREUM_RPC_URL</code> in your <code>.env</code> file to point to your
          private Ethereum node (e.g. <code>https://eth.johncharlesmonti.com</code>).
        </p>
        <p>See <code>pwa/src/lib/ethereum/provider.js</code> for details.</p>
      </div>
    {:else if activeTab === 'dns'}
      <div class="card">
        <h2>DNS Records – johncharlesmonti.com</h2>
        <p class="note">
          Reference view of the records defined in <code>dns/records.json</code>.
          Apply these with your DNS provider (see <code>dns/README.md</code>).
        </p>
        <table>
          <thead>
            <tr>
              <th>Type</th>
              <th>Name</th>
              <th>Value</th>
              <th>TTL</th>
            </tr>
          </thead>
          <tbody>
            {#each dnsRecords as record}
              <tr>
                <td><span class="badge type-{record.type.toLowerCase()}">{record.type}</span></td>
                <td><code>{record.name}</code></td>
                <td><code>{record.value}</code></td>
                <td>{record.ttl}s</td>
              </tr>
            {/each}
          </tbody>
        </table>
      </div>
    {/if}
  </section>
</main>

<style>
  :global(*, *::before, *::after) {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
  }

  :global(body) {
    font-family: system-ui, -apple-system, sans-serif;
    background: #0f0f23;
    color: #e0e0e0;
    min-height: 100vh;
  }

  main {
    max-width: 900px;
    margin: 0 auto;
    padding: 1.5rem 1rem;
  }

  header {
    margin-bottom: 1.5rem;
  }

  h1 {
    font-size: 1.6rem;
    color: #a8daff;
  }

  .subtitle {
    font-size: 0.75rem;
    color: #666;
    margin-top: 0.25rem;
    word-break: break-all;
  }

  nav {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
    flex-wrap: wrap;
  }

  .tab-btn {
    background: #16213e;
    color: #aaa;
    border: 1px solid #0f3460;
    border-radius: 6px;
    padding: 0.45rem 1rem;
    cursor: pointer;
    font-size: 0.9rem;
    transition: background 0.15s;
  }

  .tab-btn:hover {
    background: #1a2e5a;
    color: #e0e0e0;
  }

  .tab-btn.active {
    background: #0f3460;
    color: #a8daff;
    border-color: #a8daff;
  }

  .content {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }

  .dashboard-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1rem;
  }

  .card {
    background: #16213e;
    border: 1px solid #0f3460;
    border-radius: 8px;
    padding: 1.25rem 1.5rem;
  }

  .card.mt {
    margin-top: 0;
  }

  .card h2 {
    font-size: 1rem;
    color: #a8daff;
    margin-bottom: 0.75rem;
  }

  .stat-list,
  .link-list {
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .stat-list li {
    display: flex;
    justify-content: space-between;
    font-size: 0.9rem;
  }

  .link-list a {
    color: #60a5fa;
    text-decoration: none;
    font-size: 0.9rem;
  }

  .link-list a:hover {
    text-decoration: underline;
  }

  .badge {
    font-size: 0.75rem;
    padding: 0.15rem 0.5rem;
    border-radius: 4px;
    font-weight: 600;
  }

  .badge.ok {
    background: #14532d;
    color: #4ade80;
  }

  .badge.warn {
    background: #713f12;
    color: #fbbf24;
  }

  .badge.info {
    background: #1e3a5f;
    color: #60a5fa;
  }

  .badge.type-a,
  .badge.type-aaaa {
    background: #1e3a5f;
    color: #60a5fa;
  }

  .badge.type-txt {
    background: #14532d;
    color: #4ade80;
  }

  .badge.type-tlsa {
    background: #4c1d95;
    color: #c4b5fd;
  }

  .badge.type-ns {
    background: #713f12;
    color: #fbbf24;
  }

  table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.85rem;
    margin-top: 0.75rem;
  }

  th {
    text-align: left;
    color: #888;
    padding: 0.4rem 0.6rem;
    border-bottom: 1px solid #0f3460;
  }

  td {
    padding: 0.45rem 0.6rem;
    border-bottom: 1px solid #0d1a33;
    vertical-align: middle;
  }

  tr:last-child td {
    border-bottom: none;
  }

  code {
    font-family: monospace;
    font-size: 0.82rem;
    color: #c0c0e0;
  }

  p {
    font-size: 0.9rem;
    color: #aaa;
    margin-bottom: 0.5rem;
    line-height: 1.5;
  }

  .note {
    font-style: italic;
  }
</style>
