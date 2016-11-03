package de.wackernagel.android.sidekick.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import de.wackernagel.android.sidekick.R;
import de.wackernagel.android.sidekick.utils.TypefaceUtils;

public class TypefaceTextView extends TextView {

    public TypefaceTextView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public TypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes ) {
        if( isInEditMode() ) {
            return;
        }

        if( attrs != null ) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypefaceTextView, defStyleAttr, defStyleRes);
            setTypeface( resolveTypeface( context, a.getInt( R.styleable.TypefaceTextView_typeface, 5 ) ) );
            a.recycle();
        } else {
            setTypeface( TypefaceUtils.getRobotoRegular(context) );
        }
    }

    private static Typeface resolveTypeface( Context context, int typefaceIndex ) {
        switch( typefaceIndex ) {
            case 0:
                return TypefaceUtils.getRobotoBlack( context );
            case 1:
                return TypefaceUtils.getRobotoBold(context);
            case 2:
                return TypefaceUtils.getRobotoItalic(context);
            case 3:
               return TypefaceUtils.getRobotoLight(context);
            case 4:
                return TypefaceUtils.getRobotoMedium(context);
            case 6:
                return TypefaceUtils.getRobotoThin(context);
            case 5:
            default:
                return TypefaceUtils.getRobotoRegular(context);
        }
    }
}
