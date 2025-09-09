import Foundation
import Network
import Security

final class TLSConnection {

    enum Event {
        case ready
        case received(Data)
        case error(String)
        case closed(String?)
    }

    private let id: String
    private let host: String
    private let port: UInt16
    private let insecure: Bool
    private let sni: String?
    private let alpn: [String]
    private let timeoutMs: Int

    private var connection: NWConnection?
    private let queue = DispatchQueue(label: "CapacitorTls.\(UUID().uuidString)")
    private let handler: (Event) -> Void
    private var connectTimer: DispatchSourceTimer?

    init(
        id: String,
        host: String,
        port: UInt16,
        insecure: Bool,
        sni: String?,
        alpn: [String],
        timeoutMs: Int,
        handler: @escaping (Event) -> Void
    ) {
        self.id = id
        self.host = host
        self.port = port
        self.insecure = insecure
        self.sni = sni
        self.alpn = alpn
        self.timeoutMs = timeoutMs
        self.handler = handler
    }

    func start() {
        let tlsOptions = NWProtocolTLS.Options()
        let sec = tlsOptions.securityProtocolOptions

        // Pin minimum TLS version to 1.2 (mirrors openssl -tls1_2)
        sec_protocol_options_set_min_tls_protocol_version(sec, .TLSv12)

        // ALPN
        for proto in alpn {
            proto.withCString { cstr in
                sec_protocol_options_add_tls_application_protocol(sec, cstr)
            }
        }

        // SNI override
        if let sniHost = (sni?.isEmpty == false ? sni : nil) {
            sec_protocol_options_set_tls_server_name(sec, sniHost)
        }

        if insecure {
            // DEV ONLY: trust-all (do not ship to production)
            sec_protocol_options_set_verify_block(sec, { _, _, completion in
                completion(true)
            }, queue)
        }

        let params = NWParameters(tls: tlsOptions)
        params.allowLocalEndpointReuse = true

        // Optional: TCP keepalives help ride out short AP flaps
        if let tcp = params.defaultProtocolStack.internetProtocol as? NWProtocolTCP.Options {
            tcp.enableKeepalive = true
            tcp.keepaliveCount = 3
            tcp.keepaliveIdle = 10
            tcp.keepaliveInterval = 3
        }

        guard let nwPort = NWEndpoint.Port(rawValue: self.port) else {
            handler(.error("Invalid port"))
            handler(.closed("Invalid port"))
            return
        }

        let conn = NWConnection(host: NWEndpoint.Host(self.host), port: nwPort, using: params)
        self.connection = conn

        conn.stateUpdateHandler = { [weak self] state in
            guard let self = self else { return }
            switch state {
            case .ready:
                self.cancelConnectTimer()
                self.handler(.ready)
                self.receiveLoop()
            case .failed(let error):
                self.cancelConnectTimer()
                self.handler(.error("TLS connection failed: \(error.localizedDescription)"))
                self.handler(.closed(error.localizedDescription))
            case .cancelled:
                self.cancelConnectTimer()
                self.handler(.closed(nil))
            default:
                break
            }
        }

        conn.start(queue: queue)

        // Best-effort connect timeout
        if timeoutMs > 0 {
            let timer = DispatchSource.makeTimerSource(queue: queue)
            timer.schedule(deadline: .now() + .milliseconds(timeoutMs))
            timer.setEventHandler { [weak self] in
                guard let self = self else { return }
                self.handler(.error("Connect timeout after \(self.timeoutMs)ms"))
                self.stop()
                self.handler(.closed("timeout"))
            }
            timer.resume()
            self.connectTimer = timer
        }
    }

    private func cancelConnectTimer() {
        connectTimer?.cancel()
        connectTimer = nil
    }

    private func receiveLoop() {
        connection?.receive(minimumIncompleteLength: 1, maximumLength: 64 * 1024) { [weak self] data, _, isComplete, error in
            guard let self = self else { return }
            if let d = data, !d.isEmpty {
                self.handler(.received(d))
            }
            if let e = error {
                self.handler(.error("Receive error: \(e.localizedDescription)"))
                self.stop()
                return
            }
            if isComplete {
                self.handler(.closed(nil))
                self.stop()
                return
            }
            self.receiveLoop()
        }
    }

    func send(data: Data, completion: @escaping (String?) -> Void) {
        connection?.send(content: data, completion: .contentProcessed({ error in
            completion(error?.localizedDescription)
        }))
    }

    func stop() {
        cancelConnectTimer()
        connection?.cancel()
        connection = nil
    }
}