package xyz.aprildown.flashmenu.util;

import android.content.res.Configuration;
import android.view.View;

public class LocalizationUtils {
    private static Boolean sIsLayoutRtl;

    /**
     * Returns whether the Android layout direction is RTL.
     * <p>
     * Note that the locale direction can be different from layout direction. Two known cases:
     * - RTL languages on Android 4.1, due to the lack of RTL layout support on 4.1.
     * - When user turned on force RTL layout option under developer options.
     * <p>
     * Therefore, only this function should be used to query RTL for layout purposes.
     */
    public static boolean isLayoutRtl() {
        if (sIsLayoutRtl == null) {
            Configuration configuration =
                    ContextUtils.getApplicationContext().getResources().getConfiguration();
            sIsLayoutRtl = ApiCompatibilityUtils.getLayoutDirection(configuration)
                    == View.LAYOUT_DIRECTION_RTL;
        }

        return sIsLayoutRtl;
    }
}
