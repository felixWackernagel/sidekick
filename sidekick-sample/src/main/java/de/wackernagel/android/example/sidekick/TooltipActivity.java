package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.wackernagel.android.sidekick.utils.TooltipUtils;

public class TooltipActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tooltip);

        TooltipUtils.createFor( findViewById(R.id.text) );

        TooltipUtils.createFor( findViewById(R.id.text1) );

        TooltipUtils.createFor( findViewById(R.id.image), "Tooltip from code");

        TooltipUtils.createFor( findViewById(R.id.button), R.string.tooltip_from_resource );
    }
}
