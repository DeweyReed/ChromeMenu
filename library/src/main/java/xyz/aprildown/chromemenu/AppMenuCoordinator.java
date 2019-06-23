// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A UI coordinator the app menu.
 */
public class AppMenuCoordinator {
    private final Context mContext;
    private final MenuButtonDelegate mButtonDelegate;
    private final AppMenuDelegate mAppMenuDelegate;
    private AppMenuPropertiesDelegate mAppMenuPropertiesDelegate;
    private AppMenuHandler mAppMenuHandler;

    /**
     * Construct a new AppMenuCoordinator.
     *
     * @param context         The activity context.
     * @param buttonDelegate  The {ToolbarManager for the containing activity.
     * @param appMenuDelegate The {@link AppMenuDelegate} for the containing activity.
     * @param decorView       The decor {@link View}, e.g. from Window#getDecorView(), for the containing
     *                        activity.
     */
    public AppMenuCoordinator(Context context,
                              MenuButtonDelegate buttonDelegate,
                              AppMenuDelegate appMenuDelegate,
                              View decorView) {
        mContext = context;
        mButtonDelegate = buttonDelegate;
        mAppMenuDelegate = appMenuDelegate;
        mAppMenuPropertiesDelegate = mAppMenuDelegate.createAppMenuPropertiesDelegate();

        mAppMenuHandler = new AppMenuHandler(mAppMenuPropertiesDelegate, appMenuDelegate, mAppMenuPropertiesDelegate.getAppMenuLayoutId(), decorView);

        // TODO(twellington): Move to UpdateMenuItemHelper or common UI coordinator parent?
        mAppMenuHandler.addObserver(new AppMenuObserver() {
            @Override
            public void onMenuVisibilityChanged(boolean isVisible) {
                if (isVisible) return;

                mAppMenuPropertiesDelegate.onMenuDismissed();
            }

            @Override
            public void onMenuHighlightChanged(boolean highlighting) {
            }
        });
    }

    /**
     * Called when the containing activity is being destroyed.
     */
    public void destroy() {
        // Prevent the menu window from leaking.
        if (mAppMenuHandler != null) mAppMenuHandler.destroy();
    }

    /**
     * Shows the app menu (if possible) for a key press on the keyboard with the correct anchor view
     * chosen depending on device configuration and the visible menu button to the user.
     */
    public void showAppMenuForKeyboardEvent() {
        if (mAppMenuHandler == null || !mAppMenuDelegate.shouldShowAppMenu()) return;

        boolean hasPermanentMenuKey = ViewConfiguration.get(mContext).hasPermanentMenuKey();
        mAppMenuHandler.showAppMenu(
                hasPermanentMenuKey ? null : mButtonDelegate.getMenuButtonView(), false,
                mButtonDelegate.isMenuFromBottom());
    }

    /**
     * @return The {@link AppMenuHandler} associated with this activity.
     */
    public AppMenuHandler getAppMenuHandler() {
        return mAppMenuHandler;
    }

    /**
     * @return The {@link AppMenuPropertiesDelegate} associated with this activity.
     */
    public AppMenuPropertiesDelegate getAppMenuPropertiesDelegate() {
        return mAppMenuPropertiesDelegate;
    }

    /**
     * A delegate to handle menu item selection.
     */
    public interface AppMenuDelegate {
        /**
         * Called whenever an item in the app menu is selected.
         * See {@link android.app.Activity#onOptionsItemSelected(MenuItem)}.
         *
         * @param item         The the menu item that was selected.
         * @param menuItemData Extra data associated with the menu item. May be null.
         */
        boolean onOptionsItemSelected(@NonNull MenuItem item, @Nullable Bundle menuItemData);

        /**
         * @return {@link AppMenuPropertiesDelegate} instance that the {@link AppMenuHandler}
         * should be using.
         */
        AppMenuPropertiesDelegate createAppMenuPropertiesDelegate();

        /**
         * @return Whether the app menu should be shown.
         */
        boolean shouldShowAppMenu();
    }

    /**
     * A delegate that provides the button that triggers the app menu.
     */
    public interface MenuButtonDelegate {
        /**
         * @return The {@link View} for the menu button, used to anchor the app menu.
         */
        @Nullable
        View getMenuButtonView();

        /**
         * @return Whether the menu is shown from the bottom of the screen.
         */
        boolean isMenuFromBottom();
    }
}