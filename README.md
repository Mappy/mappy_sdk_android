# Mappy SDK

Mappy SDK is a library that provides APIs that could be shown on the map.

## Table of Contents  

- [Versions](#versions)
- [Technical considerations](#technical-considerations)
- [Getting started](#getting-started)
- [Advanced](#advanced)


## Versions

See [CHANGELOG](CHANGELOG)

## Technical considerations

 - Mappy SDK is compatible with API 21+
 - Mappy SDK requires the following permissions : `android.permission.INTERNET`, `android.permission.ACCESS_NETWORK_STATE`

## Getting started

- ### Accessing the Mappy repository

To access Mappy Repository add the following configuration in your project root `build.gradle` file.
```groovy
buildscript {
    repositories {
        maven {
            credentials {
                username '${MAPPY_USER_NAME}'
                password '${MAPPY_PASSWORD}'
            }
            url "http://sdkandroid.mappy.net/"
        }
    }
}
```
`MAPPY_USER_NAME`, `MAPPY_PASSWORD` should be provided by Mappy.
If you don't have this information, please contact the support.

- ### Import the library

```groovy
implementation "com.mappy:mappy-sdk:$mappy_version"
```


- ### Identify to Mappy

Mappy SDK requires your app to identify itself on our servers. In `AndroidManifest.xml`, add your approved `client-id` (contact Mappy to get one) inside the `<application>` tag :

```xml
<meta-data
    android:name="com.mappy.client_id"
    android:value="@string/YOUR_APPLICATION_CLIENT_ID" />
```

## Advanced

If you want the detail of all available services in the Mappy SDK, you will find it in the [Readme](../samples/mappy_sdk_sample/README.md)
