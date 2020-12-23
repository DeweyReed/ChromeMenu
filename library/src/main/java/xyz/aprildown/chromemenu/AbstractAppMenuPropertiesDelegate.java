// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public abstract class AbstractAppMenuPropertiesDelegate implements AppMenuPropertiesDelegate {

    @Override
    public void destroy() {
    }

    @Nullable
    @Override
    public List<CustomViewBinder> getCustomViewBinders() {
        return null;
    }

    @Override
    public void prepareMenu(@NonNull Menu menu, @NonNull AppMenuHandler handler) {
    }

    @Nullable
    @Override
    public Bundle getBundleForMenuItem(@NonNull MenuItem item) {
        return null;
    }

    @Override
    public void onMenuDismissed() {
    }

    @Override
    public int getFooterResourceId() {
        return 0;
    }

    @Override
    public int getHeaderResourceId() {
        return 0;
    }

    @Override
    public int getAppMenuLayoutId() {
        return 0;
    }

    @Override
    public int getGroupDividerId() {
        return 0;
    }

    @Override
    public boolean shouldShowFooter(int maxMenuHeight) {
        return false;
    }

    @Override
    public boolean shouldShowHeader(int maxMenuHeight) {
        return false;
    }

    @Override
    public void onFooterViewInflated(@NonNull AppMenuHandler appMenuHandler, @NonNull View view) {
    }

    @Override
    public void onHeaderViewInflated(@NonNull AppMenuHandler appMenuHandler, @NonNull View view) {
    }

    @Override
    public boolean shouldShowIconBeforeItem() {
        return false;
    }

    @Override
    public boolean recordAppMenuSimilarSelectionIfNeeded(int previousMenuItemId, int currentMenuItemId) {
        return false;
    }
}