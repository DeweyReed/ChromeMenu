// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A delegate to handle menu item selection.
 */
public interface AppMenuDelegate {
    /**
     * Called whenever an item in the app menu is selected.
     * See {@link android.app.Activity#onOptionsItemSelected(MenuItem)}.
     *
     * @param item         The menu item that was selected.
     * @param menuItemData Extra data associated with the menu item. May be null.
     */
    boolean onOptionsItemSelected(@NonNull MenuItem item, @Nullable Bundle menuItemData);

    /**
     * @return {@link AppMenuPropertiesDelegate} instance that the {@link AppMenuHandlerInterface}
     * should be using.
     */
    AppMenuPropertiesDelegate createAppMenuPropertiesDelegate();

    /**
     * @return Whether the app menu should be shown.
     */
    boolean shouldShowAppMenu();
}