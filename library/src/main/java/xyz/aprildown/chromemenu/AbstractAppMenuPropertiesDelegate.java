// Copyright 2015 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

public abstract class AbstractAppMenuPropertiesDelegate implements AppMenuPropertiesDelegate {

    @Override
    public void prepareMenu(@NonNull Menu menu) {
    }

    @Override
    public boolean shouldShowHeader(int maxMenuHeight) {
        return false;
    }

    @Override
    public int getHeaderResourceId() {
        return 0;
    }

    @Override
    public void onHeaderViewInflated(@NonNull AppMenu appMenu, @NonNull View view) {
    }

    @Override
    public boolean shouldShowFooter(int maxMenuHeight) {
        return false;
    }

    @Override
    public int getFooterResourceId() {
        return 0;
    }

    @Override
    public void onFooterViewInflated(@NonNull AppMenu appMenu, @NonNull View view) {
    }

    @Override
    public Bundle getBundleForMenuItem(MenuItem item) {
        return null;
    }

    @Override
    public void onMenuDismissed() {
    }
}