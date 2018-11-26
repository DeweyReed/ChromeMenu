package xyz.aprildown.chromemenu;

import android.view.animation.Interpolator;

/**
 * A pre-baked bezier-curved interpolator for quantum-paper transitions.
 * TODO(dtrainor): Move to the API Compatability version iff that supports the curves we need and
 * once we move to that SDK.
 */
class BakedBezierInterpolator implements Interpolator {

    /**
     * Lookup table values.
     * Generated using a Bezier curve from (0,0) to (1,1) with control points:
     * P0 (0.0, 0.0)
     * P1 (0.0, 0.0)
     * P2 (0.2, 1.0)
     * P3 (1.0, 1.0)
     * <p>
     * Values sampled with x at regular intervals between 0 and 1.
     */
    private static final float[] FADE_IN_VALUES = new float[]{
            0.0029f, 0.043f, 0.0785f, 0.1147f, 0.1476f, 0.1742f, 0.2024f, 0.2319f, 0.2575f, 0.2786f,
            0.3055f, 0.3274f, 0.3498f, 0.3695f, 0.3895f, 0.4096f, 0.4299f, 0.4474f, 0.4649f, 0.4824f,
            0.5f, 0.5176f, 0.5322f, 0.5468f, 0.5643f, 0.5788f, 0.5918f, 0.6048f, 0.6191f, 0.6333f,
            0.6446f, 0.6573f, 0.6698f, 0.6808f, 0.6918f, 0.704f, 0.7148f, 0.7254f, 0.7346f, 0.7451f,
            0.7554f, 0.7655f, 0.7731f, 0.783f, 0.7916f, 0.8f, 0.8084f, 0.8166f, 0.8235f, 0.8315f,
            0.8393f, 0.8459f, 0.8535f, 0.8599f, 0.8672f, 0.8733f, 0.8794f, 0.8853f, 0.8911f, 0.8967f,
            0.9023f, 0.9077f, 0.9121f, 0.9173f, 0.9224f, 0.9265f, 0.9313f, 0.9352f, 0.9397f, 0.9434f,
            0.9476f, 0.9511f, 0.9544f, 0.9577f, 0.9614f, 0.9644f, 0.9673f, 0.9701f, 0.9727f, 0.9753f,
            0.9777f, 0.98f, 0.9818f, 0.9839f, 0.9859f, 0.9877f, 0.9891f, 0.9907f, 0.9922f, 0.9933f,
            0.9946f, 0.9957f, 0.9966f, 0.9974f, 0.9981f, 0.9986f, 0.9992f, 0.9995f, 0.9998f, 1.0f, 1.0f
    };
    /**
     * 0.0 to 0.2 bezier curve.  Should be used for fading in.
     */
    static final BakedBezierInterpolator FADE_IN_CURVE =
            new BakedBezierInterpolator();

    private final float[] mValues;
    private final float mStepSize;

    /**
     * Use the INSTANCE variable instead of instantiating.
     */
    private BakedBezierInterpolator() {
        super();
        mValues = BakedBezierInterpolator.FADE_IN_VALUES;
        mStepSize = 1.f / (mValues.length - 1);
    }

    @Override
    public float getInterpolation(float input) {
        if (input >= 1.0f) {
            return 1.0f;
        }

        if (input <= 0f) {
            return 0f;
        }

        int position = Math.min(
                (int) (input * (mValues.length - 1)),
                mValues.length - 2);

        float quantized = position * mStepSize;
        float difference = input - quantized;
        float weight = difference / mStepSize;

        return mValues[position] + weight * (mValues[position + 1] - mValues[position]);
    }
}
