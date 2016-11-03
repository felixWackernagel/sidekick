package de.wackernagel.android.sidekick.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;

import de.wackernagel.android.sidekick.R;

public class ForegroundRelativeLayout extends RelativeLayout {

    private Drawable foregroundDrawable;
    private int foregroundGravity = Gravity.FILL;

    private final Rect mSelfBounds = new Rect();
    private final Rect mOverlayBounds = new Rect();

    private boolean foregroundBoundsChanged = false;

    public ForegroundRelativeLayout(Context context) {
        this(context, null);
    }

    public ForegroundRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForegroundRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
        init(context, attrs, defStyle);
    }

    private void init( @NonNull final Context context, @Nullable final AttributeSet attrs, int defStyle ) {
        if( attrs != null ) {
            final TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.ForegroundRelativeLayout, defStyle, 0 );

            setForeground(a.getDrawable(R.styleable.ForegroundRelativeLayout_android_foreground));
            setForegroundGravity(a.getInt(R.styleable.ForegroundRelativeLayout_android_foregroundGravity, Gravity.FILL));

            a.recycle();
        }
    }

    public int getForegroundGravity() {
        return foregroundGravity;
    }

    public void setForegroundGravity(int foregroundGravity) {
        if (this.foregroundGravity != foregroundGravity) {
            if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.START;
            }

            if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                foregroundGravity |= Gravity.TOP;
            }

            this.foregroundGravity = foregroundGravity;


            if (this.foregroundGravity == Gravity.FILL && foregroundDrawable != null) {
                Rect padding = new Rect();
                foregroundDrawable.getPadding(padding);
            }

            requestLayout();
        }
    }

    @Override
    protected boolean verifyDrawable( @NonNull Drawable who) {
        return super.verifyDrawable(who) || (who == foregroundDrawable);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (foregroundDrawable != null) DrawableCompat.jumpToCurrentState(foregroundDrawable);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (foregroundDrawable != null && foregroundDrawable.isStateful()) {
            foregroundDrawable.setState(getDrawableState());
        }
    }

    public void setForeground(Drawable drawable) {
        if (foregroundDrawable != drawable) {
            if (foregroundDrawable != null) {
                foregroundDrawable.setCallback(null);
                unscheduleDrawable(foregroundDrawable);
            }

            foregroundDrawable = drawable;

            if (drawable != null) {
                setWillNotDraw(false);
                drawable.setCallback(this);
                if (drawable.isStateful()) {
                    drawable.setState(getDrawableState());
                }
                if (foregroundGravity == Gravity.FILL) {
                    Rect padding = new Rect();
                    drawable.getPadding(padding);
                }
            } else {
                setWillNotDraw(true);
            }
            requestLayout();
            invalidate();
        }
    }

    public Drawable getForeground() {
        return foregroundDrawable;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        foregroundBoundsChanged = changed;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        foregroundBoundsChanged = true;
    }

    @Override
    public void draw( @NonNull final Canvas canvas) {
        super.draw(canvas);

        if (foregroundDrawable != null) {
            final Drawable foreground = foregroundDrawable;

            if (foregroundBoundsChanged) {
                foregroundBoundsChanged = false;
                final Rect selfBounds = mSelfBounds;
                final Rect overlayBounds = mOverlayBounds;

                final int w = getRight() - getLeft();
                final int h = getBottom() - getTop();
                selfBounds.set(0, 0, w, h);

                Gravity.apply(foregroundGravity, foreground.getIntrinsicWidth(), foreground.getIntrinsicHeight(), selfBounds, overlayBounds);
                foreground.setBounds( overlayBounds );
            }

            foreground.draw(canvas);
        }
    }
}