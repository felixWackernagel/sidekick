package de.wackernagel.android.example.sidekick;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.wackernagel.android.example.sidekick.provider.ArticleContract;
import de.wackernagel.android.example.sidekick.provider.ArticleModel;
import de.wackernagel.android.example.sidekick.provider.CallbackAsyncQueryHandler;
import de.wackernagel.android.sidekick.frameworks.objectcursor.ObjectCursor;
import de.wackernagel.android.sidekick.frameworks.objectcursor.ObjectCursorLoader;
import de.wackernagel.android.sidekick.utils.DeviceUtils;
import de.wackernagel.android.sidekick.utils.SparseArrayUtils;

public class SimpleProviderActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ObjectCursor<ArticleModel>>, CallbackAsyncQueryHandler.Callback {

    private ArticleAdapter adapter;
    private CallbackAsyncQueryHandler queryHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_provider);

        adapter = new ArticleAdapter() {
            @Override
            public void onItemClick(View itemView, int adapterPosition) {
                update( getItems().get( adapterPosition ) );
            }
        };

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setAdapter(adapter);
        recyclerView.getItemAnimator().setAddDuration( 1000L );

        queryHandler = new CallbackAsyncQueryHandler( getContentResolver() );
        queryHandler.setCallback( this );

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        queryHandler.setCallback(null);
    }

    public void insert( View v ) {
        final EditText input = (EditText) findViewById(R.id.input);
        final String title = input.getText().toString().trim();
        if( TextUtils.isEmpty( title ) ) {
            Toast.makeText( this, "Enter a title", Toast.LENGTH_SHORT ).show();
            return;
        }

        final ContentValues values = new ContentValues( 1 );
        values.put(ArticleContract.COLUMN_TITLE, title);
        queryHandler.startInsert(0, null, ArticleContract.CONTENT_URI, values);
    }

    @Override
    public void onInsertComplete(int token, Object cookie, Uri uri) {
        final long id = ContentUris.parseId( uri );
        if( id > 0 ) {
            final EditText input = (EditText) findViewById(R.id.input);
            input.setText(null);
            Toast.makeText( SimpleProviderActivity.this, "New Article with ID " + id, Toast.LENGTH_SHORT ).show();
        }
    }

    void update( @NonNull final ArticleModel article ) {
        final ContentValues values = new ContentValues( 1 );
        values.put( ArticleContract.COLUMN_TITLE, article.getTitle() + " (changed)" );
        queryHandler.startUpdate( 0, null, ContentUris.withAppendedId( ArticleContract.CONTENT_URI, article.getId() ), values, null, null );
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int result) {
        Toast.makeText( this, result + " article changed.", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public Loader<ObjectCursor<ArticleModel>> onCreateLoader(int i, Bundle bundle) {
        /**
         * The loader is responsible to fetch the data again by observing the uri.
         * The ContentProvider notifies each observer about data changes by insert, update and delete operations.
         */
        return new ObjectCursorLoader<>(
                this,
                ArticleContract.CONTENT_URI,
                ArticleModel.FACTORY,
                ArticleContract.PROJECTION,
                null,
                null,
                ArticleContract.COLUMN_ID + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<ObjectCursor<ArticleModel>> loader, ObjectCursor<ArticleModel> objectCursor) {
        if( objectCursor != null ) {
            adapter.swapItems( SparseArrayUtils.asList( objectCursor.getObjects() ) );
        }
    }

    @Override
    public void onLoaderReset(Loader<ObjectCursor<ArticleModel>> loader) {
        adapter.clearItems();
    }

    /**
     * Helper to explain differences in ArticleModels
     */
    public static class ArticleDiffCallback extends DiffUtil.Callback {
        private final List<ArticleModel> oldList;
        private final List<ArticleModel> newList;

        ArticleDiffCallback(final List<ArticleModel> oldList, final List<ArticleModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get( oldItemPosition ).getId() == newList.get( newItemPosition ).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get( oldItemPosition ).equals( newList.get( newItemPosition ) );
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            final ArticleModel newArticle = newList.get(newItemPosition);
            final ArticleModel oldArticle = oldList.get(oldItemPosition);
            final Bundle diffBundle = new Bundle();
            if ( !newArticle.getTitle().equals( oldArticle.getTitle() ) ) {
                diffBundle.putString( ArticleContract.COLUMN_TITLE, newArticle.getTitle() );
            }
            if (diffBundle.size() == 0) return null;
            return diffBundle;
        }
    }

    /**
     * A simple ViewHolder for Articles
     */
    public static class ArticleHolder extends RecyclerView.ViewHolder {
        public final TextView title;

        ArticleHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById( R.id.text );
        }
    }

    /**
     * A RecycleView Adapter which holds 'ArticleModel's and uses DiffUtil to calculate changes in updates.
     */
    public static class ArticleAdapter extends RecyclerView.Adapter<ArticleHolder> {
        ArrayList<ArticleModel> items;
        private AsyncTask<ArticleModel, Void, DiffUtil.DiffResult> itemSwapTask;

        ArticleAdapter() {
            this.items = new ArrayList<>();
            setHasStableIds( true );
        }

        @NonNull
        ArrayList<ArticleModel> getItems() {
            return items;
        }

        void swapItems( @NonNull final ArrayList<ArticleModel> newItems ) {
            if( itemSwapTask != null && itemSwapTask.getStatus() != AsyncTask.Status.FINISHED ) {
                itemSwapTask.cancel( true );
            }

            itemSwapTask = new AsyncTask<ArticleModel, Void, DiffUtil.DiffResult>() {
                @Override
                protected DiffUtil.DiffResult doInBackground(ArticleModel... params) {
                    return DiffUtil.calculateDiff( new ArticleDiffCallback( items, Arrays.asList( params ) ) );
                }

                @Override
                protected void onPostExecute(DiffUtil.DiffResult diffResult) {
                    items.clear();
                    items.addAll( newItems );
                    diffResult.dispatchUpdatesTo(ArticleAdapter.this);
                }
            }.execute( newItems.toArray(new ArticleModel[ newItems.size() ] ) );
        }

        void clearItems() {
            int size = items.size();
            items.clear();
            notifyItemRangeRemoved(0, size);
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.simple_item;
        }

        @Override
        public long getItemId(int position) {
            return items.get( position ).getId();
        }

        @Override
        public ArticleHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            final View itemView = LayoutInflater.from(viewGroup.getContext()).inflate( viewType, viewGroup, false);
            final ArticleHolder viewHolder = new ArticleHolder( itemView );
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    final int adapterPosition = viewHolder.getAdapterPosition();
                    if( adapterPosition != RecyclerView.NO_POSITION ) {
                        onItemClick( itemView, adapterPosition );
                    }
                }
            });
            return viewHolder;
        }

        public void onItemClick( final View itemView, final int adapterPosition ) {
            Toast.makeText( itemView.getContext(), items.get( adapterPosition ).toString(), Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onBindViewHolder( @NonNull ArticleHolder articleHolder, int position) {
            articleHolder.title.setText( items.get( position ).getTitle() );
        }

        @Override
        public void onBindViewHolder( @NonNull ArticleHolder articleHolder, int position, @NonNull List<Object> payloads) {
            if( payloads.isEmpty() ) {
                onBindViewHolder( articleHolder, position  );
            } else {
                final Bundle changes = (Bundle) payloads.get( 0 );
                articleHolder.title.setText(changes.getString( ArticleContract.COLUMN_TITLE ) );
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * A ItemDecoration which draws a divider under each item.
     */
    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final Drawable mDivider;
        private final int mHeight;

        DividerItemDecoration( final Context context ) {
            mDivider = new ColorDrawable(ContextCompat.getColor( context, R.color.sidekick_divider ) );
            mHeight = DeviceUtils.dpToPx(1f, context);
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();
            final int childCount = Math.max( parent.getChildCount() - 1, 0 );
            for (int position = 0; position < childCount; position++) {
                final View child = parent.getChildAt(position);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin + (int) ViewCompat.getTranslationY( child );
                final int bottom = top + mHeight;
                // include ItemAnimators effect to drawing process
                if( parent.getItemAnimator().isRunning() ) {
                    mDivider.setAlpha( (int) ( 255 * ViewCompat.getAlpha( child ) ) );
                } else {
                    mDivider.setAlpha(255);
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, mHeight);
        }
    }
}
