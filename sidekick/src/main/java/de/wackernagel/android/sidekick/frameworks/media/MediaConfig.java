package de.wackernagel.android.sidekick.frameworks.media;

import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public class MediaConfig {

    private final String applicationName;

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int compressQuality = 76;

    /**
     * Create a {@link MediaConfig} for the {@link MediaUtils} class.
     *
     * @param applicationName which is used as a unique sub directory name for medias
     */
    public MediaConfig( @NonNull final String applicationName ) {
        this.applicationName = applicationName;
    }

    /**
     * @return your application name which is used as a unique sub directory name for medias
     */
    public final String getApplicationName() {
        return applicationName;
    }

    /**
     * @return format for Bitmap compression (default is JPEG)
     */
    @NonNull
    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    /**
     * @param compressFormat for Bitmap compression
     */
    public void setCompressFormat( @NonNull final Bitmap.CompressFormat compressFormat) {
        if( compressFormat == Bitmap.CompressFormat.JPEG || compressFormat == Bitmap.CompressFormat.PNG ) {
            this.compressFormat = compressFormat;
        } else {
            throw new IllegalArgumentException( "MediaConfig#setCompressFormat accepts at the moment only JPEG and PNG." );
        }
    }

    /**
     * @return quality for Bitmap compression (default is 76)
     */
    @IntRange( from = 0, to = 100 )
    public int getCompressQuality() {
        return compressQuality;
    }

    /**
     * @param compressQuality for Bitmap compression
     */
    public void setCompressQuality( @IntRange( from = 0, to = 100 ) int compressQuality) {
        this.compressQuality = compressQuality;
    }


}