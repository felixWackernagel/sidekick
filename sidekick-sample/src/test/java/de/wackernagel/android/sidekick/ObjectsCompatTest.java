package de.wackernagel.android.sidekick;

import junit.framework.Assert;

import org.junit.Test;

import de.wackernagel.android.example.sidekick.TagModel;
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

        final TagModel tagA = new TagModel( 1, "A" );
        final TagModel tagB = new TagModel( 2, "B" );
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