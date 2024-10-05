# ChromeMenu

[![Download](https://jitpack.io/v/DeweyReed/ChromeMenu.svg)](https://jitpack.io/#DeweyReed/ChromeMenu)
[![API](https://img.shields.io/badge/API-17%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=17)

The menu from [Chromium Android appmenu](https://chromium.googlesource.com/chromium/src/+/refs/heads/master/chrome/browser/ui/android/appmenu/internal/java/src/org/chromium/chrome/browser/ui/appmenu)([Github Mirror](https://github.com/chromium/chromium/tree/master/chrome/browser/ui/android/appmenu/internal/java/src/org/chromium/chrome/browser/ui/appmenu)). It can do this:

![Example GIF](https://github.com/DeweyReed/ChromeMenu/blob/master/images/example.gif?raw=true)

Notice **the press, move, selection, up are finished in one gesture**.

I love this design. It significantly improves the efficiency of using an app. So I dig into chromium, extract related code and pack them into this library.

**Update**: Recently, I found that [getDragToOpenListener](https://developer.android.com/reference/kotlin/androidx/appcompat/widget/PopupMenu#getdragtoopenlistener) provides a similar effect.

## Usage

1. Install dependency

    - JitPack: `implementation 'com.github.DeweyReed:ChromeMenu:0.2.0'`

2. Set up touch listener

    ```Kotlin
    val coordinator = AppMenuCoordinatorFactory.createAppMenuCoordinator(
        this,
        { binding.btn },
        object : AppMenuDelegate {
            override fun onOptionsItemSelected(item: MenuItem, menuItemData: Bundle?): Boolean {
                onMenuItemClicked(item)
                return true
            }
    
            override fun createAppMenuPropertiesDelegate(): AppMenuPropertiesDelegate {
                return object : AbstractAppMenuPropertiesDelegate() {
                    override fun getAppMenuLayoutId(): Int = R.menu.menu
                }
            }
    
            override fun shouldShowAppMenu(): Boolean = true
        },
        window.decorView
    )
    
    binding.btn.setOnTouchListener(
        coordinator.appMenuHandler.createAppMenuButtonHelper()
    )
    ```

3. Check the sample for more customization.

## More Usage

- Change menu width

    Add this line to `dimens.xml`:

    ```XML
    <!-- 256dp is the original width -->
    <dimen name="cm_menu_width">258dp</dimen>
    ```

## ChangeLog and Migration

- 0.2.0

  - New usage
  - Target Android SDK 34, Min Android SDK 21, AGP 8.3.2, AppCompat 1.7.0

- 0.1.1

  - Pull changes from chromium
    - fffabbc: IPH : Set focus to highlighted menu item after opening app menu
    - ead7304: Add common colors in night- and some initial clean up on themes
  - Add proguard rules #1.

- 0.1.0

    To set custom menu width, use `cm_menu_width` instead of the old `menu_width`.
