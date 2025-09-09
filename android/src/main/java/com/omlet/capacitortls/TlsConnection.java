package com.omlet.capacitortls;

import android.os.Build;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.X509TrustManager;

public class TlsConnection {
    interface ReadyListener { void onReady(); }
    interface DataListener { void onData(byte[] data); }
    interface CloseListener { void onClose(String error); }
    interface ErrorListener { void onError(String message); }

    private final String id;
    private final String host;
    private final int port;
    private final boolean insecure;
    private final String sni;
    private final List<String> alpn;
    private final int timeoutMs;
    private final ReadyListener onReady;
    private final DataListener onData;
    private final CloseListener onClose;
    private final ErrorListener onError;

    private volatile SSLSocket socket;
    private volatile InputStream input;
    private volatile OutputStream output;
    private final ExecutorService exec = Executors.newCachedThreadPool();

    TlsConnection(
            String id,
            String host,
            int port,
            boolean insecure,
            String sni,
            List<String> alpn,
            int timeoutMs,
            ReadyListener onReady,
            DataListener onData,
            CloseListener onClose,
            ErrorListener onError
    ) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.insecure = insecure;
        this.sni = sni;
        this.alpn = alpn;
        this.timeoutMs = timeoutMs;
        this.onReady = onReady;
        this.onData = onData;
        this.onClose = onClose;
        this.onError = onError;
    }

    void start() {
        exec.execute(() -> {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                if (insecure) {
                    ctx.init(null, new javax.net.ssl.TrustManager[]{trustAll()}, new SecureRandom());
                } else {
                    ctx.init(null, null, null);
                }

                SSLSocket raw = (SSLSocket) ctx.getSocketFactory().createSocket();
                raw.connect(new InetSocketAddress(host, port), timeoutMs);

                SSLParameters params = new SSLParameters();
                if (!insecure) {
                    // Enable hostname verification
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        params.setEndpointIdentificationAlgorithm("HTTPS");
                    }
                }
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        params.setServerNames(Collections.singletonList(new SNIHostName(sni)));
                    }
                } catch (Throwable ignored) {}

                // ALPN best-effort via reflection (API >= 29 generally supported)
                try {
                    Method m = SSLParameters.class.getMethod("setApplicationProtocols", String[].class);
                    m.invoke(params, (Object) alpn.toArray(new String[0]));
                } catch (Throwable ignored) {}

                raw.setSSLParameters(params);
                raw.startHandshake();

                socket = raw;
                input = raw.getInputStream();
                output = raw.getOutputStream();

                onReady.onReady();
                startReader();
            } catch (Exception e) {
                onError.onError("TLS connect error: " + e.getMessage());
                onClose.onClose(e.getMessage());
                close();
            }
        });
    }

    private void startReader() {
        exec.execute(() -> {
            byte[] buf = new byte[64 * 1024];
            try {
                while (true) {
                    InputStream in = input;
                    if (in == null) break;
                    int n = in.read(buf);
                    if (n == -1) break;
                    if (n > 0) {
                        byte[] chunk = new byte[n];
                        System.arraycopy(buf, 0, chunk, 0, n);
                        onData.onData(chunk);
                    }
                }
            } catch (Exception e) {
                onError.onError("Read error: " + e.getMessage());
            } finally {
                onClose.onClose(null);
                close();
            }
        });
    }

    void send(byte[] bytes) {
        exec.execute(() -> {
            try {
                OutputStream out = output;
                if (out != null) {
                    out.write(bytes);
                    out.flush();
                }
            } catch (Exception e) {
                onError.onError("Write error: " + e.getMessage());
                onClose.onClose(e.getMessage());
                close();
            }
        });
    }

    void close() {
        try { if (input != null) input.close(); } catch (Exception ignored) {}
        try { if (output != null) output.close(); } catch (Exception ignored) {}
        try { if (socket != null) socket.close(); } catch (Exception ignored) {}
        input = null;
        output = null;
        socket = null;
    }

    private X509TrustManager trustAll() {
        return new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        };
    }

}
