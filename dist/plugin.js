var capacitorCapacitorTls = (function (exports, core) {
    'use strict';

    const CapacitorTls = core.registerPlugin('CapacitorTls', {
        web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.CapacitorTlsWeb()),
    });

    class CapacitorTlsWeb extends core.WebPlugin {
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

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        CapacitorTlsWeb: CapacitorTlsWeb
    });

    exports.CapacitorTls = CapacitorTls;

    return exports;

})({}, capacitorExports);
//# sourceMappingURL=plugin.js.map
