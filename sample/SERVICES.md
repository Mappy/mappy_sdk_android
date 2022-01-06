# Table of Contents  

* [What's the sample for ?](#whats-the-sample-for-)
* [Foreword](#Foreword)  
* [MappyDownloadManager](#mappydownloadmanager)  

# What's the sample for ?

You can find here all the available API on the service SDK.


# Foreword

If you didn't read the README yet, please read it [here](README.md)

# MappyRouteRequestBuilder

This class provides a simple use of multipath request.

For example if you want to run a request with car between two city you will use 

```java
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

Three modes are available : CAR, BIKE and PEDESTRIAN, and each mode can be called by this function

    - requestCarRoute : run a multipath request with car
    - requestPedestrianRoute : run a multipath request with pedestrian
    - requestBikeRoute : run a multipath request with bike


Furthermore you must add departure and arrival to run the request by using the method

    - **departure(departure, isMyLocation)** : the departure in LatLng , and precise if this point is the current Location of user, by default isMyLocation is false
    - **arrival(arrival)** : the arrival in LatLng

More options are available :

    - **step**: if you want a third city between the other to create a step point in itinerary
    - **withTraffic** : ask to return the itinerary in polyline
    - **date** : the date when you want the itinerary will start


# MappyDownloadManager

## getSuggestions

**Autocompletion Service** 

Get a list of suggestion by query and the bounding box

**Params** : 

       - *query*       String get suggestion from this query
       - *filter*      filter to apply to the suggest request
       - *boundingbox* encoded geobounds, the bounding box of the research by default in FRANCE, use `GeoBounds` Util to get the string
       - *fromMicro*   tell the request is from microphone research
       - *maxResults*  change maximum suggest request responses (default is {@value #DEFAULT_MAX_RESULTS})

**Return** : a `SuggestionStore` contains a list of `Suggestion`
 
## GetLocationByQuery 

**Geocode Service**

Get the list of POI by query ( bounding box, text, suggestion, etc. )

**Params** : 

      - *searchedText*          `String` a text present
      - *geoBounds*             `GeoBounds` bounding box corresponding to user map lat_min,long_min,lat_max,long_max (SW, NE)
      - *extendsBoundingBox*    `Boolean` search outside bounding box if true (default), do not search outside bounding box if false
      - *isForRoute*            `Boolean` is the answer is for multi path or not
      - *filter*                `GetLocationByQueryRequest.FilterType` filter the result set
         PLACES (default) -> results will be chosen between POIs and addresses
         ADDRESS -> results will be addresses only
         POI -> results will POIs only
         POI_XOR_ADDRESS -> results will be addresses if any, POIs otherwise
      - *suggestion*            `Suggestion` the suggestion linked to the research
      - *favoriteCountry*       `Int` the search is not limited to that country, but locations in this country will have preference

**Return**:  a `LocationStore` contains a list of `MappyLocation` and other information

## getLocationByCoordinates 

**Reverse Geocode**

Get the list of POI by coordinates

**Params** : 

      - *coordinates* `LatLng` the coordinates of the research

**Return**:  a `LocationStore` contains a list of `MappyLocation` and other information

##Contact us

If you have any question, please contact dt.mobile.android@mappy.com
