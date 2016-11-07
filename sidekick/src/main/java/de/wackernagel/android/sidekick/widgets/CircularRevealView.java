package de.wackernagel.android.sidekick.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
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
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.wackernagel.android.sidekick.R;

public class CircularRevealView extends View {

    public static final int STATE_CONCEALED = 0;
    public static final int STATE_REVEAL_STARTED = 1;
    public static final int STATE_CONCEAL_STARTED = 2;
    public static final int STATE_REVEALED = 3;

    @IntDef({STATE_CONCEALED, STATE_REVEAL_STARTED, STATE_CONCEAL_STARTED, STATE_REVEALED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private static final int ANIMATION_STARTING = 0;
    private static final int ANIMATION_RUNNING = 1;
    private static final int ANIMATION_NONE = 2;

    private int animationState = ANIMATION_NONE;
    private long animationStartTimeMillis;
    private long animationDuration = 400L;
    private Interpolator interpolator = new AccelerateInterpolator();

    private int state;
    private OnStateChangeListener stateChangeListener;

    private int maxRadius;
    private int circleX;
    private int circleY;
    private Paint circlePaint;

    public CircularRevealView(@NonNull final Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public CircularRevealView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public CircularRevealView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public CircularRevealView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init( @NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes ) {
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.WHITE);

        state = STATE_CONCEALED;

        if( attrs != null ) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularRevealView, defStyleAttr, defStyleRes);
            circlePaint.setColor( a.getColor( R.styleable.CircularRevealView_circularColor, Color.WHITE ) );
            state = a.getInt( R.styleable.CircularRevealView_state, STATE_CONCEALED);
            a.recycle();
        }
    }

    @ColorInt
    public int getCircularColor() {
        return circlePaint.getColor();
    }

    public void setCircularColor(@ColorInt int color ) {
        if( color != circlePaint.getColor() ) {
            circlePaint.setColor(color);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setCircularColorResource(@ColorRes int resId ) {
        setCircularColor( ContextCompat.getColor(getContext(), resId) );
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public long getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        maxRadius = (int) Math.sqrt( Math.pow( (double) getMeasuredWidth(), 2d ) + Math.pow( (double) getMeasuredHeight(), 2d ) );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if( state == STATE_CONCEALED ) {
            return;
        }
        if( state == STATE_REVEALED ) {
            canvas.drawRect( 0, 0, getWidth(), getHeight(), circlePaint );
            return;
        }

        boolean animationDone = true;
        int circleRadius = state == STATE_REVEAL_STARTED ? 0 : maxRadius;

        switch( animationState ) {
            case ANIMATION_STARTING:
                animationStartTimeMillis = SystemClock.uptimeMillis();
                animationDone = false;
                animationState = ANIMATION_RUNNING;
                break;

            case ANIMATION_RUNNING:
                if ( animationStartTimeMillis >= 0) {
                    float normalized = Math.min( 1.0f, (float) (SystemClock.uptimeMillis() - animationStartTimeMillis) / animationDuration );
                    animationDone = normalized >= 1.0f;
                    if( state == STATE_REVEAL_STARTED ) {
                        circleRadius = (int) ( interpolator.getInterpolation(normalized) * maxRadius );
                    } else {
                        circleRadius = (int) ( interpolator.getInterpolation(1.0f - normalized) * maxRadius );
                    }
                }
                break;
        }

        canvas.drawCircle( circleX, circleY, circleRadius, circlePaint );

        if( animationDone ) {
            animationState = ANIMATION_NONE;

            if( state == STATE_REVEAL_STARTED ) {
                changeState(STATE_REVEALED );
            } else {
                changeState(STATE_CONCEALED);
            }
        } else {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void reveal(@Size(2) @NonNull final int[] from ) {
        changeState(STATE_REVEAL_STARTED);
        animationState = ANIMATION_STARTING;
        circleX = from[0];
        circleY = from[1];
        ViewCompat.postInvalidateOnAnimation( this );
    }

    public void conceal(@Size(2) @NonNull final int[] to ) {
        changeState(STATE_CONCEAL_STARTED);
        animationState = ANIMATION_STARTING;
        circleX = to[0];
        circleY = to[1];
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setRevealed() {
        changeState(STATE_REVEALED);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setConcealed() {
        changeState(STATE_CONCEALED);
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
     * Implement interface to get notified on state changes (STATE_CONCEALED, STATE_REVEAL_STARTED, STATE_REVEALED, STATE_CONCEAL_STARTED).
     */
    public interface OnStateChangeListener {
        void onStateChange(@State int state);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.state = state;
        savedState.color = circlePaint.getColor();
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState( savedState.getSuperState() );
        this.state = savedState.state;
        this.circlePaint.setColor( savedState.color );
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

        int state;
        int color;

        SavedState( final Parcelable superState ) {
            super( superState );
        }

        SavedState( final Parcel in ) {
            super( in );
            state = in.readInt();
            color = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(state);
            out.writeInt(color);
        }
    }
}