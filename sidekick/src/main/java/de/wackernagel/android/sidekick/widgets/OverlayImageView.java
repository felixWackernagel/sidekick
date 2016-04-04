package de.wackernagel.android.sidekick.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Draw a rectangle above the image with the given color and alpha value.
 *
 * TODO: Remove the overdraw which is increased by 1 with this drawing technique.
 */
public class OverlayImageView extends ImageView {

    private int overlayColor;
    private int overlayAlpha;
    private Paint overlayPaint;

    public OverlayImageView( final Context context) {
        this(context, null);
    }

    public OverlayImageView( final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayImageView( final Context context, @Nullable final AttributeSet attrs, final int defStyle ) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init( final Context context, @Nullable final AttributeSet attrs, final int defStyle ) {
        overlayColor = Color.BLACK;
        overlayAlpha = 51; // 20%
        overlayPaint = new Paint();
        updateOverlay();
    }

    private void updateOverlay() {
        overlayPaint.setColor( ColorUtils.setAlphaComponent( overlayColor, overlayAlpha ) );
    }

    @ColorInt
    public int getOverlayColor() {
        return overlayColor;
    }

    public void setOverlayColorResource( @ColorRes final int colorRes ) {
        setOverlayColor(ContextCompat.getColor(getContext(), colorRes));
    }

    public void setOverlayColor( @ColorInt final int color ) {
        if (this.overlayColor != color) {
            this.overlayColor = color;
            updateOverlay();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @ColorInt
    public int getOverlayAlpha() {
        return overlayAlpha;
    }

    public void setOverlayAlpha( @FloatRange( from = 0.0, to = 1.0 ) final float alpha ) {
        int overlayAlpha  = (int) (255 * alpha);
        if( this.overlayAlpha != overlayAlpha ) {
            this.overlayAlpha = overlayAlpha;
            updateOverlay();
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), overlayPaint);
    }

}