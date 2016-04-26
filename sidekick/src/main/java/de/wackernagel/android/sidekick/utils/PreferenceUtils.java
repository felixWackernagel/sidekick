package de.wackernagel.android.sidekick.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.preference.PreferenceManager;

import java.util.Date;
import java.util.Set;

public class PreferenceUtils {

    public static String getString(@NonNull final Context context, @NonNull final String key, final String fallback) {
        return getString(PreferenceManager.getDefaultSharedPreferences(context), key, fallback);
    }

	public static String getString(@NonNull final SharedPreferences preferences, @NonNull final String key, final String fallback) {
		return preferences.getString(key, fallback);
	}

    public static void setString(@NonNull final Context context, @NonNull final String key, final String value) {
        setString(PreferenceManager.getDefaultSharedPreferences(context), key, value);
    }
	
	public static void setString(@NonNull final SharedPreferences preferences, @NonNull final String key, final String value) {
		final Editor editor = preferences.edit();
		editor.putString( key, value );
		apply(editor);
	}

    public static boolean getBoolean(@NonNull final Context context, @NonNull final String key, final boolean fallback) {
        return getBoolean(PreferenceManager.getDefaultSharedPreferences(context), key, fallback);
    }

	public static boolean getBoolean(@NonNull final SharedPreferences preferences, @NonNull final String key, final boolean fallback) {
		return preferences.getBoolean(key, fallback);
	}

    public static void setBoolean(@NonNull final Context context, @NonNull final String key, final boolean value) {
        setBoolean(PreferenceManager.getDefaultSharedPreferences(context), key, value);
    }

    public static void setBoolean(@NonNull final SharedPreferences preferences, @NonNull final String key, final boolean value) {
        final Editor editor = preferences.edit();
        editor.putBoolean( key, value );
        apply(editor);
    }

    public static long getLong(@NonNull final Context context, @NonNull final String key, final long fallback) {
        return getLong(PreferenceManager.getDefaultSharedPreferences(context), key, fallback);
    }

    public static long getLong(@NonNull final SharedPreferences preferences, @NonNull final String key, final long fallback) {
        return preferences.getLong(key, fallback);
    }

    public static void setLong(@NonNull final Context context, @NonNull final String key, final long value) {
        setLong(PreferenceManager.getDefaultSharedPreferences(context), key, value);
    }
	
	public static void setLong(@NonNull final SharedPreferences preferences, @NonNull final String key, final long value) {
		final Editor editor = preferences.edit();
		editor.putLong( key, value );
		apply(editor);
	}

    public static float getFloat(@NonNull final Context context, @NonNull final String key, final float fallback ) {
        return getFloat(PreferenceManager.getDefaultSharedPreferences(context), key, fallback);
    }

	public static float getFloat(@NonNull final SharedPreferences preferences, @NonNull final String key, final float fallback ) {
		return preferences.getFloat(key, fallback);
	}

    public static void setFloat(@NonNull final Context context, @NonNull final String key, final float value) {
        setFloat(PreferenceManager.getDefaultSharedPreferences(context), key, value);
    }

    public static void setFloat(@NonNull final SharedPreferences preferences, @NonNull final String key, final float value) {
        final Editor editor = preferences.edit();
        editor.putFloat( key, value );
        apply(editor);
    }

    public static int getInt(@NonNull final Context context, @NonNull final String key, final int fallback ) {
        return getInt(PreferenceManager.getDefaultSharedPreferences(context), key, fallback);
    }

    public static int getInt(@NonNull final SharedPreferences preferences, @NonNull final String key, final int fallback ) {
        return preferences.getInt(key, fallback);
    }

    public static void setInt(@NonNull final Context context, @NonNull final String key, final int value) {
        setInt(PreferenceManager.getDefaultSharedPreferences(context), key, value);
    }

    public static void setInt(@NonNull final SharedPreferences preferences, @NonNull final String key, final int value) {
        final Editor editor = preferences.edit();
        editor.putInt( key, value );
        apply(editor);
    }

    @TargetApi(11)
    public static Set<String> getStringSet(@NonNull final Context context, @NonNull final String key, final Set<String> fallback ) {
        return getStringSet(PreferenceManager.getDefaultSharedPreferences(context), key, fallback);
    }

    @TargetApi(11)
    public static Set<String> getStringSet(@NonNull final SharedPreferences preferences, @NonNull final String key, final Set<String> fallback ) {
        return preferences.getStringSet(key, fallback);
    }

    @TargetApi(11)
    public static void setStringSet(@NonNull final Context context, @NonNull final String key, final Set<String> value) {
        setStringSet(PreferenceManager.getDefaultSharedPreferences(context), key, value);
    }

    @TargetApi(11)
    public static void setStringSet(@NonNull final SharedPreferences preferences, @NonNull final String key, final Set<String> value) {
        final Editor editor = preferences.edit();
        editor.putStringSet(key, value);
        apply(editor);
    }

    @NonNull
    public static Date getDate(@NonNull final Context context, @NonNull final String key, @NonNull final Date fallback) {
        return getDate( PreferenceManager.getDefaultSharedPreferences(context), key, fallback );
    }

    @NonNull
    public static Date getDate(@NonNull final SharedPreferences preferences, @NonNull final String key, @NonNull final Date fallback) {
        return new Date( getLong( preferences, key, fallback.getTime() ) );
    }

    public static void setDate(@NonNull final Context context, @NonNull final String key, @NonNull final Date value) {
        setDate( PreferenceManager.getDefaultSharedPreferences(context), key, value );
    }

    public static void setDate(@NonNull final SharedPreferences preferences, @NonNull final String key, @NonNull final Date value) {
        setLong(preferences, key, value.getTime());
    }

	private static void apply(@NonNull final Editor editor) {
		SharedPreferencesCompat.EditorCompat.getInstance().apply( editor );
	}

}