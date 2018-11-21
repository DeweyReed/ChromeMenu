package xyz.aprildown.flashmenu.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class AccessibilityUtil {
    /**
     * Shows the content description toast for items on the toolbar.
     *
     * @param context     The context to use for the toast.
     * @param view        The view to anchor the toast.
     * @param description The string shown in the toast.
     * @return Whether a toast has been shown successfully.
     */
    @SuppressLint("RtlHardcoded")
    public static boolean showAccessibilityToast(
            Context context, View view, CharSequence description) {
        if (description == null) return false;

        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        final int[] screenPos = new int[2];
        view.getLocationOnScreen(screenPos);
        final int width = view.getWidth();
        final int height = view.getHeight();

        final int horizontalGravity =
                (screenPos[0] < screenWidth / 2) ? Gravity.LEFT : Gravity.RIGHT;
        final int xOffset = (screenPos[0] < screenWidth / 2)
                ? screenPos[0] + width / 2
                : screenWidth - screenPos[0] - width / 2;
        final int yOffset = (screenPos[1] < screenHeight / 2) ? screenPos[1] + height / 2
                : screenPos[1] - height * 3 / 2;

        Toast toast = Toast.makeText(context, description, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | horizontalGravity, xOffset, yOffset);
        toast.show();
        return true;
    }
}
