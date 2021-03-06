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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.wackernagel.android.example.sidekick.db.TagModel;
import de.wackernagel.android.example.sidekick.provider.ArticleContract;
import de.wackernagel.android.example.sidekick.provider.ArticleProvider;

@RunWith(AndroidJUnit4.class)
public class ProviderTest extends ProviderTestCase2<ArticleProvider> {

    public ProviderTest() {
        super(ArticleProvider.class, ArticleProvider.AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Test
    public void testQueryEmptyCursor(){
        final Cursor cursor = getMockContentResolver().query(ArticleContract.CONTENT_URI, ArticleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    @Test
    public void testInsert(){
        final ContentValues values = new ContentValues();
        values.put(ArticleContract.COLUMN_TITLE, "Hello World");
        final Uri result = getMockContentResolver().insert(ArticleContract.CONTENT_URI, values);
        assertNotNull(result);
        assertEquals(1, ContentUris.parseId(result));

        final Cursor cursor = getMockContentResolver().query(ArticleContract.CONTENT_URI, ArticleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        assertTrue(cursor.moveToFirst());
        assertEquals( "Hello World", cursor.getString(cursor.getColumnIndex(ArticleContract.COLUMN_TITLE)) );
        cursor.close();
    }

    @Test
    public void testBulkInsert() {
        final ContentValues valueA = new ContentValues();
        valueA.put( ArticleContract.COLUMN_TITLE, "Hello World" );

        final ContentValues valueB = new ContentValues();
        valueB.put( ArticleContract.COLUMN_TITLE, "Foo Bar" );

        final ContentValues[] values = { valueA, valueB };
        final int successfulInserts = getMockContentResolver().bulkInsert(ArticleContract.CONTENT_URI, values);
        assertEquals(2, successfulInserts);

        final Cursor cursor = getMockContentResolver().query(ArticleContract.CONTENT_URI, ArticleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(2, cursor.getCount());

        assertTrue(cursor.moveToFirst());
        assertEquals("Hello World", cursor.getString(cursor.getColumnIndex(ArticleContract.COLUMN_TITLE)));
        assertTrue(cursor.moveToNext());
        assertEquals("Foo Bar", cursor.getString(cursor.getColumnIndex(ArticleContract.COLUMN_TITLE)));
        assertTrue(cursor.isLast());
        cursor.close();
    }

    @Test
    public void testBulkInsertWithError() {
        final ContentValues valueA = new ContentValues();
        valueA.put( ArticleContract.COLUMN_TITLE, "Hello World" );

        final ContentValues valueB = new ContentValues();
        valueB.put( ArticleContract.COLUMN_TITLE, "Foo Bar" );

        final ContentValues valueError = new ContentValues();
        valueB.put( ArticleContract.COLUMN_TITLE, "Hello World" );

        final ContentValues[] values = { valueA, valueB, valueError };
        final int successfulInserts = getMockContentResolver().bulkInsert(ArticleContract.CONTENT_URI, values);
        assertEquals(0, successfulInserts);

        final Cursor cursor = getMockContentResolver().query(ArticleContract.CONTENT_URI, ArticleContract.PROJECTION, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    @Test
    public void testDate() {
        Cursor cursor = getMockContentResolver().query(TagModel.Contract.CONTENT_URI, TagModel.Contract.PROJECTION, null, null, null);
        assertNotNull( cursor );
        assertEquals( 0, cursor.getCount() );

        final Date now = new Date();
        final ContentValues values = TagModel.builder().setName( "tag1" ).setType( "tag" ).setCreated( now ).setChanged( now ).build();
        final String nowAsString = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault() ).format( now );
        assertEquals( "ContentValues name", "tag1", values.getAsString( TagModel.Contract.COLUMN_NAME ) );
        assertEquals( "ContentValues created",nowAsString, values.getAsString( TagModel.Contract.COLUMN_CREATED ) );
        assertEquals( "ContentValues changed", nowAsString, values.getAsString( TagModel.Contract.COLUMN_CHANGED ) );
        assertEquals( "ContentValues type", "tag", values.getAsString( TagModel.Contract.COLUMN_TYPE ) );

        final Uri insert = getMockContentResolver().insert( TagModel.Contract.CONTENT_URI, values );
        assertNotNull( insert );
        assertEquals( 1, ContentUris.parseId( insert ) );

        cursor = getMockContentResolver().query(TagModel.Contract.CONTENT_URI, TagModel.Contract.PROJECTION, null, null, null);
        assertNotNull( cursor );
        assertEquals( 1, cursor.getCount() );
        assertTrue( cursor.moveToFirst() );
        final TagModel model = TagModel.FACTORY.createFromCursor( cursor );
        assertEquals( "Tag id", 1L, model.getId() );
        assertEquals( "Tag type", "tag", model.getType() );
        assertEquals( "Tag name", "tag1", model.getName() );
        assertEquals( "Tag created",now, model.getCreated() );
        assertEquals( "Tag changed", now, model.getChanged() );
        cursor.close();
        assertTrue( cursor.isClosed() );
    }

    @Test
    public void testDateDefault() {
        Cursor cursor = getMockContentResolver().query(TagModel.Contract.CONTENT_URI, TagModel.Contract.PROJECTION, null, null, null);
        assertNotNull( cursor );
        assertEquals( 0, cursor.getCount() );

        final Uri insert = getMockContentResolver().insert( TagModel.Contract.CONTENT_URI,
                TagModel.builder().setName( "tag1" ).setType("tag").build() );
        assertNotNull( insert );
        assertEquals( 1, ContentUris.parseId( insert ) );

        cursor = getMockContentResolver().query(TagModel.Contract.CONTENT_URI, TagModel.Contract.PROJECTION, null, null, null);
        assertNotNull( cursor );
        assertEquals( 1, cursor.getCount() );
        assertTrue( cursor.moveToFirst() );
        final TagModel model = TagModel.FACTORY.createFromCursor( cursor );
        assertEquals( 1L, model.getId() );
        assertEquals( "tag1", model.getName() );
        assertEquals( "tag", model.getType() );
        assertNotNull( model.getCreated() );
        assertNotNull( model.getChanged() );

        cursor.close();
        assertTrue( cursor.isClosed() );
    }
}
