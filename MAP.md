# Table of Contents  

- [Versions](#versions)
* [What's the sample for ?](#whats-the-sample-for-)
* [Foreword](#foreword)  
* [Mappy Android sdk](#mappy-android-sdk)  
* [Get Native library symbol files](#get-native-library-symbol-files) 
* [Advanced](#advanced) 

#Versions

See [CHANGELOG](CHANGELOG)

# What's the sample for ?

This sample shows how to display and interact with the map view i.e the `MappyMapFragment`.
Also included : the services. 
It shows how you can deal with the map and the services which is used to request some data to display them on the map


# Foreword

If you have not read the main readme, please read it [here](../README.md)


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

Use a Maven repository and add `implementation 'com.mappy:mappysdk:6.+'` to your `build.gradle` (See [Access Mappy Maven Repository](../README.md#access-mappy-maven-repository)).

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


## Manipulate the map

To manipulate the MapView, call `MappyMapFragment.getMapControllerAsync` allowing to :

 - set map position (with precise coordinates or an area),
 - set map visual options (Theme and Layer)
 - set copyright position
 ....

```java
    MappyMapFragment mapFragment = (MappyMapFragment) getSupportFragmentManager().findFragmentById(R.id.sample_map_style_mapFragment);
         mapFragment.getMapControllerAsync(new MapControllerCallback() {
             @Override
             public void onMapControllerReady(MapController mapController) {
                 mMapController = mapController;
                 mMapController.setVisualOptions(Theme.DEFAULT, Layer.NEUTRAL);
                 manageWording();
             }
         });
```

## Add elements to the map

The SDK provides different types of element to display on the MapView : markers, polylines, polygons and popups.

see MarkerGLSample, MarkerViewSampe to display marker and popup
see PolylineAndPolygonSample yo display polylines and polygones

### You can add a Polygon with optional hole to the Map

First, you have to construct the MappyPolygon object :
```kotlin
val mappyPolygon = MappyPolygon()
mappyPolygon.fillColor(fillColor) // int that represents a simple color. ex : #00846B
mappyPolygon.strokeColor(strokeColor) // int that represents  a simple color. ex : #ffffffff
mappyPolygon.alpha(POLYGON_ALPHA) // float that represents an alpha to use to draw a polygon. ex : 0.5f
mappyPolygon.add(example1Polygon.toTypedArray()) // examplePolygon is a List<LatLng>
mappyPolygon.addHole(example1Hole1) // exampleHole is a List<LatLng>
```

Then, you should call the MapController to pass the constructed MappyPolygon
```kotlin
mapController.clearPolygons()
mapController.addPolygon(mappyPolygon)
mapController.displayWholePolygons()
```

### You can add a Polygon with optional hole to the Map using a GeoJson file
First you should prepare your GeoJson file.
The structure of the GeoJson file should be as described bellow

````
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "properties": {
        
      },
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          [
            [
              2.370471954345703,
              48.900668635982115
            ],
            [
              2.370471954345703,
              48.89666249816737
            ],
            [
              2.3768234252929688,
              48.89073733961978
            ],
            [
              2.3888397216796875,
              48.90083790234088
            ],
            [
              2.370471954345703,
              48.900668635982115
            ]
          ]
        ]
      }
    }
  ]
}
````  

To add holes to the polygon, you should integrate the holes coordinates as well to the coordinates array and respect a LinearRing rule: start with the outline polygon coordinates, then the hole coordinates and the last coordinates should always be the first LatLng of the outline polygon.
Here is an example:

````
{
  "type": "Feature",
  "properties": {
    "name": "Home Zone"
  },
  "geometry": {
    "type": "Polygon",
    "coordinates": [
      [
        //beginning of the Outline Polygone
        [
          2.300777435302734,
          48.87205438637627
        ],
         .....
         .....
        [
          2.2982025146484375,
          48.87826399706969
        ],
        [
          2.300777435302734,
          48.87205438637627
        ],
        // End of the outline Polygone 
        // Beginning of the hole coordinates
        [
          2.3234367370605464,
          48.86623923394761
        ],
        [
          2.3218917846679688,
          48.86398074013291
        ],
        [
          2.3301315307617188,
          48.86121394630214
        ],
        [
          2.3313331604003906,
          48.863472564977805
        ],
        [
          2.3234367370605464,
          48.86623923394761
        ],
         // End of the hole coordinates
        [
          2.300777435302734,
          48.87205438637627
        ],
         // Add this to mark the end, it's the first LatLng of the outline polygon
      ]
    ]
  }
}
````

You have the possibility to add many polygons as well, just add the coordinates as an array inside the 
coordinates object, and change the type to be `MultiPolygon`.

Then you should create a GeoJsonHolder.Builder

```kotlin
val builder = GeoJsonHolder.newBuilder(json) //json is the content of your GeoJson file
.stylePolygon(polygonStrokeColor, polygonFillColor, POLYGON_ALPHA)
```

Then, you should call the MapController to pass the constructed Polygon

```kotlin
mapController.addGeoJson(builder, object : MapController.GeoJsonAddedListener {  
    override fun geoJsonAddedSuccess(geoJsonHolder: GeoJsonHolder) {  
        mapController.displayWholeGeoJsons()
        //Add your custom logic here  
    }  
  
    override fun geoJsonAddedError(error: Throwable) {  
        //Handle errors 
    }  
})
```

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


# Get Native library symbol files
----------  
Use your access to Maven repository (See [Access Mappy Maven Repository](../README.md#access-mappy-maven-repository)) to download the symbol files:

```  
 http://sdkandroid.mappy.net/symbols/{version}/armeabi-v7a.zip http://sdkandroid.mappy.net/symbols/{version}/arm64-v8a.zip http://sdkandroid.mappy.net/symbols/{version}/x86.zip http://sdkandroid.mappy.net/symbols/{version}/x86_64.zip```  
  
with `{version}` like `6.1930.0`.  
  
## Advanced  
  
If you want the detail of all available functionality in SDK MAP, you will find it in the [Advanced](sample/README.md)