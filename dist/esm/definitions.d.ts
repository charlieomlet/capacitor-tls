export declare type Encoding = 'base64' | 'utf8' | 'hex';
export interface TlsPermissions {
    nearbyWifi: 'granted' | 'denied' | 'prompt';
}
export interface ConnectOptions {
    host: string;
    port: number;
    insecure?: boolean;
    sni?: string;
    alpnProtocols?: string[];
    timeout?: number;
    id?: string;
}
export interface SendOptions {
    id: string;
    data: string;
    encoding?: Encoding;
}
export interface DisconnectOptions {
    id: string;
}
export interface ConnectResult {
    id: string;
}
export interface DataEvent {
    id: string;
    data: string;
    encoding: 'base64';
}
export interface ConnectEvent {
    id: string;
}
export interface CloseEvent {
    id: string;
    error?: string;
}
export interface ErrorEvent {
    id?: string;
    error: string;
}
export interface CapacitorTlsPlugin {
    checkPermissions(): Promise<TlsPermissions>;
    requestPermissions(): Promise<TlsPermissions>;
    connect(options: ConnectOptions): Promise<ConnectResult>;
    send(options: SendOptions): Promise<void>;
    disconnect(options: DisconnectOptions): Promise<void>;
    disconnectAll(): Promise<void>;
    bindToWifi(): Promise<void>;
    unbindNetwork(): Promise<void>;
    addListener(eventName: 'connect', listenerFunc: (ev: ConnectEvent) => void): Promise<any>;
    addListener(eventName: 'data', listenerFunc: (ev: DataEvent) => void): Promise<any>;
    addListener(eventName: 'close', listenerFunc: (ev: CloseEvent) => void): Promise<any>;
    addListener(eventName: 'error', listenerFunc: (ev: ErrorEvent) => void): Promise<any>;
    removeAllListeners(): Promise<void>;
}
