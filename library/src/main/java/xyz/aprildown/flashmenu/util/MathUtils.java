package xyz.aprildown.flashmenu.util;

public class MathUtils {
    /**
     * Moves {@code value} forward to {@code target} based on {@code speed}.
     *
     * @param value  The current value.
     * @param target The target value.
     * @param speed  How far to move {@code value} to {@code target}.  0 doesn't move it at all.  1
     *               moves it to {@code target}.
     * @return The new interpolated value.
     */
    public static float interpolate(float value, float target, float speed) {
        return (value + (target - value) * speed);
    }
}
