package xyz.aprildown.flashmenu;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.widget.ImageViewCompat;

class ApiCompatibilityUtils {
    /**
     * @see android.content.res.Resources#getDrawable(int id).
     * TODO(ltian): use {@link AppCompatResources} to parse drawable to prevent fail on
     * {@link VectorDrawable}. (http://crbug.com/792129)
     */
    @SuppressWarnings("deprecation")
    static Drawable getDrawable(Resources res, int id) throws Resources.NotFoundException {
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return res.getDrawable(id, null);
            } else {
                return res.getDrawable(id);
            }
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    /**
     * Returns true if view's layout direction is right-to-left.
     *
     * @param view the View whose layout is being considered
     */
    static boolean isLayoutRtl(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        } else {
            // All layouts are LTR before JB MR1.
            return false;
        }
    }

    /**
     * @see Configuration#getLayoutDirection()
     */
    static int getLayoutDirection(Configuration configuration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return configuration.getLayoutDirection();
        } else {
            // All layouts are LTR before JB MR1.
            return View.LAYOUT_DIRECTION_LTR;
        }
    }

    static void setImageTintList(
            @NonNull ImageView view, @Nullable ColorStateList tintList) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            // Work around broken workaround in ImageViewCompat, see https://crbug.com/891609#c3.
            if (tintList != null && view.getImageTintMode() == null) {
                view.setImageTintMode(PorterDuff.Mode.SRC_IN);
            }
        }
        ImageViewCompat.setImageTintList(view, tintList);
    }

    /**
     * Creates regular LayerDrawable on Android L+. On older versions creates a helper class that
     * fixes issues around {@link LayerDrawable#mutate()}. See https://crbug.com/890317 for details.
     *
     * @param layers A list of drawables to use as layers in this new drawable.
     */
    static LayerDrawable createLayerDrawable(@NonNull Drawable[] layers) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return new LayerDrawableCompat(layers);
        }
        return new LayerDrawable(layers);
    }

    /**
     * Helper for {@link LayerDrawableCompat#mutate}.
     * Obtains the bounds of layers so they can be restored after a mutation.
     */
    private static Rect[] getLayersBounds(LayerDrawable layerDrawable) {
        Rect[] result = new Rect[layerDrawable.getNumberOfLayers()];
        for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
            result[i] = layerDrawable.getDrawable(i).getBounds();
        }
        return result;
    }

    /**
     * Helper for {@link LayerDrawableCompat#mutate}.
     * Restores the bounds of layers after a mutation.
     */
    private static void restoreLayersBounds(LayerDrawable layerDrawable, Rect[] oldBounds) {
        if (layerDrawable.getNumberOfLayers() == oldBounds.length) {
            for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
                layerDrawable.getDrawable(i).setBounds(oldBounds[i]);
            }
        }
    }

    /**
     * @see android.content.res.Resources#getColor(int id).
     */
    @SuppressWarnings("deprecation")
    static int getColor(Resources res, int id) throws Resources.NotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(id, null);
        } else {
            return res.getColor(id);
        }
    }

    private static class LayerDrawableCompat extends LayerDrawable {
        private boolean mMutated;

        LayerDrawableCompat(@NonNull Drawable[] layers) {
            super(layers);
        }

        @NonNull
        @Override
        public Drawable mutate() {
            // LayerDrawable in Android K loses bounds of layers, so this method works around that.
            if (mMutated) {
                // This object has already been mutated and shouldn't have any shared state.
                return this;
            }

            Rect[] oldBounds = getLayersBounds(this);
            Drawable superResult = super.mutate();
            // LayerDrawable.mutate() always returns this, bail out if this isn't the case.
            if (superResult != this) return superResult;
            restoreLayersBounds(this, oldBounds);
            mMutated = true;
            return this;
        }
    }
}
