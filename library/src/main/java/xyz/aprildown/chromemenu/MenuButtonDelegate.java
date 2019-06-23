// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.view.View;

import androidx.annotation.Nullable;

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