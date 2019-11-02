# ChromeMenu

[![Download](https://api.bintray.com/packages/reedreed/maven/chrome-menu/images/download.svg)](https://bintray.com/reedreed/maven/chrome-menu/_latestVersion)
[![API](https://img.shields.io/badge/API-17%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=17)

The menu from [Chromium Android appmenu](https://github.com/chromium/chromium/tree/master/chrome/android/java/src/org/chromium/chrome/browser/appmenu). It can do this:

![Example GIF](https://github.com/DeweyReed/ChromeMenu/blob/master/images/example.gif?raw=true)

Notice **the press, move, selection, up are finished in one gesture**.

I love this design. It significantly improves the efficiency of using an app. So I dig into chromium, extract related code and pack them into this library.

## Usage

1. Install dependency

    - Jcenter: `implementation 'xyz.aprildown:chrome-menu:0.1.1'`

1. Set up touch listener

    ```Kotlin
    val handler = AppMenuHandler(activity, listener, R.menu.menu)
    val helper = AppMenuButtonHelper(handler)
    btn.setOnTouchListener(helper)
    ```

    `listener` is an [AppMenuPropertiesDelegate](https://github.com/DeweyReed/ChromeMenu/blob/master/library/src/main/java/xyz/aprildown/chromemenu/AppMenuPropertiesDelegate.java#L15) interface or use its abstract version [AbstractAppMenuPropertiesDelegate()](https://github.com/DeweyReed/ChromeMenu/blob/master/library/src/main/java/xyz/aprildown/chromemenu/AbstractAppMenuPropertiesDelegate.java#L12)

    You can define header and footer and hijack menu items after they are created through the listener.

1. Check the sample for more customization.

## More Usage

- Change menu width

    Add this line to `dimens.xml`:

    ```XML
    <!-- 256dp is the original width -->
    <dimen name="cm_menu_width">258dp</dimen>
    ```

## ChangeLog and Migration

- 0.1.1

    - Pull changes from chromium
        - fffabbc: IPH : Set focus to highlighted menu item after opening app menu
        - ead7304: Add common colors in night- and some initial clean up on themes
    - Add proguard rules #1.

- 0.1.0

    To set custom menu width, use `cm_menu_width` instead of the old `menu_width`.
