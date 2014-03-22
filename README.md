kasahorow Keyboard For Android
==============================

kasahorow keyboard for Android. See http://kasahorow.com for more info. 
This project is based on [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard)

![Kasahorow Keyboard on HTC One](https://lh5.googleusercontent.com/-TOyx-zjPMzQ/Udu4AwTKt4I/AAAAAAAAJP0/WT5v9CKiD0g/s640/framed_Screenshot_2013-07-09-15-22-30.png)

Build
====

Clone the repository

`git clone --recursive git://github.com/eyedol/kasahorow-Keyboard-For-Android.git`

The recursive option is there to clone the submodules as well.

After, you should have

```
.
|-- AnySoftKeyboard
|-- build.gradle
|-- dictionaries
|-- gradle
|-- gradlew
|-- gradlew.bat
|-- kasahorow-Android-Keyboard
|-- README.md
`-- settings.gradle

```

### Debug Build

To make a debug  build, in the project's root directory, issue

`./gradlew assembleDebug`

This should build an unsigned apk for testing and debugging purposes.

### Release Build

To make a release build for distribution at the app store, in the project's root directory, issue

`./gradlew build`

This should build a signed and an aligned apk for distribution. This project has been configured so the private key and credentials are read from `gradle.properties` file. For a successful release build make sure you've the properties below defined in your `gradle.properties` file and your private key is in the root directory of the project. 

**gradle.properties**

```
storePassword="store_password"
keyAlias="key_alias"
keyPassword="key_password"
supportEmailAddress="support@emailaddress.com"
```

`AnySoftKeyboard` has been modified to a library project and support for custom fonts was added. If you plan to merge changes from the AnySoftKeyboard folks, take note, there might be merge conflicts because of the changes. Use tools like `meld` or `git mergetool` to resolve the conflicts. They're usually minor conflicts so shouldn't be hard to fix.
