package xyz.aprildown.chromemenu;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

class ViewHighlighter {

    /**
     * Create a circular highlight layer over the view.
     * @param view The view to be highlighted.
     */
    public static void turnOnCircularHighlight(View view) {
        if (view == null) return;

        PulseDrawable pulseDrawable = PulseDrawable.createCircle(view.getContext());

        attachViewAsHighlight(view, pulseDrawable);
    }

    /**
     * Create a rectangular highlight layer over the view.
     * @param view The view to be highlighted.
     */
    public static void turnOnRectangularHighlight(View view) {
        if (view == null) return;

        PulseDrawable pulseDrawable = PulseDrawable.createRectangle(view.getContext());

        attachViewAsHighlight(view, pulseDrawable);
    }

    /**
     * Turns off the highlight from the view. The original background of the view is restored.
     * @param view The associated view.
     */
    public static void turnOffHighlight(View view) {
        if (view == null) return;

        boolean highlighted = view.getTag(R.id.cm_highlight_state) != null
                ? (boolean) view.getTag(R.id.cm_highlight_state)
                : false;
        if (!highlighted) return;
        view.setTag(R.id.cm_highlight_state, false);

        Resources resources = view.getContext().getApplicationContext().getResources();
        Drawable existingBackground = view.getBackground();
        if (existingBackground instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) existingBackground;
            if (layerDrawable.getNumberOfLayers() >= 2) {
                view.setBackground(
                        layerDrawable.getDrawable(0).getConstantState().newDrawable(resources));
            } else {
                view.setBackground(null);
            }
        }
    }

    /**
     * Attach a custom PulseDrawable as a highlight layer over the view.
     *
     * Will not highlight if the view is already highlighted.
     *
     * @param view The view to be highlighted.
     * @param pulseDrawable The highlight.
     */
    public static void attachViewAsHighlight(View view, PulseDrawable pulseDrawable) {
        boolean highlighted = view.getTag(R.id.cm_highlight_state) != null
                ? (boolean) view.getTag(R.id.cm_highlight_state)
                : false;
        if (highlighted) return;

        Resources resources = view.getContext().getResources();
        Drawable background = view.getBackground();
        if (background != null) {
            background = background.getConstantState().newDrawable();
        }

        Drawable[] layers = background == null ? new Drawable[] {pulseDrawable}
                : new Drawable[] {background, pulseDrawable};
        LayerDrawable drawable = new LayerDrawable(layers);
        view.setBackground(drawable);
        view.setTag(R.id.cm_highlight_state, true);

        pulseDrawable.start();
    }
}
