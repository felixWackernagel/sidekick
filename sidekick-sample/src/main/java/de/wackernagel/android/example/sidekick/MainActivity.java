package de.wackernagel.android.example.sidekick;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.wackernagel.android.sidekick.compats.ObjectsCompat;
import de.wackernagel.android.sidekick.frameworks.objectcursor.ObjectLoader;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<MainActivity.SimpleItem>> {

    private final SimpleAdapter adapter = new SimpleAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView recyclerView = ObjectsCompat.requireNonNull(
                ( RecyclerView ) findViewById(R.id.recyclerView),
                "No RecyclerView in layout found!" );
        recyclerView.setLayoutManager( new LinearLayoutManager(this) );
        recyclerView.setAdapter( adapter );
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader( 0, null, this );
    }

    @Override
    public Loader<List<SimpleItem>> onCreateLoader(int id, Bundle args) {
        if( id != 0 ) {
            return null;
        }
        return new ObjectLoader<List<SimpleItem>>( this ) {
            @Override
            public List<SimpleItem> loadInBackground() {
                final List<SimpleItem> list = new ArrayList<>();
                list.add( new SimpleItem( "Widgets - TypefaceTextView", TypefaceTextViewActivity.class ) );
                list.add( new SimpleItem( "Widgets - AspectRatioImageView", AspectRatioImageViewActivity.class ) );
                list.add( new SimpleItem( "Widgets - CircularRevealView", RevealViewActivity.class ) );
                list.add( new SimpleItem( "Widgets - IndicatorView", IndicatorActivity.class ) );
                list.add( new SimpleItem( "Utils - Tooltip", TooltipActivity.class ) );
                list.add( new SimpleItem( "Utils - ColorFilterUtils", ColorFilterUtilsActivity.class ) );
                list.add( new SimpleItem( "Utils - Device and Network", DeviceActivity.class ) );
                list.add( new SimpleItem( "Utils - Drawable Tinting", TintingActivity.class ) );
                list.add( new SimpleItem( "Helper - Photos", PhotoActivity.class ) );
                list.add( new SimpleItem( "Helper - Grid Gutter Decoration", GridGutterDecorationActivity.class ) );
                list.add( new SimpleItem( "Resources - Colors", ColorsActivity.class ) );
                list.add( new SimpleItem( "Frameworks - ContentProviderProcessor", SimpleProviderActivity.class ) );
                return list;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<SimpleItem>> loader, List<SimpleItem> data) {
        if( loader.getId() != 0 ) {
            return;
        }
        adapter.addItems( data );

        final ProgressBar progress = ( ProgressBar ) findViewById( R.id.progressBar );
        progress.setVisibility( View.GONE );
    }

    @Override
    public void onLoaderReset(Loader<List<SimpleItem>> loader) {
        if( loader.getId() != 0 ) {
            return;
        }
        adapter.clearItems();
    }

    public static class SimpleItem {
        final String name;
        final Class<?> activity;

        SimpleItem(String name, Class<?> activity) {
            this.name = name;
            this.activity = activity;
        }
    }

    public static class SimpleAdapter extends RecyclerView.Adapter<SimpleViewHolder> {
        private ArrayList<SimpleItem> items = new ArrayList<>();

        void addItems( List<SimpleItem> items ) {
            this.items.addAll( items );
            notifyItemRangeInserted( 0, items.size() );
        }

        void clearItems() {
            int size = items.size();
            this.items.clear();
            notifyItemRangeRemoved(0, size);
        }

        SimpleItem getItem( int position ) {
            return items.get( position );
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SimpleViewHolder(LayoutInflater.from( parent.getContext() ).inflate( R.layout.simple_item, parent, false ) );
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            final int adaptPos = holder.getAdapterPosition();
            holder.text.setText( items.get( position ).name );
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), getItem(adaptPos).activity));
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        SimpleViewHolder(View itemView) {
            super(itemView);
            text = ( TextView ) itemView.findViewById(R.id.text);
        }
    }

}
