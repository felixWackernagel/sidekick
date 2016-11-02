package de.wackernagel.android.example.sidekick;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SimpleAdapter adapter = new SimpleAdapter();
        adapter.addItem( "Widgets - TypefaceTextView", TypefaceTextViewActivity.class );
        adapter.addItem( "Widgets - AspectRatioImageView", AspectRatioImageViewActivity.class );
        adapter.addItem( "Widgets - CircularRevealView", RevealViewActivity.class );
        adapter.addItem( "Widgets - IndicatorView", IndicatorActivity.class );
        adapter.addItem( "Utils - Tooltip", TooltipActivity.class );
        adapter.addItem( "Utils - ColorFilterUtils", ColorFilterUtilsActivity.class );
        adapter.addItem( "Utils - Device and Network", DeviceActivity.class );
        adapter.addItem( "Utils - Drawable Tinting", TintingActivity.class );
        adapter.addItem( "Helper - Photos", PhotoActivity.class );
        adapter.addItem( "Resources - Colors", ColorsActivity.class );
        adapter.addItem( "Frameworks - ContentProviderProcessor", SimpleProviderActivity.class );

        final RecyclerView recyclerView = ( RecyclerView ) findViewById(R.id.recyclerView);
        if( recyclerView != null ) {
            recyclerView.setLayoutManager( new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false ) );
            recyclerView.setAdapter( adapter );
        }
    }

    public static class SimpleAdapter extends RecyclerView.Adapter<SimpleViewHolder> {
        private ArrayList<String> names = new ArrayList<>();
        private ArrayList<Class<?>> activities = new ArrayList<>();

        void addItem( String name, Class<?> activity ) {
            names.add( name );
            activities.add( activity );
            notifyDataSetChanged();
        }

        Class<?> getItem( int position ) {
            return activities.get( position );
        }

        @Override
        public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SimpleViewHolder(LayoutInflater.from( parent.getContext() ).inflate( R.layout.simple_item, parent, false ) );
        }

        @Override
        public void onBindViewHolder(SimpleViewHolder holder, int position) {
            final int adaptPos = holder.getAdapterPosition();
            holder.text.setText( names.get( position ) );
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(new Intent(v.getContext(), getItem(adaptPos)));
                }
            });
        }

        @Override
        public int getItemCount() {
            return names.size();
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
