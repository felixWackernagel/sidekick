package de.wackernagel.android.sidekick.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.wackernagel.android.sidekick.R;

public class AspectRatioImageView extends ImageView {

    public static final int ASPECT_WIDTH = 0;
    public static final int ASPECT_HEIGHT = 1;

    @IntDef({ASPECT_WIDTH, ASPECT_HEIGHT})
    @Retention( RetentionPolicy.SOURCE)
    public @interface Aspect {}

    public static final int RATIO_NONE = 0;
    public static final int RATIO_16_9 = 1;
    public static final int RATIO_3_2 = 2;
    public static final int RATIO_4_3 = 3;
    public static final int RATIO_1_1 = 4;
    public static final int RATIO_3_4 = 5;
    public static final int RATIO_2_3 = 6;

    @IntDef({RATIO_NONE, RATIO_16_9, RATIO_3_2, RATIO_4_3, RATIO_1_1, RATIO_3_4, RATIO_2_3})
    @Retention( RetentionPolicy.SOURCE)
    public @interface Ratio {}

    private int aspect;
    private int ratio;
    private float ratioFactor;

    public AspectRatioImageView(Context context) {
        this( context, null );
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        this( context, attrs, 0 );
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if( attrs != null ) {
            final TypedArray viewAttrs = context.obtainStyledAttributes( attrs, R.styleable.AspectRatioImageView );
            ratio = viewAttrs.getInt(R.styleable.AspectRatioImageView_ratio, RATIO_NONE);
            aspect = viewAttrs.getInt(R.styleable.AspectRatioImageView_aspect, ASPECT_WIDTH);
            viewAttrs.recycle();
        } else {
            ratio = RATIO_NONE;
            aspect = ASPECT_WIDTH;
        }

        ratioFactor = resolveAspectRatioFactor( ratio );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if( ratio == RATIO_NONE ) {
            return;
        }

        int width;
        int height;

        if( aspect == ASPECT_WIDTH ) {
            width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            height = (int)( width / ratioFactor);
        } else {
            height = getMeasuredHeight() - getPaddingBottom() - getPaddingTop();
            width = (int)( height / ratioFactor);
        }

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingBottom() + getPaddingTop();

        setMeasuredDimension(width, height);
    }

    private float resolveAspectRatioFactor( int aspectRatio) {
        switch( aspectRatio ) {
            case RATIO_NONE:
                return 0f;
            case RATIO_16_9:
                return 1.78f;
            case RATIO_3_2:
                return 1.5f;
            case RATIO_4_3:
                return 1.34f;
            case RATIO_1_1:
                return 1.0f;
            case RATIO_3_4:
                return 0.75f;
            case RATIO_2_3:
                return 0.67f;
            default:
                return 0f;
        }
    }

    @Aspect
    public int getAspect() {
        return aspect;
    }

    public void setAspect( @Aspect int aspect ) {
        if( aspect != ASPECT_WIDTH && aspect != ASPECT_HEIGHT ) {
            throw new IllegalArgumentException(" Aspect can be 0 (width) or 1 (height).");
        }

        if( this.aspect == aspect ) {
            return;
        }

        this.aspect = aspect;
        requestLayout();
    }

    @Ratio
    public int getRatio() {
        return ratio;
    }

    public void setRatio( @Ratio int ratio ) {
        if( ratio < RATIO_NONE || ratio > RATIO_2_3 ) {
            throw new IllegalArgumentException("Ratio can be between 0 and 6.");
        }

        if( this.ratio == ratio ) {
            return;
        }

        this.ratio = ratio;
        this.ratioFactor = resolveAspectRatioFactor(ratio);
        requestLayout();
    }
}
