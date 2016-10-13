package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.wackernagel.android.sidekick.utils.TintUtils;

public class TintingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tinting);

        final ImageView image = ( ImageView ) findViewById(R.id.image);
        final TextView text = ( TextView ) findViewById(R.id.text);
        final Button reset = ( Button ) findViewById(R.id.reset);
        final View red = findViewById(R.id.red);
        final View black = findViewById(R.id.black);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TintUtils.clearTint( image.getDrawable() );
                TintUtils.clearTint( text );
            }
        });

        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = ContextCompat.getColor(v.getContext(), R.color.sidekick_text_error);
                TintUtils.tint( image.getDrawable(), color );
                TintUtils.tintCompoundDrawables( text, color );
            }
        });

        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = ContextCompat.getColor(v.getContext(), android.R.color.black);
                TintUtils.tint( image.getDrawable(), color );
                TintUtils.tintCompoundDrawables(text, color);
            }
        });
    }

}
