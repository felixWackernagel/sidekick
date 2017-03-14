package de.wackernagel.android.sidekick.frameworks.media;

import android.graphics.Bitmap;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

public class MediaConfig {

    private final String applicationName;
    private final Bitmap.CompressFormat compressionFormat;
    private final int compressionQuality;

    /**
     * Create a {@link MediaConfig} for the {@link MediaUtils} class.
     *
     * @param applicationName as unique directory name for medias
     * @param compressionFormat for all image medias
     * @param compressionQuality for all image medias
     */
    public MediaConfig(@NonNull final String applicationName, @NonNull final Bitmap.CompressFormat compressionFormat, @IntRange( from = 0, to = 100 ) final int compressionQuality) {
        this.applicationName = applicationName;
        this.compressionFormat = compressionFormat;
        this.compressionQuality = compressionQuality;
    }

    /**
     * @return your application name which is used as a unique sub directory name for medias
     */
    @NonNull
    public final String getApplicationName() {
        return applicationName;
    }

    /**
     * @return format for Bitmap compression (default is JPEG)
     */
    @NonNull
    public final Bitmap.CompressFormat getCompressionFormat() {
        return compressionFormat;
    }

    /**
     * @return quality for Bitmap compression (default is 76)
     */
    @IntRange( from = 0, to = 100 )
    public final int getCompressionQuality() {
        return compressionQuality;
    }

    public static Builder builder(@NonNull final String applicationName) {
        return new Builder(applicationName);
    }

    public static class Builder {

        private final String applicationName;
        private Bitmap.CompressFormat compressionFormat;
        private int compressionQuality;

        public Builder(@NonNull final String applicationName) {
            this.applicationName = applicationName;
            this.compressionFormat = Bitmap.CompressFormat.JPEG;
            this.compressionQuality = 76;
        }

        /**
         * @param compressionFormat for Bitmap compression
         */
        public Builder compressionFormat(@NonNull final Bitmap.CompressFormat compressionFormat) {
            this.compressionFormat = compressionFormat;
            return this;
        }

        /**
         * @param compressionQuality for Bitmap compression
         */
        public Builder compressionQuality(@IntRange( from = 0, to = 100 ) final int compressionQuality) {
            this.compressionQuality = compressionQuality;
            return this;
        }

        public MediaConfig build() {
            return new MediaConfig(applicationName, compressionFormat, compressionQuality);
        }
    }
}