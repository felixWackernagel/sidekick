package de.wackernagel.android.sidekick.frameworks.media;

import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public class MediaConfig {

    private final String applicationName;
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int compressQuality = 76;

    public MediaConfig( @NonNull final String applicationName ) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    public void setCompressFormat( @NonNull final Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public int getCompressQuality() {
        return compressQuality;
    }

    public void setCompressQuality( @IntRange( from = 0, to = 100 ) int compressQuality) {
        this.compressQuality = compressQuality;
    }


}