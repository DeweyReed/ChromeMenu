// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package xyz.aprildown.chromemenu;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

/** Reference to one of each standard interpolator to avoid allocations. */
class Interpolators {
    public static final FastOutSlowInInterpolator FAST_OUT_SLOW_IN_INTERPOLATOR =
            new FastOutSlowInInterpolator();
}
