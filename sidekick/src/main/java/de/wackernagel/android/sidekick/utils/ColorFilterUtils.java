package de.wackernagel.android.sidekick.utils;

import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * @author LANNY MCNIE
 * @see ColorMatrix Class in AS3 (http://blog.gskinner.com/archives/2007/12/colormatrix_cla.html)
 */
public class ColorFilterUtils {

    private static float[] DELTA = {
            0f,    0.01f, 0.02f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.1f,  0.11f,
            0.12f, 0.14f, 0.15f, 0.16f, 0.17f, 0.18f, 0.20f, 0.21f, 0.22f, 0.24f,
            0.25f, 0.27f, 0.28f, 0.30f, 0.32f, 0.34f, 0.36f, 0.38f, 0.40f, 0.42f,
            0.44f, 0.46f, 0.48f, 0.5f,  0.53f, 0.56f, 0.59f, 0.62f, 0.65f, 0.68f,
            0.71f, 0.74f, 0.77f, 0.80f, 0.83f, 0.86f, 0.89f, 0.92f, 0.95f, 0.98f,
            1.0f,  1.06f, 1.12f, 1.18f, 1.24f, 1.30f, 1.36f, 1.42f, 1.48f, 1.54f,
            1.60f, 1.66f, 1.72f, 1.78f, 1.84f, 1.90f, 1.96f, 2.0f,  2.12f, 2.25f,
            2.37f, 2.50f, 2.62f, 2.75f, 2.87f, 3.0f,  3.2f,  3.4f,  3.6f,  3.8f,
            4.0f,  4.3f,  4.7f,  4.9f,  5.0f,  5.5f,  6.0f,  6.5f,  6.8f,  7.0f,
            7.3f,  7.5f,  7.8f,  8.0f,  8.4f,  8.7f,  9.0f,  9.4f,  9.6f,  9.8f,
            10.0f
    };

    public static void adjustHue( @NonNull final ColorMatrix matrix, @IntRange( from = -180, to = 180) int value ) {
        float x = adjustBounds(value, 180) / 180f * (float) Math.PI;

        if (x == 0) {
            return;
        }

        float cosVal = (float) Math.cos(x);
        float sinVal = (float) Math.sin(x);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = {
                lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,
                0f, 0f, 0f, 1f, 0f
        };
        matrix.postConcat(new ColorMatrix(mat));
    }

    public static void adjustBrightness( @NonNull final ColorMatrix matrix, @IntRange( from = -100, to = 100) int value ) {
        value = adjustBounds(value, 100);
        if (value == 0) {
            return;
        }

        float[] mat =  {
                1,0,0,0,value,
                0,1,0,0,value,
                0,0,1,0,value,
                0,0,0,1,0,
        };
        matrix.postConcat(new ColorMatrix(mat));
    }

    public static void adjustContrast( @NonNull final ColorMatrix matrix, @IntRange( from = -100, to = 100) int value ) {
        value = adjustBounds(value, 100);
        if (value == 0) {
            return;
        }

        float x;
        if( value < 0 ) {
            x = 127f + (float)value / 100f * 127f;
        } else {
            x = DELTA[value] * 127f + 127f;
        }

        float[] mat =  {
                x/127,0,0,0, 0.5f*(127-x),
                0,x/127,0,0, 0.5f*(127-x),
                0,0,x/127,0, 0.5f*(127-x),
                0,0,0,1,0
        };
        matrix.postConcat(new ColorMatrix(mat));
    }

    public static void adjustSaturation(@NonNull final ColorMatrix matrix, @IntRange( from = -100, to = 100) int value) {
        float y = adjustBounds(value, 100);
        if (y == 0) {
            return;
        }

        float x = 1+((y > 0) ? 3 * y / 100 : y / 100);
        float lumR = 0.3086f;
        float lumG = 0.6094f;
        float lumB = 0.0820f;

        float[] mat = {
                lumR*(1-x)+x,lumG*(1-x),lumB*(1-x),0,0,
                lumR*(1-x),lumG*(1-x)+x,lumB*(1-x),0,0,
                lumR*(1-x),lumG*(1-x),lumB*(1-x)+x,0,0,
                0,0,0,1,0
        };
        matrix.postConcat(new ColorMatrix(mat));
    }

    private static int adjustBounds(int value, int limit) {
        return Math.min( limit, Math.max( -limit, value ) );
    }

    public static ColorFilter adjustColor(
            @IntRange( from = -100, to = 100) int brightness,
            @IntRange( from = -100, to = 100) int contrast,
            @IntRange( from = -100, to = 100) int saturation,
            @IntRange( from = -180, to = 180) int hue) {

        final ColorMatrix matrix = new ColorMatrix();
        adjustHue(matrix, hue);
        adjustContrast(matrix, contrast);
        adjustBrightness(matrix, brightness);
        adjustSaturation(matrix, saturation);

        return new ColorMatrixColorFilter(matrix);
    }

}
