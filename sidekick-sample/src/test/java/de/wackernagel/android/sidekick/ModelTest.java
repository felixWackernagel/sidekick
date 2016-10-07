package de.wackernagel.android.sidekick;

import junit.framework.Assert;

import org.junit.Test;

import de.wackernagel.android.example.sidekick.db.TagModel;

public class ModelTest {

    @Test
    public void model_equals() {
        final TagModel tagA = new TagModel( 1, "A" );
        final TagModel tagB = new TagModel( 2, "B" );
        final TagModel tagC = new TagModel( 1, "A" );

        Assert.assertEquals( tagA, tagA );
        Assert.assertNotSame(tagA, tagB);
        Assert.assertEquals(tagA, tagC);
    }

}
