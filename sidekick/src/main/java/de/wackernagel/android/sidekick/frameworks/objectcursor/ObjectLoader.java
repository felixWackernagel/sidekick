package de.wackernagel.android.sidekick.frameworks.objectcursor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

public abstract class ObjectLoader<T> extends AsyncTaskLoader<T> {

    /* previous result cache */
    private T mData;

    public ObjectLoader(@NonNull final Context context) {
        super(context);
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(T data) {
        if (isReset()) {
            if( data != null ) {
                onReleaseResources( data );
            }
            return;
        }

        T oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if( oldData != null ) {
            onReleaseResources(oldData);
        }
    }

    protected void onReleaseResources(T data) {
    }

    /**
     * Starts an asynchronous task. When the result is ready the callbacks
     * will be called on the UI thread. If a previous perform has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mData != null && isResultValid( mData ) ) {
            deliverResult(mData);
        }

        onRegisterObserver();

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    protected boolean isResultValid( T cachedResult) {
        return true;
    }

    protected void onRegisterObserver() {
    }

    protected void onUnregisterObserver() {
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current perform task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(T data) {
        if ( data != null ) {
            onReleaseResources( data );
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        onUnregisterObserver();

        if ( mData != null ) {
            onReleaseResources( mData );
        }
        mData = null;
    }

}
