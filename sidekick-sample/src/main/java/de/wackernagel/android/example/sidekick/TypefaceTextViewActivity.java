package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import de.wackernagel.android.sidekick.utils.TypefaceUtils;

public class TypefaceTextViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typefacetextview);

        TextView text = ( TextView ) findViewById(R.id.text);
        text.setTypeface( TypefaceUtils.getRobotoThin( this ) );
    }
}
