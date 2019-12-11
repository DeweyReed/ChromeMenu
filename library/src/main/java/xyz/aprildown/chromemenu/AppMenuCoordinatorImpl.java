// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.content.Context;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * A UI coordinator the app menu.
 */
class AppMenuCoordinatorImpl implements AppMenuCoordinator {

    private final Context mContext;
    private final MenuButtonDelegate mButtonDelegate;
    private final AppMenuDelegate mAppMenuDelegate;

    private AppMenuPropertiesDelegate mAppMenuPropertiesDelegate;
    private AppMenuHandlerImpl mAppMenuHandler;

    /**
     * Construct a new AppMenuCoordinatorImpl.
     *
     * @param context         The activity context.
     * @param buttonDelegate The {@link MenuButtonDelegate} for the containing activity.
     * @param appMenuDelegate The {@link AppMenuDelegate} for the containing activity.
     * @param decorView       The decor {@link View}, e.g. from Window#getDecorView(), for the containing
     *                        activity.
     */
    AppMenuCoordinatorImpl(
            Context context,
            MenuButtonDelegate buttonDelegate,
            AppMenuDelegate appMenuDelegate,
            View decorView) {
        mContext = context;
        mButtonDelegate = buttonDelegate;
        mAppMenuDelegate = appMenuDelegate;
        mAppMenuPropertiesDelegate = mAppMenuDelegate.createAppMenuPropertiesDelegate();

        mAppMenuHandler = new AppMenuHandlerImpl(mAppMenuPropertiesDelegate, appMenuDelegate, mAppMenuPropertiesDelegate.getAppMenuLayoutId(), decorView);

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

    @Override
    public void destroy() {
        // Prevent the menu window from leaking.
        if (mAppMenuHandler != null) mAppMenuHandler.destroy();

        mAppMenuPropertiesDelegate.destroy();
    }

    @Override
    public void showAppMenuForKeyboardEvent() {
        if (mAppMenuHandler == null || !mAppMenuDelegate.shouldShowAppMenu()) return;

        boolean hasPermanentMenuKey = ViewConfiguration.get(mContext).hasPermanentMenuKey();
        mAppMenuHandler.showAppMenu(
                hasPermanentMenuKey ? null : mButtonDelegate.getMenuButtonView(), false,
                mButtonDelegate.isMenuFromBottom());
    }

    @Override
    public AppMenuHandler getAppMenuHandler() {
        return mAppMenuHandler;
    }

    @Override
    public AppMenuPropertiesDelegate getAppMenuPropertiesDelegate() {
        return mAppMenuPropertiesDelegate;
    }
}