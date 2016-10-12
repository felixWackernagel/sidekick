package de.wackernagel.android.example.sidekick;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.wackernagel.android.example.sidekick.provider.ArticleContract;
import de.wackernagel.android.example.sidekick.provider.ArticleModel;
import de.wackernagel.android.sidekick.frameworks.objectcursor.ObjectCursor;
import de.wackernagel.android.sidekick.frameworks.objectcursor.SimpleObjectCursorLoader;

public class SimpleProviderActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ObjectCursor<ArticleModel>> {

    private final ArticleAdapter adapter = new ArticleAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_simple_provider );

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setAdapter( adapter );
        recyclerView.setHasFixedSize( true );

        getSupportLoaderManager().initLoader( 0, null, this );
    }

    public void insert( View v ) {
        final EditText input = (EditText) findViewById(R.id.input);
        final String title = input.getText().toString();
        final ContentValues values = new ContentValues( 1 );
        values.put( ArticleContract.COLUMN_TITLE, title );
        getContentResolver().insert( ArticleContract.CONTENT_URI, values );
    }

    @Override
    public Loader<ObjectCursor<ArticleModel>> onCreateLoader(int i, Bundle bundle) {
        return new SimpleObjectCursorLoader<>(
                this,
                ArticleContract.CONTENT_URI,
                ArticleModel.FACTORY,
                ArticleContract.PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<ObjectCursor<ArticleModel>> loader, ObjectCursor<ArticleModel> objectCursor) {
        if( objectCursor != null ) {
            adapter.setItems( objectCursor.getObjectList() );
        }
    }

    @Override
    public void onLoaderReset(Loader<ObjectCursor<ArticleModel>> loader) {
        adapter.clearItems();
    }

    public static class ArticleHolder extends RecyclerView.ViewHolder {
        public final TextView title;

        public ArticleHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById( R.id.text );
        }
    }

    public static class ArticleAdapter extends RecyclerView.Adapter<ArticleHolder> {
        private ArrayList<ArticleModel> items;

        public ArticleAdapter() {
            this.items = new ArrayList<>();
            setHasStableIds( true );
        }

        public void setItems( @NonNull final List<ArticleModel> newItems ) {
            items.clear();
            items.addAll(newItems);
            notifyItemRangeInserted( 0, newItems.size() );
        }

        public void clearItems() {
            int size = items.size();
            items.clear();
            notifyItemRangeRemoved(0 , size);
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
        public ArticleHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ArticleHolder(LayoutInflater.from(viewGroup.getContext()).inflate( i, viewGroup, false));
        }

        @Override
        public void onBindViewHolder( ArticleHolder articleHolder, int position ) {
            articleHolder.title.setText( items.get( position ).getTitle() );
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
