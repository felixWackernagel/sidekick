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

        Typeface typeface;
        int typefaceIndex = 5;

        if( attrs != null ) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TypefaceTextView, defStyleAttr, defStyleRes);
            typefaceIndex = a.getInt( R.styleable.TypefaceTextView_typeface, 5 );
            a.recycle();
        }

        switch( typefaceIndex ) {
            case 0:
                typeface = TypefaceUtils.getRobotoBlack( context );
                break;
            case 1:
                typeface = TypefaceUtils.getRobotoBold(context);
                break;
            case 2:
                typeface = TypefaceUtils.getRobotoItalic(context);
                break;
            case 3:
                typeface = TypefaceUtils.getRobotoLight(context);
                break;
            case 4:
                typeface = TypefaceUtils.getRobotoMedium(context);
                break;
            case 5:
                typeface = TypefaceUtils.getRobotoRegular(context);
                break;
            case 6:
                typeface = TypefaceUtils.getRobotoThin(context);
                break;
            default:
                typeface = TypefaceUtils.getRobotoRegular(context);
                break;
        }

        setTypeface( typeface );
    }
}
