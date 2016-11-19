package de.wackernagel.android.sidekick;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Date;

import de.wackernagel.android.example.sidekick.db.TagModel;

public class ModelTest {

    @Test
    public void model_equals() {
        final Date now = new Date();
        final TagModel tagA = new TagModel( 1, "A", now, now );
        final TagModel tagB = new TagModel( 2, "B", now, now );
        final TagModel tagC = new TagModel( 1, "A", now, now );

        Assert.assertEquals( tagA, tagA );
        Assert.assertNotSame(tagA, tagB);
        Assert.assertEquals(tagA, tagC);
    }

}
