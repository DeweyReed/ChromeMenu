// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

/**
 * A UI coordinator the app menu.
 */
public interface AppMenuCoordinatorInterface {
    /**
     * Called when the containing activity is being destroyed.
     */
    void destroy();

    /**
     * Shows the app menu (if possible) for a key press on the keyboard with the correct anchor view
     * chosen depending on device configuration and the visible menu button to the user.
     */
    void showAppMenuForKeyboardEvent();

    /**
     * @return The {@link AppMenuHandlerInterface} associated with this activity.
     */
    AppMenuHandlerInterface getAppMenuHandler();

    /**
     * @return The {@link AppMenuPropertiesDelegate} associated with this activity.
     */
    AppMenuPropertiesDelegate getAppMenuPropertiesDelegate();
}