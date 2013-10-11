kasahorow Keyboard For Android
==============================

kasahorow keyboard for Android. See http://kasahorow.com for more info. 
This project is heavily based on [AnySoftKeyboard](https://github.com/AnySoftKeyboard/AnySoftKeyboard)

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
|-- AnySoftKeyboard-API
|-- kasahorow-Android-Keyboard
`-- README.md

```

Update the main project and it dependencies. Issue the command below in `kasahorow-Android-Keyboard` as well as in `AnySoftKeyboard-API` and finally in `AnySoftKeyboard`

`android update project -p .`

**Note:** Make sure you've the `android` tool in your `PATH` variable. Makes life easier.

Using ant, in `kasahorow-Android-Keyboard` issue

`ant debug`

If you're an eclipse user, import all the projects into eclipse. Then build `kasahorow-Android-Keyboard` project not any of the library projects. Both `AnySoftKeyboard-API` and `AnySoftKeyboard` are library projects.

`AnySoftKeyboard` has been modified to a library project and support for custom fonts was added. If you planned to merge changes from the AnySoftKeyboard folks. Take note, there might be merge conflicts because of the changes. Use tools like `meld` or `git mergetool` to resolve the conflicts. It usually minor conflicts so shouldn't be hard to fix.
