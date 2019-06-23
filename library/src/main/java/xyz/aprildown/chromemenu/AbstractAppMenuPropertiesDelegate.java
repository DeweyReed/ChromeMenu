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

public abstract class AbstractAppMenuPropertiesDelegate implements AppMenuPropertiesDelegate {

    @Override
    public void destroy() {
    }

    @Override
    public void prepareMenu(@NonNull Menu menu) {
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
    public boolean shouldShowFooter(int maxMenuHeight) {
        return false;
    }

    @Override
    public boolean shouldShowHeader(int maxMenuHeight) {
        return false;
    }

    @Override
    public void onFooterViewInflated(@NonNull AppMenuHandlerInterface appMenuHandler, @NonNull View view) {
    }

    @Override
    public void onHeaderViewInflated(@NonNull AppMenuHandlerInterface appMenuHandler, @NonNull View view) {
    }
}