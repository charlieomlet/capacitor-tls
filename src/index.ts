import { registerPlugin } from '@capacitor/core';

import type { CapacitorTlsPlugin } from './definitions';

const CapacitorTls = registerPlugin<CapacitorTlsPlugin>('CapacitorTls', {
  web: () => import('./web').then((m) => new m.CapacitorTlsWeb()),
});

export * from './definitions';
export { CapacitorTls };
