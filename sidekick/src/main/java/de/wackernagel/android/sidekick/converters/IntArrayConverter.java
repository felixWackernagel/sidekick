package de.wackernagel.android.sidekick.converters;

import android.support.annotation.Nullable;

import java.nio.ByteBuffer;

public class IntArrayConverter {

    private IntArrayConverter() {
    }

    @Nullable
    public static byte[] toByteArray( @Nullable final int[] src ) {
        if( src == null ) {
            return null;
        }

        if( src.length == 0 ) {
            return new byte[0];
        }

        final ByteBuffer buffer = ByteBuffer.allocate(src.length * 4);
        buffer.asIntBuffer().put(src);
        return buffer.array();
    }

    @Nullable
    public static int[] toIntArray( @Nullable byte[] src ) {
        if( src == null ) {
            return null;
        }

        if( src.length == 0 ) {
            return new int[0];
        }

        final int[] dst = new int[src.length / 4];
        final ByteBuffer buffer = ByteBuffer.wrap( src );
        buffer.asIntBuffer().get(dst);
        return dst;
    }
}
