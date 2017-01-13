package de.wackernagel.android.sidekick;

import android.support.v4.util.Pair;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.ContentProviderProcessorUtils;

public class ContentProviderProcessorUtilsTest {
    @Test
    public void qualified_projection() {
        Assert.assertArrayEquals( new String[]{"table.abc", "table.def"}, ContentProviderProcessorUtils.qualifiedProjection( "table", new String[]{ "abc","def" } ) );

        final Pair<String, String[]> tableA = Pair.create("table", new String[]{ "abc","def" } );
        final Pair<String, String[]> tableB = Pair.create("otherTable", new String[]{ "ghi" } );
        Assert.assertArrayEquals( new String[]{"table.abc", "table.def", "otherTable.ghi"}, ContentProviderProcessorUtils.qualifiedProjection(Arrays.asList(tableA, tableB)));
    }
}
