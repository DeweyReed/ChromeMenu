// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * App Menu helper that handles hiding and showing menu items based on activity state.
 */
public interface AppMenuPropertiesDelegate {

    /**
     * @return Whether the App Menu should be shown.
     */
    boolean shouldShowAppMenu();

    /**
     * Allows the delegate to show and hide items before the App Menu is shown. It is called every
     * time the menu is shown. This assumes that the provided menu contains all the items expected
     * in the application menu (i.e. that the main menu has been inflated into it).
     *
     * @param menu Menu that will be used as the source for the App Menu pop up.
     */
    void prepareMenu(@NonNull Menu menu);

    /**
     * Determines whether the header should be shown based on the maximum available menu height.
     *
     * @param maxMenuHeight The maximum available height for the menu to draw.
     * @return Whether the header, as specified in getHeaderView(), should be shown.
     */
    boolean shouldShowHeader(int maxMenuHeight);

    /**
     * @return The resource ID for a layout the be used as the app menu header if there should be
     * one. 0 otherwise. The header will be displayed as the first item in the app menu. It
     * will be scrolled off as the menu scrolls.
     */
    int getHeaderResourceId();


    /**
     * A notification that the header view has finished inflating.
     *
     * @param view    The view that was inflated.
     * @param appMenu The menu the view is inside of.
     */
    void onHeaderViewInflated(@NonNull AppMenu appMenu, @NonNull View view);

    /**
     * Determines whether the footer should be shown based on the maximum available menu height.
     *
     * @param maxMenuHeight The maximum available height for the menu to draw.
     * @return Whether the footer, as specified in {@link #getFooterResourceId()}, should be shown.
     */
    boolean shouldShowFooter(int maxMenuHeight);

    /**
     * @return Resource layout id for the footer if there should be one. O otherwise. The footer
     * is shown at a fixed position at the bottom the app menu. It is always visible and
     * overlays other app menu items if necessary.
     */
    int getFooterResourceId();

    /**
     * A notification that the footer view has finished inflating.
     *
     * @param view    The view that was inflated.
     * @param appMenu The menu the view is inside of.
     */
    void onFooterViewInflated(@NonNull AppMenu appMenu, @NonNull View view);
}
