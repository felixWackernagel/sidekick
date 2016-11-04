/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.wackernagel.android.sidekick.frameworks.objectcursor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContentResolverCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

/**
 * A copy of the framework's {@link android.content.CursorLoader} class. Copied because
 * CursorLoader is not parameterized, and we want to parameterize over the underlying cursor type.
 * @param <T>
 */
public class ObjectCursorLoader<T> extends AsyncTaskLoader<ObjectCursor<T>> {
    private final ForceLoadContentObserver mObserver;
    private final ObjectCreator<T> mFactory;
    private ObjectCursor<T> mCursor;
    private CancellationSignal mCancellationSignal;

    private final Uri mUri;
    private final String[] mProjection;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final String mSortOrder;

    public ObjectCursorLoader(@NonNull final Context context, @NonNull final Uri uri, @NonNull final ObjectCreator<T> factory,
                              @Nullable final String[] projection, @Nullable final String selection, @Nullable final String[] selectionArgs, @Nullable final String sortOrder) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mFactory = factory;
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    @Override
    public final ObjectCursor<T> loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        try {
            final Cursor inner = loadCursorInBackground();
            if (inner != null) {
                // Ensure the cursor window is filled
                inner.getCount();
                inner.registerContentObserver(mObserver);

                final ObjectCursor<T> cursor = new ObjectCursor<>(inner, mFactory);
                cursor.fillCache();
                return cursor;
            }
            return null;
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    @Nullable
    public CancellationSignal getCancellationSignal() {
        return mCancellationSignal;
    }

    @Nullable
    public Cursor loadCursorInBackground() {
        return ContentResolverCompat.query(
                getContext().getContentResolver(),
                mUri,
                mProjection,
                mSelection,
                mSelectionArgs,
                mSortOrder,
                getCancellationSignal());
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(ObjectCursor<T> cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        final Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous perform of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous perform has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     *
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
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
    public void onCanceled(ObjectCursor<T> cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }
}
