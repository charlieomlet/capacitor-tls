import { WebPlugin } from '@capacitor/core';

import type { CapacitorTlsPlugin, ConnectOptions, DisconnectOptions, SendOptions } from './definitions';

export class CapacitorTlsWeb extends WebPlugin implements CapacitorTlsPlugin {
  async checkPermissions(): Promise<{ nearbyWifi: 'granted' | 'denied' | 'prompt' }> {
    throw this.unimplemented('Not implemented on Web');
  }

  async requestPermissions(): Promise<{ nearbyWifi: 'granted' | 'denied' | 'prompt' }> {
    throw this.unimplemented('Not implemented on Web');
  }

  async connect(_options: ConnectOptions): Promise<{ id: string }> {
    throw this.unimplemented('TLS sockets are not available on the web.');
  }

  async send(_options: SendOptions): Promise<void> {
    throw this.unimplemented('TLS sockets are not available on the web.');
  }

  async disconnect(_options: DisconnectOptions): Promise<void> {
    throw this.unimplemented('TLS sockets are not available on the web.');
  }

  async disconnectAll(): Promise<void> {
    throw this.unimplemented('TLS sockets are not available on the web.');
  }

  async bindToWifi(): Promise<void> {
    throw this.unimplemented('Not implemented on Web');
  }
  
  async unbindNetwork(): Promise<void> {
    throw this.unimplemented('Not implemented on Web');
  }
}