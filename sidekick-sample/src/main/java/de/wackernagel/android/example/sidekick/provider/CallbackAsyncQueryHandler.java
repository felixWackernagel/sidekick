package de.wackernagel.android.example.sidekick.provider;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.Nullable;

public class CallbackAsyncQueryHandler extends AsyncQueryHandler {

    public interface Callback {
        void onInsertComplete(int token, Object cookie, Uri uri);
        void onUpdateComplete(int token, Object cookie, int result);
    }

    private Callback callback;

    public CallbackAsyncQueryHandler(ContentResolver cr) {
        super(cr);
    }

    public void setCallback(@Nullable final Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        if( callback == null ) return;
        callback.onInsertComplete(token,cookie,uri);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        if( callback == null ) return;
        callback.onUpdateComplete( token, cookie, result );
    }
}
