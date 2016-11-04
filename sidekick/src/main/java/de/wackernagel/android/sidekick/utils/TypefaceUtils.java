package de.wackernagel.android.sidekick.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

public class TypefaceUtils {

    private final static int ROBOTO_BLACK = 0;
    private final static int ROBOTO_BOLD = 1;
    private final static int ROBOTO_ITALIC = 2;
    private final static int ROBOTO_LIGHT = 3;
    private final static int ROBOTO_MEDIUM = 4;
    private final static int ROBOTO_REGULAR = 5;
    private final static int ROBOTO_THIN = 6;

    private final static SparseArray<Typeface> mTypefaces = new SparseArray<>( 6 );

    private TypefaceUtils() {
    }

    public static Typeface getRobotoThin( Context context ) {
    	return getTypeface( context, ROBOTO_THIN );
    }

    public static Typeface getRobotoLight( Context context ) {
    	return getTypeface( context, ROBOTO_LIGHT );
    }

    public static Typeface getRobotoRegular( Context context ) {
    	return getTypeface( context, ROBOTO_REGULAR );
    }
    
    public static Typeface getRobotoItalic( Context context ) {
    	return getTypeface( context, ROBOTO_ITALIC );
    }
    
    public static Typeface getRobotoMedium( Context context ) {
    	return getTypeface( context, ROBOTO_MEDIUM );
    }

    public static Typeface getRobotoBold( Context context ) {
    	return getTypeface( context, ROBOTO_BOLD );
    }

    public static Typeface getRobotoBlack( Context context ) {
    	return getTypeface( context, ROBOTO_BLACK );
    }

    private static Typeface getTypeface( Context context, int typefaceValue ) throws IllegalArgumentException {
        Typeface typeface = mTypefaces.get( typefaceValue );
        if( typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            mTypefaces.put( typefaceValue, typeface );
        }
        return typeface;
    }

    private static Typeface createTypeface( Context context, int typefaceValue ) throws IllegalArgumentException {
        Typeface typeface;
        switch (typefaceValue) {
            case ROBOTO_THIN:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Sidekick-Roboto-Thin.ttf");
                break;
            case ROBOTO_LIGHT:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Sidekick-Roboto-Light.ttf");
                break;
            case ROBOTO_REGULAR:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Sidekick-Roboto-Regular.ttf");
                break;
            case ROBOTO_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Sidekick-Roboto-Italic.ttf");
                break;
            case ROBOTO_MEDIUM:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Sidekick-Roboto-Medium.ttf");
                break;
            case ROBOTO_BOLD:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Sidekick-Roboto-Bold.ttf");
                break;
            case ROBOTO_BLACK:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Sidekick-Roboto-Black.ttf");
                break;
            default:
                throw new IllegalArgumentException( "Unknown `typeface` attribute value " + typefaceValue );
        }
        return typeface;
    }

}
