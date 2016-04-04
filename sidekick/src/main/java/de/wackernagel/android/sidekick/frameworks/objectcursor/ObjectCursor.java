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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import java.util.ArrayList;

/**
 * A cursor-backed type that can return an object for each row of the cursor. This class is most
 * useful when:
 * 1. The cursor is returned in conjuction with an AsyncTaskLoader and created off the UI thread.
 * 2. A single row in the cursor specifies everything for an object.
 */
public class ObjectCursor <T> extends CursorWrapper {
    private final SparseArrayCompat<T> mCache;
    private final ObjectCreator<T> mFactory;
    private final Cursor mCursor;
    private boolean cacheFilled = false;

    /**
     * Creates a new object cursor.
     * @param cursor the underlying cursor this wraps.
     * @param factory to create the object from cursor.
     */
    public ObjectCursor( @NonNull final Cursor cursor, @NonNull final ObjectCreator<T> factory) {
        super(cursor);
        mCache = new SparseArrayCompat<>(cursor.getCount());
        mCursor = cursor;
        mFactory = factory;
    }

	public Cursor getWrappedCursorCompat() {
    	if( Build.VERSION.SDK_INT >= 11 ) {
    		return super.getWrappedCursor();
    	} else {
    		return mCursor;
    	}
    }

    /**
     * Create a concrete object at the current cursor position. There is no guarantee on object
     * creation: an object might have been previously created, or the cache might be populated
     * by calling {@link #fillCache()}. In both these cases, the previously created object is
     * returned.
     * @return a model
     */
    public final T getObject() {
        final Cursor c = mCursor;
        final int currentPosition = c.getPosition();
        // The cache contains this object, return it.
        final T prev = mCache.get(currentPosition);
        if (prev != null) {
            return prev;
        }
        // Get the object at the current position and add it to the cache.
        final T object = mFactory.createFromCursor(c);
        mCache.put(currentPosition, object);
        return object;
    }

    @NonNull
    public final SparseArrayCompat<T> getObjects() {
        if( !cacheFilled ) {
            fillCache();
        }
        return mCache;
    }

    @NonNull
    public final ArrayList<T> getObjectList() {
        if( !cacheFilled ) {
            fillCache();
        }
        final int cacheSize = mCache.size();
    	final ArrayList<T> models = new ArrayList<>( cacheSize );
    	for( int index = 0; index < cacheSize; index++ ) {
    		models.add( mCache.get( index ) );
    	}
    	return models;
    }

    /**
     * Reads the entire cursor to populate the objects in the cache. Subsequent calls to {@link
     * #getObject()} will return the cached objects as far as the underlying cursor does not change.
     */
    final void fillCache() {
        final Cursor c = mCursor;
        cacheFilled = true;
        if( !c.moveToFirst() ) {
            return;
        }
        do {
            getObject(); // get or cache model
        } while (c.moveToNext());
    }

    @Override
    public void close() {
        super.close();
        mCache.clear();
    }

}
