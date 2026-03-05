import { mount } from 'svelte';
import App from './App.svelte';
import { registerSW } from 'virtual:pwa-register';

// Register the PWA service worker with automatic update handling
const updateSW = registerSW({
  onNeedRefresh() {
    if (confirm('New version available. Reload to update?')) {
      updateSW(true);
    }
  },
  onOfflineReady() {
    console.info('TTAG Monitor is ready to work offline.');
  },
});

const app = mount(App, { target: document.getElementById('app') });

export default app;
