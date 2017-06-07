# Geeny Android SDK
![Status](https://img.shields.io/badge/status-alpha-orange.svg?style=flat)
![Kotlin version](https://img.shields.io/badge/kotlin-1.1.4-blue.svg?style=flat)
![License](https://img.shields.io/badge/license-MPLv2-orange.svg?style=flat)

The Geeny Android SDK is a collection of libraries that help iOS developers to develop apps that work with the Geeny platform. The SDK is accompanied by an example app which demonstrates its functionalities.

Currently, the Android SDK contains the Router SDK.

## Geeny Gateway SDK

On a high level, the Router SDK allows an Android app to become a relay between a "non-IP-enabled Thing" and the Geeny platform. It enables such a Thing to be registered with the Geeny Cloud and securely transfer data to and from the Geeny MQTT endpoint.

At this stage, we require the Things to be "Geeny-native".

### Beta notice

This SDK is still under development and is currently released as Beta. Although it has been tested,, bugs and issues may be present. Some code might require cleanup. In addition, until version 1.0 is released, we cannot guarantee that API calls will not break from one SDK version to the next. Be sure to consult the Change Log for any breaking changes / additions to the SDK.

### Glossary:

* **Thing**: A connected device that can send and receive data - a fitness tracker or a smart lamp.
* **Non-IP-enabled Thing**: A thing which does not connect directly to the Internet by design (for example, it does not have a WIFI module built in). These Things are usually low-powered.
* **Geeny-enabled Thing**: A Thing that is compliant with the Geeny Thing specs.
* **ThingInfo**: Metadata of a Thing. Represents all metadata of a Thing, including its identifier, the characteristics and Geeny Thing Info.
* **Virtual Thing**: a Thing that is fully managed by a developer and connected to the Geeny SDK manually - e.g. a HomeKit appliance or HealthKit Data

### Requirements

* Andrioid Studio Beta.
* An Android device running minSdkVersion 18 or higher. Tested on Sony Xperia Z5. Bluetooth is not supported in Simulator; thus, a real device is needed.
* A "Geeny-native" Bluetooth LE Thing for testing the automatic data publishing. For example, the [nRF52 DK](https://www.nordicsemi.com/eng/Products/Bluetooth-low-energy/nRF52-DK). See [More Information](#more-information) below.
* Alternatively, you can create a [virtual Thing](#adding-virtual-things). 

### More Information

First, make sure your "Geeny-native" Bluetooth LE Thing is on.
If you have a Nordic nRF52 DK (PCA10040), you can flash the [DevThing firmware](https://github.com/geeny/devthing-e0) to make it "Geeny-native”.

Also, please verify that the ThingType of your Thing and its resources are set up on [Geeny Labs](https://labs.geeny.io/things/docs/).

How to use the app:
1. Log in to your Geeny account by tapping on Login button.
1. After logging in, tap on Scan button in the top bar.
1. From the list, select a ”Geeny-native” Thing indicated by the Geeny logo.
1. The SDK will try to connect to the Thing. After successful connect Geeny relevant information will be displayed.
1. Tap on Register Thing.
1. After registration, the Gateway you will see registered resources in a list. 
1. To  start publishing data to the Geeny Cloud. Connect the different resources.
*Please note that only the [MessageTypes](https://labs.geeny.io/things/docs/#/MessageTypes) defined in the [resources](https://labs.geeny.io/things/docs/#/ThingTypes/get_thingTypes__thingTypeId__resources) of this specific ThingType will be accepted by the Geeny Cloud.*



### Running the Example app from command line

After checking out the project, you should be able to get it running on your Android after following these steps:

1. [Install](https://docs.gradle.org/current/userguide/installation.html) Gradle.
1. In Terminal, change to the local repository:  `$ cd <repoRoot>`.
1. Run the dependencies: `$ ./gradlew :demoApp:installDebug`.

### Running the Example app from Android Studio Beta

1. [Download](https://developer.android.com/studio/preview/index.html) Android Studio Beta and install it following the instructions.
1. Open Android Studio Beta and and click on Import Project. Select <repoRoot> and click through the next questions without changing anything.
1. After the import you will see a play button on the top toolbar. Next to it there is a selector field. Select demo-app and hit play.

To integrate Geeny Android SDK into your Android Studio project using Gradle, put the dependency under dependencies in your build.gradle:

    compile 'io.geeny.sdk:android:x.y.z'

### Set up

In your dependency tool or if you don't have on in your Application class add the GeenySdk as a dependency.
You need a configuration file first. 

KOTLIN
 ```kotlin
    
    val sdk: GeenySdk by lazy {
        val config = GeenyConfiguration.Builder().build()
        GeenySdk.create(config, context.applicationContext)
    }
 
 ```
 JAVA
 ```java
    GeenyConfiguration config = GeenyConfiguration.Builder().build()
    GeenySdk sdk = GeenySdk.create(config, context.applicationContext)
 ```
 

Then you can initialize the GeenySdk in a launching activity by calling:

KOTLIN
 ```kotlin
    sdk.init().subscribe(
        {Log.d(TAG, it.toString())},
        {Log.e(TAG, it.message, it)}
    )
 ```
 JAVA
 ```java
    sdk.init().subscribe(
        {sdkInitializationResult -> Log.d(TAG, it.toString())},
        {error -> Log.e(TAG, error.message, error)}
    )
 ```

### User Login

Before a user can send and receive data from the Geeny Cloud, they have to log in into their [Geeny account]((https://labs.geeny.io/login)).

Request the username and the password from the user and pass them on to the `Gateway.shared.login()` method:


KOTLIN & JAVA
 ```kotlin
    sdk.geeny.auth.signInWithCredentials(c)
                         .subscribeOn(ioScheduler)
                         .observeOn(mainScheduler)
                         .subscribe(
                                 { e.g.: openMain... },
                                 { e.g.: showError...}
                         )
 ```
You can always check if the user is logged in by looking at the `isLoggedIn` property:

KOTLIN & JAVA
```kotlin
        sdk.geeny.auth.isSignedIn()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe { booleanState ->
                    if (booleanState) {
                        e.g.: openMain....
                    } else {
                        e.g.: openSignIn...
                    }
                }
```

And of course logout:

```kotlin
        sdk.geeny.auth.signOut()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe { 
                        e.g.: openSignIn...  
                    
                }
```

### Adding Things

Once the user is logged in, they can connect their Things.
**Important note:** make sure you have your [ThingType](https://labs.geeny.io/things/docs/#/ThingTypes/post_thingTypes) and [MessageTypes](https://labs.geeny.io/things/docs/#/MessageTypes/post_messageTypes) registered on the Geeny API before you proceed with this step.

#### Adding Bluetooth LE Things

To speed up the integration process with Bluetooth LE connected devices, the Route SDK can fully take on the Bluetooth communication with the Thing. For this to work, the hardware provider must implement the Geeny BLE characteristic in the device firmware. This characteristic contains info such as the registered ThingType and the serial number of the device. The iOS GeenyGateway SDK will recognize this information, parse it and pass it on to the Cloud during the registration process.

**Scanning for Things**


```kotlin    
    sdk.clients.ble.availableDevices()
        .observeOn(mainScheduler)
            .subscribe{
                bleDevice -> e.g: update list
            }
            
    sdk.clients.ble.startScan()
    
    ... and then 
    
    sdk.clients.ble.stopScan()
    
```

**DeviceInfo**

Scanning results returns an array of `[BleClient]`, this object provides different information. Some of them
are directly accessible, other need a connection first or the information are streamed in an Observable as
they might change and/or not accessible until a successful connection. 

Before connection and registration:

```kotlin
    client.address() // the mac address of the bluetooth client 
    client.name() // the name of the ble device
    client.isGeenyDevice() // indeicates if it has a gatt service with the Geeny UUID
```

After connection:

```kotlin
    client.connection()             // returns an Observable of type ConnectionState to track the connection state of the device
    client.geenyInformation()       //returns an Observable of type DeviceInfo to track additional geeny related infos after connection
```

The information a DeviceInfo provides:

```kotlin
    DeviceInfo(
        deviceName,                  // (String) value representing the name of the device,
        address,                     // (String) value representing the ble mac address
        protocolVersion,             // (Int) value representing the protocol version this thing communicates
        serialNumber,                // (UUID) value representing the serial number
        thingTypeId                 // (UUID) value representing the thingtype registerd at Geeny
    )
```

**Registering Things**

Once you have a `DeviceInfo`, you can register the Thing on the Geeny Cloud and retrieve a `BleThing` (a combination of device and cloud data):

```kotlin 
    sdk.geeny.register(deviceInfo: DeviceInfo)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(doSomethingwith(bleThing))
```

At any later time you can get this information back with the serialNumber:

```kotlin 
    serialnumber = getSerialNumberFromYourPersistentToolOfYourChoice 
    sdk.geeny.getThing(serialNumber)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(doSomethingWith(bleThing))
```

**Routing**

With the `BleThing` you can get a list `GeenyFlow` that contains all the routes, that you registered for.


```kotlin 
    serialnumber = getSerialNumberFromYourPersistentSourceOfYourChoice 
    sdk.geeny.getThing(serialNumber)
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(doSomethingWith(geenyFlow))
```

The GeenyFlow consists of a list of `Route`. A `Route` can e.g. send values from a bluetooth notify characteristic to
the broker. And another route picking up that message and sending it to the Geeny MqttServer. To understand what a particular route is
doing you can ask for the RouteInfo.

```kotlin 
    route = geenyFlow.routes[0]
    routeInfo = route.info() 
    
    RouteInfo(
        type                    // (RouteType) can be BLE, MQTT, CUSTOM
        direction               // (Direction) can be PRODUCER or CONSUMER
        topic                   // (String) the id of the storage on the broker
        val clientIdentifier    // (String) the identifier of the client, e.g. in BleClient it will be the mac adress
        val clientResourceId    // (String) the identifier of the resource on the client, e.g. in BleClient it will be the charactersticId
    )
```

You can start and stop a route by:

```kotlin 
    route.start()
    route.stop()
```

This will initiate a connection attempt to Ble/Mqtt and once successful start producing/consuming messages. You can always
check the state of the route (e.g a BleClient that disonnects will stop the route):

```kotlin 
    route.isRunning()
    ...or in a streaming fashion
     
    route.running().subscribe{doSomethingWith(connectionState)}
```

**Example Usecase**

The SDK provides an example usecase for connecting a registered Geeny device. If you have a bluetooth mac address and the Android main
RXScheduler you can get a BleGateway by:


```kotlin 
    gateway = getBleGateway(bluetoothAdress, mainScheduler)
```

The gateway provides a callback interface:

KOTLIN

```kotlin 
    interface Callback {
        fun onClientLoaded(client: BleClient)
        fun onConnectionStateHasChanged(connectionState: ConnectionState)
        fun onDeviceInfoLoad(deviceInfo: DeviceInfo)
        fun onDeviceIsNotRegisteredYet()
        fun onFlowsLoaded(flows: List<GeenyFlow>)
        fun onRouteConnectionStatusHasChanged(flow: GeenyFlow, route: Route, status: ConnectionState)
        fun progress(show: Boolean)
        fun onError(throwable: Throwable)
        fun onValueHasChanged(flow: GeenyFlow, bytes: ByteArray)
    }
```

JAVA

```kotlin 
    interface Callback {
        void onClientLoaded(BleClient client)
        void onConnectionStateHasChanged(ConnectionState connectionState)
        void onDeviceInfoLoad(DeviceInfo deviceInfo)
        void onDeviceIsNotRegisteredYet()
        void onFlowsLoaded(List<GeenyFlow> flows)
        void onRouteConnectionStatusHasChanged(GeenyFlow flow, Route route, ConnectionState status)
        void progress(boolean show)
        void onError(Throwable throwable)
        void onValueHasChanged(GeenyFlow flow, byte[] bytes)
    }
```

You can attach this callback by

KOTLIN 

```kotlin
    callback = object: BleGateway.Callback {...}
    gateway.attach(callback)
```
JAVA

```kotlin
    callback = new BleGateway.Callback() {...}
    gateway.attach(callback)
```

Don't forget to detach by calling:

```kotlin
    gateway.detach()
```

Then you can interact with the gateway by:


```kotlin
    gateway.connect(context)                // connects the ble device
    gateway.disconnect()                    // disconnects the ble device
    gateway.register()                      // register this device
    gateway.triggerRead(clientResourceId)   // triggers read on characteristic
    gateway.start(flow, context)            // once you have the flows you can start them by
```


#### Adding Virtual Things (to come) 

## Android API documentation (to come)

The complete API documentation of the Geeny Android SDK is available in: `<repoRoot>/GeenyGateway/Docs` (to come)

The documentation can also be generated locally using ... (to come).


## Not implemented yet - Coming soon

*  Background networking support and documentation
*  Automatic reconnection to registered Things
*  Complete unit test coverage of public methods
*  Repeated scan and characteristic discovery should cancel previous tasks
*  Offline handling when the Thing itself or the Geeny API are unreacheable

## License

Copyright (C) 2017 Telefónica Germany Next GmbH, Charlottenstrasse 4, 10969 Berlin.

This project is licensed under the terms of the [Mozilla Public License Version 2.0](LICENSE.md).
