package de.wackernagel.android.sidekick.frameworks.media;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class MediaResult implements Parcelable {

    public static Creator<MediaResult> CREATOR = new Creator<MediaResult>() {
        @Override
        public MediaResult createFromParcel(Parcel source) {
            return new MediaResult(source);
        }

        @Override
        public MediaResult[] newArray(int size) {
            return new MediaResult[size];
        }
    };

    private final boolean canceled;
    private final boolean failed;
    private final String mediaPath;

    private MediaResult( Parcel src ) {
        this( src.readInt() == 1, src.readInt() == 1, src.readString() );
    }

    public MediaResult( final boolean canceled, final boolean failed, @Nullable final String mediaPath ) {
        this.canceled = canceled;
        this.failed = failed;
        this.mediaPath = mediaPath;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean hasMediaPath() {
        return mediaPath != null;

    }

    public String getMediaPath() {
        return mediaPath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt( canceled ? 1 : 0 );
        dest.writeInt( failed ? 1 : 0 );
        dest.writeString(mediaPath);
    }

    @Override
    public String toString() {
        return "MediaResult( " + canceled + ", " + failed + ", " + mediaPath + " )";

    }

    public static class Builder {

        private boolean canceled;
        private boolean failed;
        private String mediaPath;

        public Builder setCanceled(boolean canceled) {
            this.canceled = canceled;
            return this;
        }

        public Builder setFailed(boolean failed) {
            this.failed = failed;
            return this;
        }

        public Builder setMediaPath(String mediaPath) {
            this.mediaPath = mediaPath;
            return this;
        }

        public MediaResult build() {
            return new MediaResult(canceled, failed, mediaPath );
        }
    }
}