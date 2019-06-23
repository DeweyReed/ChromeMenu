// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.content.Context;
import android.view.View;

/**
 * A factory for creating an {@link AppMenuCoordinator}.
 */
public class AppMenuCoordinatorFactory {
    private AppMenuCoordinatorFactory() {
    }

    /**
     * Create a new AppMenuCoordinator.
     *
     * @param context         The activity context.
     * @param buttonDelegate  The {ToolbarManager for the containing activity.
     * @param appMenuDelegate The {@link AppMenuDelegate} for the containing activity.
     * @param decorView       The decor {@link View}, e.g. from Window#getDecorView(), for the containing
     *                        activity.
     */
    public static AppMenuCoordinator createAppMenuCoordinator(
            Context context,
            MenuButtonDelegate buttonDelegate,
            AppMenuDelegate appMenuDelegate,
            View decorView) {
        return new AppMenuCoordinatorImpl(context, buttonDelegate,
                appMenuDelegate, decorView);
    }
}