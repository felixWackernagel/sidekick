package de.wackernagel.android.sidekick;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Date;

import de.wackernagel.android.example.sidekick.db.TagModel;
import de.wackernagel.android.sidekick.compats.ObjectsCompat;

public class ObjectsCompatTest {

    @Test
    public void objects_equals() {
        Assert.assertTrue(ObjectsCompat.equals("hello", "hello"));
        Assert.assertFalse(ObjectsCompat.equals("hello", "hell"));
        Assert.assertFalse(ObjectsCompat.equals("hell", "hello"));
        Assert.assertFalse( ObjectsCompat.equals("hello", null));
        Assert.assertFalse(ObjectsCompat.equals(null, "hello"));
        Assert.assertTrue(ObjectsCompat.equals(null, null));

        final Date now = new Date();
        final TagModel tagA = new TagModel( 1, "A", "tag", now, now );
        final TagModel tagB = new TagModel( 2, "B", "tag", now, now );
        Assert.assertTrue(ObjectsCompat.equals(tagA, tagA));
        Assert.assertFalse(ObjectsCompat.equals(tagA, tagB));
    }

    @Test
    public void objects_hashCode() {
        Assert.assertEquals( "A".hashCode(), ObjectsCompat.hashCode( "A" ) );
        Assert.assertNotSame( "A".hashCode(), ObjectsCompat.hashCode("a") );
        Assert.assertEquals( 0, ObjectsCompat.hashCode( null ) );
    }
}