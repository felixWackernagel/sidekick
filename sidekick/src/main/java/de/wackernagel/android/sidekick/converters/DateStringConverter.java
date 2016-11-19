package de.wackernagel.android.sidekick.converters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class DateStringConverter {

    private static final Calendar calendar = Calendar.getInstance();
    private static final StringBuilder format = new StringBuilder(23);
    static {
        calendar.clear();
    }

    private static final Pattern TIMESTAMP = Pattern.compile("\\d{4}-\\d{2}-\\d{2}( |T)\\d{2}:\\d{2}(:\\d{2}(\\.\\d{3})?)?");

    private DateStringConverter() {}

    /**
     * Supported timestamp formats are:
     *
     * YYYY-MM-DD HH:MM
     * YYYY-MM-DD HH:MM:SS
     * YYYY-MM-DD HH:MM:SS.SSS
     * YYYY-MM-DDTHH:MM
     * YYYY-MM-DDTHH:MM:SS
     * YYYY-MM-DDTHH:MM:SS.SSS
     *
     * @param timestamp to convert
     * @return Date or null when timestamp format wasn't supported or null
     */
    @Nullable
    public static Date toDate( @Nullable final String timestamp ) {
        if( !TextUtils.isEmpty( timestamp ) && TIMESTAMP.matcher( timestamp ).matches() ) {
            int year = Integer.valueOf( timestamp.substring(0, 4) );
            int month = Integer.valueOf( timestamp.substring(5, 7) );
            --month;
            int day = Integer.valueOf( timestamp.substring(8, 10) );
            int hour = Integer.valueOf( timestamp.substring(11, 13) );
            int minute = Integer.valueOf( timestamp.substring(14, 16) );
            int second = timestamp.length() > 16 ? Integer.valueOf( timestamp.substring(17, 19) ) : 0;
            int millisecond = timestamp.length() > 19 ? Integer.valueOf( timestamp.substring(20, 23) ) : 0;
            calendar.set(year, month, day, hour, minute, second);
            calendar.set( Calendar.MILLISECOND, millisecond );
            return calendar.getTime();
        }
        return null;
    }

    /**
     * Return a Date as String in format YYYY-MM-DD HH:MM:SS.SSS.
     *
     * @param date to convert
     * @return Date as String or null when date was null.
     */
    @Nullable
    public static String toString( @Nullable final Date date ) {
        if( date == null ) {
            return null;
        }

        calendar.setTime( date );
        format.setLength( 0 );
        format.append( calendar.get( Calendar.YEAR ) ).append( "-" );
        twoDigits( format, calendar.get( Calendar.MONTH ) + 1 );
        format.append( "-");
        twoDigits( format, calendar.get( Calendar.DAY_OF_MONTH ) );
        format.append( " " );
        twoDigits( format, calendar.get( Calendar.HOUR_OF_DAY ) );
        format.append( ":" );
        twoDigits( format, calendar.get( Calendar.MINUTE ) );
        format.append( ":" );
        twoDigits( format, calendar.get( Calendar.SECOND) );
        format.append( "." ).append( calendar.get( Calendar.MILLISECOND ) );
        return format.toString();
    }

    /**
     * Add a zero before the number if its less then 10.
     *
     * @param format builder
     * @param number to append
     */
    private static void twoDigits(@NonNull  final StringBuilder format, final int number) {
        if( number < 10 ) {
            format.append( 0 );
        }
        format.append(number);
    }

    public static Date convertDate( Date dateFrom, String fromTimeZone, String toTimeZone ) {
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        SimpleDateFormat sdfFrom = new SimpleDateFormat (pattern);
        sdfFrom.setTimeZone(TimeZone.getTimeZone(fromTimeZone));

        SimpleDateFormat sdfTo = new SimpleDateFormat (pattern);
        sdfTo.setTimeZone(TimeZone.getTimeZone(toTimeZone));

        try {
            return sdfFrom.parse(sdfTo.format(dateFrom));
        } catch( ParseException e ) {
            return null;
        }
    }
}
