package de.wackernagel.android.sidekick.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CircularRevealView extends View {

    public static final int STATE_UNREVEALED = 0;
    public static final int STATE_REVEAL_STARTED = 1;
    public static final int STATE_UNREVEAL_STARTED = 2;
    public static final int STATE_REVEALED = 3;

    @IntDef({STATE_UNREVEALED, STATE_REVEAL_STARTED, STATE_UNREVEAL_STARTED, STATE_REVEALED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private static final long DURATION = 500;
    private static final Interpolator INTERPOLATOR = new AccelerateInterpolator();

    private int state;

    private int circleX;
    private int circleY;
    private int circleRadius;
    private Paint circlePaint;

    private OnStateChangeListener stateChangeListener;

    public CircularRevealView(@NonNull final Context context) {
        this(context, null);
    }

    public CircularRevealView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularRevealView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init( @NonNull final Context context, @Nullable final AttributeSet attrs, int defStyle ) {
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor( Color.WHITE );

        if( attrs != null ) {
            int[] myAttr = { android.R.attr.color };
            TypedArray a = context.obtainStyledAttributes(attrs, myAttr, defStyle, 0);
            setColor( a.getColor( 0, Color.WHITE ) );
            a.recycle();
        }

        state = STATE_UNREVEALED;
        setVisibility(INVISIBLE);
    }

    @ColorInt
    public int getColor() {
        return circlePaint.getColor();
    }

    public void setColor( @ColorInt int color ) {
        if( color != circlePaint.getColor() ) {
            circlePaint.setColor(color);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setColorResource( @ColorRes int resId ) {
        setColor( ContextCompat.getColor(getContext(), resId) );
    }

    private void setCircleRadius( int radius ) {
        this.circleRadius = radius;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if ( state == STATE_REVEALED ) {
            // background
            canvas.drawRect( 0, 0, getWidth(), getHeight(), circlePaint );
        } else {
            // circular reveal
            canvas.drawCircle( circleX, circleY , circleRadius, circlePaint );
        }
    }

    public void enterReveal( @Size(2) final int[] startPoint ) {
        changeState( STATE_REVEAL_STARTED );

        circleX = startPoint[0];
        circleY = startPoint[1];

        final CircularRevealAnimation animation = new CircularRevealAnimation( this, 0, getWidth() + getHeight() );
        animation.setInterpolator(INTERPOLATOR);
        animation.setDuration(DURATION);
        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                changeState(STATE_REVEALED);
            }
        });

        setVisibility(VISIBLE);
        clearAnimation();
        startAnimation( animation );
    }

    public void exitReveal( @Size(2) final int[] endPoint ) {
        changeState( STATE_UNREVEAL_STARTED );

        circleX = endPoint[0];
        circleY = endPoint[1];

        final CircularRevealAnimation animation = new CircularRevealAnimation( this, getWidth() + getHeight(), 0 );
        animation.setInterpolator(INTERPOLATOR);
        animation.setDuration(DURATION);
        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                changeState(STATE_UNREVEALED);
                setVisibility(INVISIBLE);
            }
        });

        clearAnimation();
        startAnimation(animation);
    }

    public void setRevealed() {
        setVisibility( VISIBLE );
        changeState(STATE_REVEALED);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setUnrevealed() {
        setVisibility( INVISIBLE);
        changeState(STATE_UNREVEALED);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void changeState( @State int newState ) {
        if( state == newState) {
            return;
        }

        state = newState;
        if( stateChangeListener != null ) {
            stateChangeListener.onStateChange(newState);
        }
    }

    @State
    public int getState() {
        return state;
    }

    public OnStateChangeListener getOnStateChangeListener() {
        return stateChangeListener;
    }

    public void setOnStateChangeListener( OnStateChangeListener onStateChangeListener ) {
        this.stateChangeListener = onStateChangeListener;
    }

    /**
     * Implement interface to get notified on state changes (STATE_UNREVEALED, STATE_REVEAL_STARTED, STATE_REVEALED, STATE_UNREVEAL_STARTED).
     */
    public interface OnStateChangeListener {
        void onStateChange(@State int state);
    }

    /**
     * Animate radius of given CircularRevealView.
     */
    private static class CircularRevealAnimation extends Animation {
        private final int startRadius;
        private final int endRadius;
        private final CircularRevealView view;

        public CircularRevealAnimation( @NonNull final CircularRevealView view, final int startRadius, final int endRadius ) {
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.view = view;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            view.setCircleRadius( (int)( ( endRadius - startRadius ) * interpolatedTime + startRadius ) );
        }
    }

    /**
     * Simple AnimationListener to override only necessary method hooks.
     */
    private static class AnimationListenerAdapter implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}