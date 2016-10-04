package de.wackernagel.android.sidekick;

import android.support.v4.util.Pair;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;

import de.wackernagel.android.example.sidekick.OrderContract;
import de.wackernagel.android.example.sidekick.TagContract;
import de.wackernagel.android.example.sidekick.TagModel;
import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.ContentProviderProcessorUtils;

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
