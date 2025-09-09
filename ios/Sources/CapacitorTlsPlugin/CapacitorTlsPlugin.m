#import <Capacitor/Capacitor.h>

CAP_PLUGIN(CapacitorTlsPlugin, "CapacitorTls",
  CAP_PLUGIN_METHOD(connect, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(send, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(disconnect, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(disconnectAll, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(bindToWifi, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(unbindNetwork, CAPPluginReturnPromise);
  CAP_PLUGIN_METHOD(getWifiInfo, CAPPluginReturnPromise);
)