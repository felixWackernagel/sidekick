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

import android.database.Cursor;
import android.database.CursorWrapper;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

/**
 * A cursor-backed type that can return an object for each row of the cursor. This class is most
 * useful when:
 * 1. The cursor is returned in conjuction with an AsyncTaskLoader and created off the UI thread.
 * 2. A single row in the cursor specifies everything for an object.
 */
public class ObjectCursor<T> extends CursorWrapper {
    private final Cursor wrappedCursor;
    private final SparseArrayCompat<T> objectCache;
    private final ObjectCreator<T> objectFactory;
    private boolean cacheFilled = false;

    /**
     * Creates a new object cursor.
     *
     * @param cursor to wrap.
     * @param factory creates the object from cursor.
     */
    public ObjectCursor( @NonNull final Cursor cursor, @NonNull final ObjectCreator<T> factory) {
        super(cursor);
        wrappedCursor = cursor;
        objectCache = new SparseArrayCompat<>(cursor.getCount());
        objectFactory = factory;
    }

    /**
     * Create a concrete object at the current cursor position. There is no guarantee on object
     * creation: an object might have been previously created, or the cache might be populated
     * by calling {@link #fillCache()}. In both these cases, the previously created object is
     * returned.
     * @return a model
     */
    public final T getObject() {
        final Cursor cursor = wrappedCursor;
        final SparseArrayCompat<T> cache = objectCache;
        final int currentPosition = cursor.getPosition();

        // The cache contains this object, return it.
        final T prev = cache.get( currentPosition );
        if( prev != null ) {
            return prev;
        }

        // Get the object at the current position and add it to the cache.
        final T object = objectFactory.createFromCursor(cursor);
        cache.put(currentPosition, object);
        return object;
    }

    @NonNull
    public final SparseArrayCompat<T> getObjects() {
        if( !cacheFilled ) {
            fillCache();
        }
        return objectCache;
    }

    /**
     * Reads the entire cursor to populate the objects in the cache. Subsequent calls to {@link
     * #getObject()} will return the cached objects as far as the underlying cursor does not change.
     */
    final void fillCache() {
        final Cursor cursor = wrappedCursor;
        cacheFilled = true;
        if( !cursor.moveToFirst() ) {
            return;
        }
        do {
            getObject(); // get or cache model
        } while (cursor.moveToNext());
    }

    @Override
    public void close() {
        super.close();
        objectCache.clear();
    }

}
