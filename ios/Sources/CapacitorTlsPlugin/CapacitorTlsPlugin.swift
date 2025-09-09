import Foundation
import Capacitor
import Network

@objc(CapacitorTlsPlugin)
public class CapacitorTlsPlugin: CAPPlugin {

    private var connections: [String: TLSConnection] = [:]

    // MARK: Core API

    @objc public func connect(_ call: CAPPluginCall) {
        guard let host = call.getString("host"),
              let port = call.getInt("port") else {
            call.reject("'host' and 'port' are required")
            return
        }

        let insecure = call.getBool("insecure") ?? false
        let sni = call.getString("sni")
        let alpn = call.getArray("alpnProtocols", String.self) ?? []
        let id = call.getString("id") ?? UUID().uuidString
        let timeoutMs = call.getInt("timeout") ?? 10_000

        if connections[id] != nil {
            call.reject("Connection id already in use: \(id)")
            return
        }

        let conn = TLSConnection(
            id: id,
            host: host,
            port: UInt16(port),
            insecure: insecure,
            sni: sni,
            alpn: alpn,
            timeoutMs: timeoutMs
        ) { [weak self] event in
            guard let self = self else { return }
            switch event {
            case .ready:
                self.notifyListeners("connect", data: ["id": id])
            case .received(let data):
                self.notifyListeners("data", data: [
                    "id": id,
                    "data": data.base64EncodedString(),
                    "encoding": "base64"
                ])
            case .error(let msg):
                self.notifyListeners("error", data: ["id": id, "error": msg])
            case .closed(let errorMsg):
                var payload: [String: Any] = ["id": id]
                if let e = errorMsg { payload["error"] = e }
                self.notifyListeners("close", data: payload)
                self.connections.removeValue(forKey: id)
            }
        }

        connections[id] = conn
        conn.start()
        call.resolve(["id": id])
    }

    @objc public func send(_ call: CAPPluginCall) {
        guard let id = call.getString("id"),
              let dataStr = call.getString("data") else {
            call.reject("'id' and 'data' are required")
            return
        }

        let encoding = call.getString("encoding") ?? "base64"
        guard let conn = connections[id] else {
            call.reject("No such connection: \(id)")
            return
        }

        let data: Data?
        switch encoding {
            case "base64": data = Data(base64Encoded: dataStr)
            case "utf8":   data = dataStr.data(using: .utf8)
            case "hex":    data = Data(hexString: dataStr)
            default:       data = Data(base64Encoded: dataStr)
        }

        guard let toSend = data else {
            call.reject("Failed to decode data with encoding: \(encoding)")
            return
        }

        conn.send(data: toSend) { err in
            if let err = err { call.reject(err) } else { call.resolve() }
        }
    }

    @objc public func disconnect(_ call: CAPPluginCall) {
        guard let id = call.getString("id") else {
            call.reject("'id' is required")
            return
        }
        if let conn = connections[id] {
            conn.stop()
            connections.removeValue(forKey: id)
            call.resolve()
        } else {
            call.reject("No such connection: \(id)")
        }
    }

    @objc public func disconnectAll(_ call: CAPPluginCall) {
        connections.values.forEach { $0.stop() }
        connections.removeAll()
        call.resolve()
    }

    // MARK: Android-parity helpers (no-ops on iOS)

    @objc public func bindToWifi(_ call: CAPPluginCall) {
        // iOS has no public API to bind process to Wi-Fi; resolve for parity.
        call.resolve()
    }

    @objc public func unbindNetwork(_ call: CAPPluginCall) {
        call.resolve()
    }

    @objc public func getWifiInfo(_ call: CAPPluginCall) {
        // Restricted on iOS; return empty fields for cross-platform TS shape.
        call.resolve([
            "ip": "", "gateway": "", "netmask": "",
            "dns1": "", "dns2": "", "server": ""
        ])
    }
}

// MARK: - Small helper
private extension Data {
    init?(hexString: String) {
        let clean = hexString.replacingOccurrences(of: "[^0-9A-Fa-f]", with: "", options: .regularExpression)
        let len = clean.count
        guard len % 2 == 0 else { return nil }
        var data = Data(capacity: len / 2)
        var idx = clean.startIndex
        while idx < clean.endIndex {
            let next = clean.index(idx, offsetBy: 2)
            let byteStr = clean[idx..<next]
            guard let byte = UInt8(byteStr, radix: 16) else { return nil }
            data.append(byte)
            idx = next
        }
        self = data
    }
}