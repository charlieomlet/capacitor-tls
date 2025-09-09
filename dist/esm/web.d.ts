import { WebPlugin } from '@capacitor/core';
import type { CapacitorTlsPlugin, ConnectOptions, DisconnectOptions, SendOptions } from './definitions';
export declare class CapacitorTlsWeb extends WebPlugin implements CapacitorTlsPlugin {
    connect(_options: ConnectOptions): Promise<{
        id: string;
    }>;
    send(_options: SendOptions): Promise<void>;
    disconnect(_options: DisconnectOptions): Promise<void>;
    disconnectAll(): Promise<void>;
    bindToWifi(): Promise<void>;
    unbindNetwork(): Promise<void>;
}
