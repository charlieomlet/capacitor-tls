# capacitor-tls

TLS functionality for Capacitor JS

## Install

```bash
npm install capacitor-tls
npx cap sync
```

## API

<docgen-index>

* [`connect(...)`](#connect)
* [`send(...)`](#send)
* [`disconnect(...)`](#disconnect)
* [`disconnectAll()`](#disconnectall)
* [`bindToWifi()`](#bindtowifi)
* [`unbindNetwork()`](#unbindnetwork)
* [`addListener('connect', ...)`](#addlistenerconnect-)
* [`addListener('data', ...)`](#addlistenerdata-)
* [`addListener('close', ...)`](#addlistenerclose-)
* [`addListener('error', ...)`](#addlistenererror-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### connect(...)

```typescript
connect(options: ConnectOptions) => Promise<ConnectResult>
```

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#connectoptions">ConnectOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#connectresult">ConnectResult</a>&gt;</code>

--------------------


### send(...)

```typescript
send(options: SendOptions) => Promise<void>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#sendoptions">SendOptions</a></code> |

--------------------


### disconnect(...)

```typescript
disconnect(options: DisconnectOptions) => Promise<void>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#disconnectoptions">DisconnectOptions</a></code> |

--------------------


### disconnectAll()

```typescript
disconnectAll() => Promise<void>
```

--------------------


### bindToWifi()

```typescript
bindToWifi() => Promise<void>
```

--------------------


### unbindNetwork()

```typescript
unbindNetwork() => Promise<void>
```

--------------------


### addListener('connect', ...)

```typescript
addListener(eventName: 'connect', listenerFunc: (ev: ConnectEvent) => void) => Promise<any>
```

| Param              | Type                                                                   |
| ------------------ | ---------------------------------------------------------------------- |
| **`eventName`**    | <code>'connect'</code>                                                 |
| **`listenerFunc`** | <code>(ev: <a href="#connectevent">ConnectEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### addListener('data', ...)

```typescript
addListener(eventName: 'data', listenerFunc: (ev: DataEvent) => void) => Promise<any>
```

| Param              | Type                                                             |
| ------------------ | ---------------------------------------------------------------- |
| **`eventName`**    | <code>'data'</code>                                              |
| **`listenerFunc`** | <code>(ev: <a href="#dataevent">DataEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### addListener('close', ...)

```typescript
addListener(eventName: 'close', listenerFunc: (ev: CloseEvent) => void) => Promise<any>
```

| Param              | Type                                                               |
| ------------------ | ------------------------------------------------------------------ |
| **`eventName`**    | <code>'close'</code>                                               |
| **`listenerFunc`** | <code>(ev: <a href="#closeevent">CloseEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### addListener('error', ...)

```typescript
addListener(eventName: 'error', listenerFunc: (ev: ErrorEvent) => void) => Promise<any>
```

| Param              | Type                                                               |
| ------------------ | ------------------------------------------------------------------ |
| **`eventName`**    | <code>'error'</code>                                               |
| **`listenerFunc`** | <code>(ev: <a href="#errorevent">ErrorEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### ConnectResult

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### ConnectOptions

| Prop                | Type                  |
| ------------------- | --------------------- |
| **`host`**          | <code>string</code>   |
| **`port`**          | <code>number</code>   |
| **`insecure`**      | <code>boolean</code>  |
| **`sni`**           | <code>string</code>   |
| **`alpnProtocols`** | <code>string[]</code> |
| **`timeout`**       | <code>number</code>   |
| **`id`**            | <code>string</code>   |


#### SendOptions

| Prop           | Type                                          |
| -------------- | --------------------------------------------- |
| **`id`**       | <code>string</code>                           |
| **`data`**     | <code>string</code>                           |
| **`encoding`** | <code><a href="#encoding">Encoding</a></code> |


#### DisconnectOptions

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### ConnectEvent

| Prop     | Type                |
| -------- | ------------------- |
| **`id`** | <code>string</code> |


#### DataEvent

| Prop           | Type                  |
| -------------- | --------------------- |
| **`id`**       | <code>string</code>   |
| **`data`**     | <code>string</code>   |
| **`encoding`** | <code>'base64'</code> |


#### CloseEvent

| Prop        | Type                |
| ----------- | ------------------- |
| **`id`**    | <code>string</code> |
| **`error`** | <code>string</code> |


#### ErrorEvent

| Prop        | Type                |
| ----------- | ------------------- |
| **`id`**    | <code>string</code> |
| **`error`** | <code>string</code> |


### Type Aliases


#### Encoding

<code>'base64' | 'utf8' | 'hex'</code>

</docgen-api>
