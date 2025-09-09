import { registerPlugin } from '@capacitor/core';
const CapacitorTls = registerPlugin('CapacitorTls', {
    web: () => import('./web').then((m) => new m.CapacitorTlsWeb()),
});
export * from './definitions';
export { CapacitorTls };
//# sourceMappingURL=index.js.map