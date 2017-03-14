package de.wackernagel.android.example.sidekick;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.wackernagel.android.sidekick.helper.GridGutterDecoration;
import de.wackernagel.android.sidekick.utils.DeviceUtils;

public class GridGutterDecorationActivity extends AppCompatActivity {

    protected RecyclerView recyclerView;
    protected RecyclerView.ItemDecoration decoration;
    protected GridLayoutManager layoutManager;

    protected boolean topAndBottomPadding = false;
    protected boolean leftAndRightPadding = false;
    protected int paddingInDP = 0;
    protected int spanCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_gutter_decoration);

        final GridGutterDecorationActivity.SimpleAdapter adapter = new GridGutterDecorationActivity.SimpleAdapter();
        adapter.addItem( "#F44336" );
        adapter.addItem( "#FF4081" );
        adapter.addItem( "#9C27B0" );
        adapter.addItem( "#7C4DFF" );
        adapter.addItem( "#3F51B5" );
        adapter.addItem( "#4484FF" );
        adapter.addItem( "#03a9f4" );
        adapter.addItem( "#00bcd4" );
        adapter.addItem( "#009688" );
        adapter.addItem( "#4caf50" );
        adapter.addItem( "#8bc34a" );
        adapter.addItem( "#cddc39" );
        adapter.addItem( "#ffeb3b" );
        adapter.addItem( "#ffc107" );
        adapter.addItem( "#ff9800" );
        adapter.addItem( "#ff5722" );
        adapter.addItem( "#795548" );
        adapter.addItem( "#9e9e9e" );
        adapter.addItem( "#607d8b" );

        recyclerView = ( RecyclerView ) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( layoutManager = new GridLayoutManager(this, spanCount));
        recyclerView.setAdapter( adapter );

        setupLeftAndRightPadding();
        setupTopAndBottomPadding();
        setupPadding();
        setupColumnCount();
    }

    private void setupLeftAndRightPadding() {
        final CheckBox checkBox = (CheckBox ) findViewById(R.id.leftAndRight);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                leftAndRightPadding = isChecked;
                updateDecorator();
            }
        });
    }

    private void setupTopAndBottomPadding() {
        final CheckBox checkBox = (CheckBox ) findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                topAndBottomPadding = isChecked;
                updateDecorator();
            }
        });
    }

    private void setupPadding() {
        final String[] paddingValues = new String[]{"0", "4", "8", "16"};
        final ArrayAdapter<String> paddingAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                paddingValues);
        paddingAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner paddingSpinner = (Spinner) findViewById(R.id.spinner);
        paddingSpinner.setAdapter(paddingAdapter);
        paddingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                paddingInDP = Integer.valueOf( paddingValues[position] );
                updateDecorator();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupColumnCount() {
        final String[] columnValues = new String[]{"1", "2", "3", "4", "5", "6"};
        final ArrayAdapter<String> columnAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                columnValues);
        columnAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        final Spinner columnsSpinner = (Spinner) findViewById(R.id.columns);
        columnsSpinner.setAdapter(columnAdapter);
        columnsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spanCount = Integer.valueOf( columnValues[position] );
                updateDecorator();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    protected void updateDecorator() {
        if( decoration != null )
            recyclerView.removeItemDecoration(decoration);
        recyclerView.addItemDecoration(decoration = new GridGutterDecoration(DeviceUtils.dpToPx(paddingInDP, this), spanCount, topAndBottomPadding, leftAndRightPadding) );
        layoutManager.setSpanCount(spanCount);
    }

    public static class SimpleAdapter extends RecyclerView.Adapter<GridGutterDecorationActivity.SimpleViewHolder> {
        private ArrayList<String> items = new ArrayList<>();

        void addItem( String item ) {
            this.items.add( item );
            notifyItemRangeInserted( 0, items.size() );
        }

        @Override
        public GridGutterDecorationActivity.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GridGutterDecorationActivity.SimpleViewHolder(LayoutInflater.from( parent.getContext() ).inflate( R.layout.color_item, parent, false ) );
        }

        @Override
        public void onBindViewHolder(GridGutterDecorationActivity.SimpleViewHolder holder, int position) {
            final String color = items.get( position );
            holder.text.setText( color );
            holder.text.setBackgroundColor(Color.parseColor( color ));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText( v.getContext(), "Clicked " + color, Toast.LENGTH_SHORT ).show();
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
