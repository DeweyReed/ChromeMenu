// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
package xyz.aprildown.chromemenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import androidx.annotation.Nullable;

/**
 * An interface for providing a custom view binder for a menu item displayed in the app menu.
 * The binder may be used to custom layout/presentation of individual menu items. Clicks on the menu
 * item will still be handled by the app menu.
 * item need to be handled by {@link AppMenuClickHandler}, which is received in {{@link
 * #getView(MenuItem, View, ViewGroup, LayoutInflater, AppMenuClickHandler)}}.
 */
public interface CustomViewBinder {
    /**
     * Indicates that this view binder does not handle a particular menu item.
     * See {{@link #getItemViewType(int)}}.
     */
    int NOT_HANDLED = -1;

    /**
     * @return The number of types of Views that will be created by
     * {{@link #getView(MenuItem, View, ViewGroup, LayoutInflater, AppMenuClickHandler)}}. The value
     * returned by this method should be effectively treated as final. Once the CustomViewBinder has
     * been retrieved by the app menu, it is expected that the item view type count remains stable.
     */
    int getViewTypeCount();

    /**
     * @param id The id of the menu item to check.
     * @return Return the view type of the item matching the provided id or {@link #NOT_HANDLED} if
     * the item is not handled by this binder.
     */
    int getItemViewType(int id);

    /**
     * Get a View that displays the data for an item handled by this binder.
     * See {@link Adapter#getView(int, View, ViewGroup)}.
     *
     * @param item        The {@link MenuItem} for which to create and bind a view.
     * @param convertView The old view to re-use if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @param inflater    A {@link LayoutInflater} to use when inflating new views.
     * @param appMenuClickHandler A {@link AppMenuClickHandler} to handle click events in the view.
     * @return A View corresponding to the provided menu item.
     */
    View getView(
            MenuItem item, @Nullable View convertView, ViewGroup parent,
            LayoutInflater inflater, AppMenuClickHandler appMenuClickHandler);

    /**
     * Determines whether the enter animation should be applied to the menu item matching the
     * provided id.
     *
     * @param id The id of the menu item to check.
     * @return True if the standard animation should be applied.
     */
    boolean supportsEnterAnimation(int id);

    /**
     * Retrieve the pixel height for the custom view. We cannot use View#getHeight() in {{@link
     * #getView(MenuItem, View, ViewGroup, LayoutInflater, AppMenuClickHandler)}} because View#getHeight() will return 0
     * before the view is laid out. This method is for calculating popup window size, and the height
     * should bese on the layout xml file related to the custom view.
     *
     * @param context The context of the custom view.
     * @return The pixel size of the height.
     */
    int getPixelHeight(Context context);
}
