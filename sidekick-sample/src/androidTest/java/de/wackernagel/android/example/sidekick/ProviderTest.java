package de.wackernagel.android.example.sidekick;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.wackernagel.android.example.sidekick.db.SampleContentProvider;
import de.wackernagel.android.example.sidekick.db.SimpleContract;

@RunWith(AndroidJUnit4.class)
public class ProviderTest extends ProviderTestCase2<SampleContentProvider> {

    public ProviderTest() {
        super(SampleContentProvider.class, "com.example.provider");
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Test
    public void testQueryEmptyCursor(){
        final Cursor cursor = getMockContentResolver().query(SimpleContract.CONTENT_URI, SimpleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    @Test
    public void testInsert(){
        final ContentValues values = new ContentValues();
        values.put( SimpleContract.COLUMN_A_BOOLEAN, true );
        values.put( SimpleContract.COLUMN_A_DOUBLE, 2d );
        values.put( SimpleContract.COLUMN_A_INT, 1 );
        values.put( SimpleContract.COLUMN_A_STRING, "Hello World" );
        final Uri result = getMockContentResolver().insert(SimpleContract.CONTENT_URI, values);
        assertNotNull(result);
        assertEquals(1, ContentUris.parseId(result));

        final Cursor cursor = getMockContentResolver().query(SimpleContract.CONTENT_URI, SimpleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals(1, cursor.getInt(cursor.getColumnIndex(SimpleContract.COLUMN_A_BOOLEAN)));
        assertEquals( 2d, cursor.getDouble(cursor.getColumnIndex(SimpleContract.COLUMN_A_DOUBLE)));
        assertEquals( 1, cursor.getInt(cursor.getColumnIndex(SimpleContract.COLUMN_A_INT)));
        assertEquals( "Hello World", cursor.getString(cursor.getColumnIndex(SimpleContract.COLUMN_A_STRING)) );
        cursor.close();
    }

    @Test
    public void testBulkInsert() {
        final ContentValues valueA = new ContentValues();
        valueA.put( SimpleContract.COLUMN_A_BOOLEAN, true );
        valueA.put( SimpleContract.COLUMN_A_STRING, "A" );

        final ContentValues valueB = new ContentValues();
        valueB.put(SimpleContract.COLUMN_A_BOOLEAN, false);
        valueB.put(SimpleContract.COLUMN_A_STRING, "B");

        final ContentValues[] values = { valueA, valueB };
        final int successfulInserts = getMockContentResolver().bulkInsert(SimpleContract.CONTENT_URI, values);
        assertEquals(2, successfulInserts);

        final Cursor cursor = getMockContentResolver().query(SimpleContract.CONTENT_URI, SimpleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());

        assertTrue(cursor.moveToFirst());
        assertEquals(1, cursor.getInt(cursor.getColumnIndex(SimpleContract.COLUMN_A_BOOLEAN)));
        assertTrue(cursor.moveToNext());
        assertEquals(0, cursor.getInt(cursor.getColumnIndex(SimpleContract.COLUMN_A_BOOLEAN)));
        assertTrue(cursor.isLast());

        cursor.close();
    }

    @Test
    public void testBulkInsertWithError() {
        final ContentValues valueA = new ContentValues();
        valueA.put( SimpleContract.COLUMN_A_BOOLEAN, true );
        valueA.put( SimpleContract.COLUMN_A_STRING, "A" );

        final ContentValues valueB = new ContentValues();
        valueB.put(SimpleContract.COLUMN_A_BOOLEAN, false);
        valueB.put( SimpleContract.COLUMN_A_STRING, "B" );

        final ContentValues valueError = new ContentValues();
        valueError.put(SimpleContract.COLUMN_A_BOOLEAN, 2);
        valueError.put(SimpleContract.COLUMN_A_STRING, "C");

        final ContentValues[] values = { valueA, valueB, valueError };
        final int successfulInserts = getMockContentResolver().bulkInsert(SimpleContract.CONTENT_URI, values);
        assertEquals(0, successfulInserts);

        final Cursor cursor = getMockContentResolver().query(SimpleContract.CONTENT_URI, SimpleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }
}
