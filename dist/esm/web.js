import { WebPlugin } from '@capacitor/core';
export class CapacitorTlsWeb extends WebPlugin {
    async connect(_options) {
        throw this.unimplemented('TLS sockets are not available on the web.');
    }
    async send(_options) {
        throw this.unimplemented('TLS sockets are not available on the web.');
    }
    async disconnect(_options) {
        throw this.unimplemented('TLS sockets are not available on the web.');
    }
    async disconnectAll() {
        throw this.unimplemented('TLS sockets are not available on the web.');
    }
    async bindToWifi() {
        throw this.unimplemented('Not implemented on Web');
    }
    async unbindNetwork() {
        throw this.unimplemented('Not implemented on Web');
    }
}
//# sourceMappingURL=web.js.map