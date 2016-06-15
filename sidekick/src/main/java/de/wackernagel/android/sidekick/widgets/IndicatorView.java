package de.wackernagel.android.sidekick.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import de.wackernagel.android.sidekick.R;

public class IndicatorView extends View {

    public static final String TAG = "IndicatorView";

    private Drawable unselectedDrawable;
    private ScaleDrawable selectedDrawable;
    private int count = 0;
    private int selectedIndex = 0;
    private int paddingBetween = 0;
    private int maxIndicatorWidth;
    private int minContentWidth;
    private int minContentHeight;

    private int previousIndex = -1;
    private long animationStartTimeMillis;
    private long animationDuration;

    private static final int ANIMATION_STARTING = 0;
    private static final int ANIMATION_RUNNING = 1;
    private static final int ANIMATION_NONE = 2;
    private int animationState = ANIMATION_NONE;

    private static final int FULL_SIZE = 10000;
    private static final int FULL_OPACITY = 255;

    private int minScaleLevel = 0;

    public IndicatorView(Context context) {
        super(context);
        init( context, null, 0, 0 );
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init( context, attrs, 0, 0 );
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if( attrs != null ) {
            TypedArray a = context.obtainStyledAttributes( attrs, R.styleable.IndicatorView, defStyleAttr, defStyleRes );
            count = a.getInt( R.styleable.IndicatorView_count, 0 );
            selectedIndex = a.getInt( R.styleable.IndicatorView_selectedIndex, 0 );
            paddingBetween = a.getDimensionPixelSize(R.styleable.IndicatorView_paddingBetween, 0);
            selectedDrawable = newScaleDrawable( a.getDrawable(R.styleable.IndicatorView_selectedDrawable) );
            unselectedDrawable = a.getDrawable( R.styleable.IndicatorView_unselectedDrawable );
            a.recycle();
        }
        calculateSize();
    }

    /**
     * Wraps the given drawable in a ScaleDrawable if its non NULL.
     *
     * @param drawable to wrap
     * @return ScaleDrawable or NULL
     */
    private static ScaleDrawable newScaleDrawable( @Nullable final Drawable drawable ) {
        ScaleDrawable scaleDrawable = null;
        if( drawable != null ) {
            scaleDrawable = new ScaleDrawable( drawable, Gravity.NO_GRAVITY, 1.0f, 1.0f);
            scaleDrawable.setLevel(FULL_SIZE);
        }
        return scaleDrawable;
    }

    /**
     * Calculate minimum width and height of this view and the size of a single indicator.
     */
    private void calculateSize() {
        final int unselectedWidth = getIntrinsicWidth( unselectedDrawable );
        final int unselectedHeight = getIntrinsicHeight(unselectedDrawable);
        final int selectedWidth = getIntrinsicWidth( selectedDrawable );
        final int selectedHeight = getIntrinsicHeight( selectedDrawable );

        maxIndicatorWidth = Math.max( unselectedWidth, selectedWidth );
        minContentWidth = ( maxIndicatorWidth * count ) + ( ( count - 1 ) * paddingBetween );
        minContentHeight = Math.max( unselectedHeight, selectedHeight );

        if( unselectedWidth < selectedWidth ) {
            float scalingFactor = (float) unselectedWidth / (float) selectedWidth;
            minScaleLevel = (int) ( FULL_SIZE * scalingFactor );
        } else if( unselectedHeight < selectedHeight ) {
            float scalingFactor = (float) unselectedHeight / (float) selectedHeight;
            minScaleLevel = (int) ( FULL_SIZE * scalingFactor );
        }
    }

    /**
     * @param drawable to get its width
     * @return drawable width or 0 if its NULL
     */
    private static int getIntrinsicWidth( @Nullable final Drawable drawable ) {
        if( drawable != null ) {
            return drawable.getIntrinsicWidth();
        }
        return 0;
    }

    /**
     * @param drawable to get its height
     * @return drawable height or 0 if its NULL
     */
    private static int getIntrinsicHeight( @Nullable final Drawable drawable ) {
        if( drawable != null ) {
            return drawable.getIntrinsicHeight();
        }
        return 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int viewWidth = minContentWidth + getPaddingLeft() + getPaddingRight();
        final int viewHeight = minContentHeight + getPaddingTop() + getPaddingBottom();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(viewWidth, widthSize);
        } else {
            width = viewWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(viewHeight, heightSize);
        } else {
            height = viewHeight;
        }

        setMeasuredDimension( width, height );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if( selectedDrawable == null || unselectedDrawable == null ) {
            Log.w( TAG, "Skip drawing because selected or unselected drawable are NULL." );
            return;
        }

        // PREPARE ANIMATION
        boolean animationDone = true;
        int alpha = 0;
        int scalingDifference = 0;

        switch( animationState ) {
            case ANIMATION_STARTING:
                animationStartTimeMillis = SystemClock.uptimeMillis();
                animationDone = false;
                animationState = ANIMATION_RUNNING;
                break;

            case ANIMATION_RUNNING:
                if ( animationStartTimeMillis >= 0) {
                    float normalized = (float) (SystemClock.uptimeMillis() - animationStartTimeMillis) / animationDuration;
                    animationDone = normalized >= 1.0f;
                    if( animationDone ) {
                        animationState = ANIMATION_NONE;
                        if( minScaleLevel > 0 ) {
                            selectedDrawable.setLevel( FULL_SIZE );
                        }
                        selectedDrawable.setAlpha( FULL_OPACITY );
                    } else {
                        normalized = Math.min(normalized, 1.0f);
                        alpha = (int) (FULL_OPACITY * normalized);
                        if( minScaleLevel > 0 ) {
                            scalingDifference = (int) ( ( FULL_SIZE - minScaleLevel) * normalized );
                        }
                    }
                }
                break;
        }

        // VIEW DRAWING
        final int top = (getMeasuredHeight() / 2) - (minContentHeight / 2);
        int x = (getMeasuredWidth() / 2) - ( minContentWidth / 2 );

        for( int index = 0; index < count; index++ ) {
            if( index > 0 ) {
                x += paddingBetween;
            }

            final Drawable drawable = (index == selectedIndex) ? selectedDrawable : unselectedDrawable;
            final int offsetToCenter = offsetToCenterIndicator(drawable);

            if( !animationDone ) {
                onBeforeIndicatorDraw(canvas, top, x, index, alpha, scalingDifference);
            }

            onIndicatorDraw(canvas, drawable, top, x + offsetToCenter);

            if( !animationDone ) {
                onAfterIndicatorDraw(canvas, top, x, index, alpha, scalingDifference);
            }

            x += offsetToCenter + getIntrinsicWidth( drawable ) + offsetToCenter;
        }

        if ( !animationDone ) {
            invalidate();
        }
    }

    /*
     * Handle animation after the content of the view is drawn.
     */
    private void onAfterIndicatorDraw(Canvas canvas, int startY, int startX, int index, int alpha, int scalingDifference) {
        if( index == previousIndex ) {
            // draw the selected state drawable below the unselected state drawable for animations
            if( minScaleLevel > 0 ) {
                selectedDrawable.setLevel( FULL_SIZE - scalingDifference );
            }
            selectedDrawable.setAlpha( FULL_OPACITY - alpha );

            onIndicatorDraw( canvas, selectedDrawable, startY, startX + offsetToCenterIndicator( selectedDrawable ) );
        }
    }

    /*
     * Handle animation before the content of the view is drawn.
     */
    private void onBeforeIndicatorDraw(Canvas canvas, int startY, int startX, int index, int alpha, int scalingDifference) {
        if( index == selectedIndex ) {
            // draw the unselected state drawable below the selected state drawable for animations
            onIndicatorDraw(canvas, unselectedDrawable, startY, startX + offsetToCenterIndicator(unselectedDrawable));

            // prepare selected state drawable for animation
            if( minScaleLevel > 0 ) {
                selectedDrawable.setLevel( minScaleLevel + scalingDifference );
            }
            selectedDrawable.setAlpha( alpha );
        }
    }

    /**
     * Calculate the offset for the given drawable to center it horizontal in its indicator slot.
     *
     * @param drawable as indicator
     * @return offset to center
     */
    private int offsetToCenterIndicator( final Drawable drawable) {
        int width = getIntrinsicWidth( drawable );
        if( width == maxIndicatorWidth ) {
            return 0;
        }
        return ( maxIndicatorWidth - width ) / 2;
    }

    /**
     * Draw the given drawable starting on top (+ offset to center it vertical) and x.
     *  @param startY as y-coordinate
     * @param canvas for drawing
     * @param drawable to draw
     */
    private void onIndicatorDraw(final Canvas canvas, final Drawable drawable, final int startY, final int startX) {
        int drawableHeight = getIntrinsicHeight(drawable);
        int y = startY;
        if( drawableHeight != minContentHeight ) {
            // center drawable vertical
            y += ( ( minContentHeight - drawableHeight ) / 2 );
        }

        drawable.setBounds(startX, y, startX + getIntrinsicWidth( drawable ), y + drawableHeight );
        drawable.draw( canvas );
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex( final int index ) {
        setSelectedIndex( index, 0 );
    }

    public void setSelectedIndex( final int index, final long animationDuration ) {
        if( index != selectedIndex ) {
            if( animationDuration > 0 ) {
                this.animationDuration = animationDuration;
                animationState = ANIMATION_STARTING;
                previousIndex = selectedIndex;
            }
            selectedIndex = index;
            invalidate();
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount( final int count ) {
        if( this.count != count ) {
            this.count = count;
            calculateSize();
            requestLayout();
            invalidate();
        }
    }

    public Drawable getSelectedDrawable() {
        return selectedDrawable.getDrawable();
    }

    public void setSelectedDrawable( @DrawableRes final int drawableId ) {
        setSelectedDrawable( ContextCompat.getDrawable(getContext(), drawableId) );
    }

    public void setSelectedDrawable( @NonNull final Drawable newDrawable) {
        if( selectedDrawable != newDrawable ) {
            selectedDrawable = newScaleDrawable( newDrawable );
            calculateSize();
            requestLayout();
            invalidate();
        }
    }

    public Drawable getUnselectedDrawable() {
        return unselectedDrawable;
    }

    public void setUnselectedDrawable( @DrawableRes final int drawableId ) {
        setUnselectedDrawable( ContextCompat.getDrawable(getContext(), drawableId) );
    }

    public void setUnselectedDrawable( @NonNull final Drawable newDrawable) {
        if( unselectedDrawable != newDrawable ) {
            unselectedDrawable = newDrawable;
            calculateSize();
            requestLayout();
            invalidate();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.selectedIndex = selectedIndex;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState( savedState.getSuperState() );
        selectedIndex = savedState.selectedIndex;
    }

    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        int selectedIndex;

        private SavedState( final Parcelable superState ) {
            super( superState );
        }

        private SavedState( final Parcel in ) {
            super( in );
            selectedIndex = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(selectedIndex);
        }
    }
}
