# Table of Contents  

* [What's the sample for ?](#whats-the-sample-for-)
* [Foreword](#foreword)  
* [Mappy Android sdk](#mappy-android-sdk)  
* [Get Native library symbol files](#get-native-library-symbol-files) 
* [Advanced](#advanced) 

# What's the sample for ?

This sample shows how to display and interact with the map view i.e the `MappyMapFragment`.
Also included : the services. 
It shows how you can deal with the map and the services which is used to request some data to display them on the map


# Foreword

If you have not read the main readme, please read it [here](../../core_libs/mappy-sdk/README.md)


## Main features


Mappy SDK provides access to Mappy Map and geographic Web services. These include :

 - map with various layers (photos, transports, real-time traffic) and _in-map_ points of interest (POI),
 - geocoding, from address to coordinates and reverse geocoding,
 - itinerary computing for car, bike, public transports and pedestrian,
 - lexical search of addresses and points of interest
 - panoramic views (360°) of shopping streets and inside shops


## Technical considerations

 - Mappy SDK is compatible with api 21+
 - Mappy SDK is based on [mapbox-gl-native](https://github.com/mapbox/mapbox-gl-native) to manage map interactions and display


## Include mappysdk in your project

Use a Maven repository and add `implementation 'com.mappy:mappysdk:6.+'` to your `build.gradle` (See [Access Mappy Maven Repository](../../core_libs/mappy-sdk/README.md#access-mappy-maven-repository)).

Add in your build.gradle the credentials like in sample 

Mappy SDK requires to identify your app on our servers. In `AndroidManifest.xml`, add your approved `client-id` (contact Mappy to get one) inside the `<application>` tag :

```xml
<meta-data
    android:name="com.mappy.client_id"
    android:value="@string/YOUR_APPLICATION_CLIENT_ID" />
```

> **Permissions**
> mappysdk already uses the following permissions :
>
>
```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.WAKE_LOCK"/>
```


# Mappy Android sdk

## Display the map

Initialize the SDK

```java
 MappySDK.initialize(applicationContext)
```

Look at `HelloMapSample.kt` SDK sample : it displays an interactive map, centered on Paris.
The map view object is declared in `sample_hello_map.xml` layout, like this :

```xml
    <fragment
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/sample_hello_map_mapFragment"
        class="com.mappy.map.MappyMapFragment"
        android:name="com.mappy.map.MappyMapFragment"/>
```

## Location

### Default Provider


You can use the default provider provided by the sdk or create your own provider.

The default provider `MappyLocationManager` uses the [Fused Google API](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient)

You must init the sdk by default in your project, this must be call before using the map 

```kotlin
 MappyLocationServices.initExternalLocationServices()
```


### User Location


The sdk provides tools to manage the location `LocationUtil`
In this tools you can :

    - Resume / Pause the location
    - ask settings to start to localize the user, (notification and dialog system of permission)
    - etc.
    
Look at `UserLocationSample`


#### How to start location update 


```kotlin
 LocationUtil.resumeLocation(this)
```


#### How to stop location update 


```kotlin
 LocationUtil.resumeLocation(this)
```


## Manipulate the map

To manipulate the MapView, call `MappyMapFragment.getMapControllerAsync` allowing to :

 - set map position (with precise coordinates or an area),
 - set map view style (standard, transit, trafic, photo-satellite)
 - set copyright position
 ....

```java
    MappyMapFragment mapFragment = (MappyMapFragment) getSupportFragmentManager().findFragmentById(R.id.sample_map_style_mapFragment);
         mapFragment.getMapControllerAsync(new MapControllerCallback() {
             @Override
             public void onMapControllerReady(MapController mapController) {
                 mMapController = mapController;
                 mMapController.setStyle(MapStyle.STANDARD);
                 manageWording();
             }
         });
```

## Add elements to the map

The SDK provides different types of element to display on the MapView : markers, polylines, polygons and popups.

see MarkerGLSample, MarkerViewSampe to display marker and popup
see PolylineAndPolygonSample yo display polylines and polygones

## Call Mappy Web services

The `MappyDownloadManager` provides access to all Mappy Web services (except the map).
It is based on the [Rx](https://github.com/ReactiveX/RxJava/blob/3.x/README.md) library and handles resources caching, using both memory and local storage.

### GeocodeSample :

```java
    mPendingRequestListener = new MappyPendingRequestListener<LocationStore>(getApplicationContext()) {
      
        @Override
        public void onRequestSuccess(LocationStore locationStore) {
            if (locationStore.getMappyLocations().isEmpty()) {
                progressDialogHelper.dismiss();
                Toast.makeText(GeocodeSample.this, "Votre saisie ne correspond pas à une adresse connue", Toast.LENGTH_SHORT).show();
                return;
            }
            setResult(locationStore.getMappyLocations().get(0));
        }

        @Override
        public void onRequestFailure(Throwable throwable) {
            progressDialogHelper.dismiss();
        }
    };
    GetLocationByQueryRequest.LocationParams locationParams = new GetLocationByQueryRequest.LocationParams(queriedAddress, mMapController.getBoundingBox(), true, false, GetLocationByQueryRequest.Filter.ADDRESS);
    MappyDownloadManager.getLocationsByQuery(context, locationParams, mPendingRequestListener);
```


### MappyRouteRequestBuilder :

To help you deal with the mappy route API, an utilitarian class is available. `MappyRouteRequestBuilder` is a factory requesting all the services for itinerary using only one mode : car, bike or pedestrian.

```kotlin
    MappyRouteRequestBuilder.withContext(context)
        .departure(departure, false)
        .arrival(arrival)
        .requestCarRoute(new RequestListener<List<MappyMultiPathRoute>>() {
            @Override
            public void onRequestFailure(Throwable throwable) {
            }

            @Override
            public void onRequestSuccess(List<MappyMultiPathRoute> mappyMultiPathRoutes) {
                // mappyMultiPathRoutes are all available routes for the requested mode (here : car)
            }
        });
```

See `MappyRouteRequestBuilder` structure to see all possibility and `SimpleRouteWithLatLngSample` for related Sample.


### More Services

If you want the detail of all services available in the Mappy SDK, you will find it in the [Services](SERVICES.md)


# Get Native library symbol files
----------
Use your access to Maven repository (See [Access Mappy Maven Repository](../../core_libs/mappy-sdk/README.md#access-mappy-maven-repository)) to download the symbol files:

```
    http://sdkandroid.mappy.net/symbols/{version}/armeabi-v7a.zip
    http://sdkandroid.mappy.net/symbols/{version}/arm64-v8a.zip
    http://sdkandroid.mappy.net/symbols/{version}/x86.zip
    http://sdkandroid.mappy.net/symbols/{version}/x86_64.zip
```

with `{version}` like `6.1930.0`.

## Advanced

If you want the detail of all available functionality in SDK MAP, you will find it in the [Advanced](MAP.md)
