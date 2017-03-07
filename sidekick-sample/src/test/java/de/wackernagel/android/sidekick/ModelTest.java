package de.wackernagel.android.sidekick;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Date;

import de.wackernagel.android.example.sidekick.db.BaseModel;
import de.wackernagel.android.example.sidekick.db.TagModel;
import de.wackernagel.android.example.sidekick.db.TypeModel;

public class ModelTest {

    @Test
    public void model_equals() {
        final Date now = new Date();
        final TagModel tagA = new TagModel( 1, "A", "tag", now, now );
        final TagModel tagB = new TagModel( 2, "B", "tag", now, now );
        final TagModel tagC = new TagModel( 1, "A", "tag", now, now );

        Assert.assertEquals( tagA, tagA );
        Assert.assertNotSame(tagA, tagB);
        Assert.assertEquals(tagA, tagC);
    }

    @Test
    public void model_inheritance() {
        final Date now = new Date();
        final TagModel tag = new TagModel( 1, "A", "tag", now, now );
        Assert.assertTrue(tag instanceof TypeModel);
        Assert.assertTrue(tag instanceof BaseModel);

        final TypeModel type = (TypeModel) tag;
        Assert.assertEquals( "tag", type.getType() );

        final BaseModel base = (BaseModel) tag;
        Assert.assertEquals( 1, base.getId() );
        Assert.assertEquals( now, base.getCreated() );
        Assert.assertEquals( now, base.getChanged() );
    }

}
