package de.wackernagel.android.sidekick.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class SqliteDateTimeUtils {

    private static final Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
    static {
        calendar.clear();
    }

    private static final Pattern TIMESTAMP = Pattern.compile( "\\b[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}" );
    private static final Pattern DATE = Pattern.compile( "\\b[0-9]{4}-[0-9]{2}-[0-9]{2}" );
    private static final Pattern TIME = Pattern.compile( "\\b[0-9]{2}:[0-9]{2}:[0-9]{2}" );

    /**
     * @param timestamp in format YYYY-MM-DD hh:mm:ss with GMT timezone
     * @return Date with timestamp or null
     */
    @Nullable
    public static Date parseTimestamp(@Nullable final String timestamp ) {
        if( !TextUtils.isEmpty( timestamp ) && TIMESTAMP.matcher( timestamp ).matches() ) {
            int year = Integer.valueOf( timestamp.substring(0, 4) );
            int month = Integer.valueOf( timestamp.substring(5, 7) );
            --month;
            int day = Integer.valueOf( timestamp.substring(8, 10) );
            int hour = Integer.valueOf( timestamp.substring(11, 13) );
            int minute = Integer.valueOf( timestamp.substring(14, 16) );
            int second = Integer.valueOf( timestamp.substring(17, 19) );
            calendar.set(year, month, day, hour, minute, second);
            return calendar.getTime();
        }
        return null;
    }

    /**
     * @param time in format hh:mm:ss with GMT timezone
     * @return Date with time or null
     */
    @Nullable
    public static Date parseTime( @Nullable final String time ) {
        if( !TextUtils.isEmpty( time ) && TIME.matcher( time ).matches() ) {
            int hour = Integer.valueOf( time.substring(0, 2) );
            int minute = Integer.valueOf( time.substring(3, 5) );
            int second = Integer.valueOf( time.substring(6, 8) );
            calendar.set(0, 0, 0, hour, minute, second);
            return calendar.getTime();
        }
        return null;
    }

    /**
     * @param date in format YYYY-MM-DD with GMT timezone
     * @return Date with date or null
     */
    @Nullable
    public static Date parseDate( @Nullable final String date ) {
        if( !TextUtils.isEmpty( date ) && DATE.matcher( date ).matches() ) {
            int year = Integer.valueOf( date.substring(0, 4) );
            int month = Integer.valueOf( date.substring(5, 7) );
            --month;
            int day = Integer.valueOf( date.substring(8, 10) );
            calendar.set(year, month, day, 0, 0, 0);
            return calendar.getTime();
        }
        return null;
    }
}
