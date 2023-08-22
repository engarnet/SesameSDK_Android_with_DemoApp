# SesameSDK_Android

use the following sdk.
https://github.com/CANDY-HOUSE/SesameSDK_Android_with_DemoApp/tree/master/SesameSDK_Android

## how to use

add repository

```
allprojects {
    repositories {
        maven {
            url = uri("https://engarnet.github.io/SesameSDK_Android_with_DemoApp/")
        }
    }
}
```

add dependency

```
dependencies {
    implementation 'co.candyhouse.jp:sesame:2.0.7@aar'
}
```