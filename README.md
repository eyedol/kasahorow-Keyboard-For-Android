kasahorow Keyboard For Android
==============================

kasahorow keyboard for Android. See http://kasahorow.com for more info. 
This project is based on [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard)

![Kasahorow Keyboard on HTC One](https://lh5.googleusercontent.com/-TOyx-zjPMzQ/Udu4AwTKt4I/AAAAAAAAJP0/WT5v9CKiD0g/s640/framed_Screenshot_2013-07-09-15-22-30.png)

Build
====

Clone the repository

`git clone git://github.com/eyedol/kasahorow-Keyboard-For-Android.git`


After, you should have

```
.
|-- AndroidManifest.xml
|-- build
|-- build.gradle
|-- buildSrc
|-- dictionaries
|-- gradle
|-- gradlew
|-- gradlew.bat
|-- install_apk.sh
|-- keyboard_keystore
|-- proguard-rules.txt
|-- README.md
|-- res
|-- shippable.yml
|-- src
|-- StoreStuff
`-- temp_jni


```

### Debug Build

To make a debug  build, in the project's root directory, issue

`./gradlew assembleDebug`

This should build an unsigned apk for testing and debugging purposes.

### Release Build

To make a release build for distribution at the app store, in the project's root directory, issue

`./gradlew build`

This should build a signed and an aligned apk for distribution. This project has been configured so the private key and credentials are read from `gradle.properties` file. For a successful release build make sure you've the properties below defined in your `gradle.properties` file. Place your `gradle.properties` file in the root directory of your project. Also ensure that your private key is placed in the `kasahorow-Android-Keyboard` directory. Your private key should be explicitly named `release.keystore`. 

**gradle.properties**
```
storePassword="store_password"
keyAlias="key_alias"
keyPassword="key_password"
supportEmailAddress="support@emailaddress.com"
```

