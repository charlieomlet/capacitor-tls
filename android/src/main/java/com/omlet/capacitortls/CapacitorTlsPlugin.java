package com.omlet.capacitortls;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CapacitorPlugin(name = "CapacitorTls")
public class CapacitorTlsPlugin extends Plugin {
    private final Map<String, TlsConnection> connections = new ConcurrentHashMap<>();
    private ConnectivityManager.NetworkCallback wifiCallback;

    @PluginMethod
    public void bindToWifi(PluginCall call) {
        try {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);

            // If we're already bound, clean it up first
            if (wifiCallback != null) {
                try { cm.unregisterNetworkCallback(wifiCallback); } catch (Exception ignored) {}
                wifiCallback = null;
            }

            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    // Important: don’t require internet, APs often have none during provisioning
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();

            wifiCallback = new ConnectivityManager.NetworkCallback() {
                @Override public void onAvailable(Network network) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cm.bindProcessToNetwork(network);
                    } else {
                        ConnectivityManager.setProcessDefaultNetwork(network);
                    }
                    Log.d("CapacitorTlsPlugin", "Process bound to Wi-Fi: " + network);
                    // We can resolve as soon as bound
                    if (!call.isReleased()) call.resolve();
                }

                @Override public void onUnavailable() {
                    Log.w("CapacitorTlsPlugin", "No Wi-Fi network became available to bind");
                    if (!call.isReleased()) call.reject("WIFI_UNAVAILABLE");
                }
            };

            // Request a Wi-Fi network; system will call onAvailable if/when the AP is up
            cm.requestNetwork(request, wifiCallback);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void unbindNetwork(PluginCall call) {
        try {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.bindProcessToNetwork(null);
            } else {
                ConnectivityManager.setProcessDefaultNetwork(null);
            }
            if (wifiCallback != null) {
                try { cm.unregisterNetworkCallback(wifiCallback); } catch (Exception ignored) {}
                wifiCallback = null;
            }
            Log.d("CapacitorTlsPlugin", "Process unbound from Wi-Fi");
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void connect(PluginCall call) {
        String host = call.getString("host");
        Integer port = call.getInt("port");

        if (host == null || port == null) {
            call.reject("'host' and 'port' are required.");
            return;
        }

        Boolean insecure = call.getBoolean("insecure", false);
        String sni = call.getString("sni", host);
        Integer timeout = call.getInt("timeout", 10000);

        List<String> alpn = new ArrayList<>();
        JSONArray alpnJson = call.getArray("alpnProtocols");

        if (alpnJson != null) {
            for (int i = 0; i < alpnJson.length(); i++) {
                alpn.add(alpnJson.optString(i, ""));
            }
        }

        String id = call.getString("id", UUID.randomUUID().toString());

        if (connections.containsKey(id)) {
            call.reject("Connection ID is already in use.");
            return;
        }

        TlsConnection conn = new TlsConnection(
                id, host, port, insecure, sni, alpn, timeout,
                // onReady
                () -> {
                    JSObject obj = new JSObject().put("id", id);
                    notifyListeners("connect", obj);
                },
                // onData
                data -> {
                    JSObject obj = new JSObject();
                    obj.put("id", id);
                    obj.put("data", android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP));
                    obj.put("encoding", "base64");
                    notifyListeners("data", obj);
                },
                // onClose
                err -> {
                    JSObject obj = new JSObject();
                    obj.put("id", id);
                    if (err != null) obj.put("error", err);
                    notifyListeners("close", obj);
                    connections.remove(id);
                },
                // onError
                msg -> {
                    JSObject obj = new JSObject();
                    obj.put("id", id);
                    obj.put("error", msg);
                    notifyListeners("error", obj);
                }
        );

        connections.put(id, conn);
        conn.start();

        JSObject ret = new JSObject().put("id", id);
        call.resolve(ret);
    }

    @PluginMethod
    public void send(PluginCall call) {
        String id = call.getString("id");
        String dataStr = call.getString("data");
        String encoding = call.getString("encoding", "base64");
        if (encoding == null) encoding = "base64";

        if (id == null || dataStr == null) {
            call.reject("'id' and 'data' are required");
            return;
        }

        TlsConnection conn = connections.get(id);
        if (conn == null) {
            call.reject("No such connection: " + id);
            return;
        }

        try {

            byte[] bytes;
            switch (encoding) {
                case "utf8":
                    bytes = dataStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    break;
                case "hex":
                    bytes = hexToBytes(dataStr);
                    break;
                case "base64":
                default:
                    bytes = android.util.Base64.decode(dataStr, android.util.Base64.DEFAULT);
                    break;
            }
            conn.send(bytes);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        String id = call.getString("id");
        if (id == null) {
            call.reject("'id' is required");
            return;
        }
        TlsConnection conn = connections.get(id);
        if (conn != null) {
            conn.close();
        }
        connections.remove(id);
        call.resolve();
    }

    @PluginMethod
    public void disconnectAll(PluginCall call) {
        for (TlsConnection c : connections.values()) {
            c.close();
        }
        connections.clear();
        call.resolve();
    }

    private byte[] hexToBytes(String hex) {
        String clean = hex.replaceAll("[^0-9A-Fa-f]", "");
        int len = clean.length();
        if ((len & 1) == 1) {
            // odd length -> ignore last nibble
            clean = clean.substring(0, len - 1);
            len = clean.length();
        }
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) (Integer.parseInt(clean.substring(i, i + 2), 16) & 0xFF);
        }
        return out;
    }
}
