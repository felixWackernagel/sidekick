package de.wackernagel.android.sidekick;

import android.text.TextUtils;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.wackernagel.android.sidekick.converters.DateConverter;

import static org.mockito.Matchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TextUtils.class)
public class DateConverterTest {

    @Before
    public void setup() {
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.when(TextUtils.isEmpty(any(CharSequence.class))).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                CharSequence a = (CharSequence) invocation.getArguments()[0];
                return !(a != null && a.length() > 0);
            }
        });
    }

    @Test
    public void date_null() {
        Assert.assertNull( "Date is null", DateConverter.toDate( null ) );
        Assert.assertNull( "Date is empty string", DateConverter.toDate( "" ) );
        Assert.assertNull( "Date contains invalid characters", DateConverter.toDate( "yyyy-MM-dd HH:mm:ss.SSS" ) );
        Assert.assertNull( "Date pattern is to short", DateConverter.toDate( "2017-01-13 22:0" ) );
        Assert.assertNull( "Date pattern is to long", DateConverter.toDate( "2017-01-13 22:00:00.0000" ) );
    }

    @Test
    public void date_string_valid() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2017);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 1);

        Date toTest = calendar.getTime();
        Assert.assertEquals( "Date requires leading zeros (1)", "2017-01-01 01:01:01.001", DateConverter.toString( toTest ));

        calendar.set(Calendar.MILLISECOND, 10);
        toTest = calendar.getTime();
        Assert.assertEquals( "Date requires leading zeros (2)", "2017-01-01 01:01:01.010", DateConverter.toString( toTest ));

        final Date now = new Date();
        final String nowAsString = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault() ).format( now );
        Assert.assertEquals( "Date realtime conversion", nowAsString, DateConverter.toString( now ));
    }

    @Test
    public void date_date_valid() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2017);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 1);

        final Date toTest = calendar.getTime();
        final String toTestAsString = "2017-01-01 01:01:01.001";
        Assert.assertEquals( toTest, DateConverter.toDate( toTestAsString ) );
    }

}
