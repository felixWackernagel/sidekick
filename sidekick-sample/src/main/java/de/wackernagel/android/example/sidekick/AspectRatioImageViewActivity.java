package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import de.wackernagel.android.sidekick.widgets.AspectRatioImageView;

public class AspectRatioImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aspect_ratio_image);

        final AspectRatioImageView image = (AspectRatioImageView) findViewById(R.id.image);

        String[] display = new String[]{"width", "height"};
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                display);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        Spinner aspect = (Spinner) findViewById(R.id.spinner);
        aspect.setAdapter(dataAdapter);
        aspect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    image.setAspect(AspectRatioImageView.ASPECT_WIDTH);
                } else {
                    image.setAspect(AspectRatioImageView.ASPECT_HEIGHT);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        display = new String[]{"none", "16:9", "3:2", "4:3", "1:1", "3:4", "2:3"};
        dataAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                display);
        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        aspect = (Spinner) findViewById(R.id.spinner2);
        aspect.setAdapter(dataAdapter);
        aspect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        image.setRatio(AspectRatioImageView.RATIO_NONE);
                        break;
                    case 1:
                        image.setRatio(AspectRatioImageView.RATIO_16_9);
                        break;
                    case 2:
                        image.setRatio(AspectRatioImageView.RATIO_3_2);
                        break;
                    case 3:
                        image.setRatio(AspectRatioImageView.RATIO_4_3);
                        break;
                    case 4:
                        image.setRatio(AspectRatioImageView.RATIO_1_1);
                        break;
                    case 5:
                        image.setRatio(AspectRatioImageView.RATIO_3_4);
                        break;
                    case 6:
                        image.setRatio(AspectRatioImageView.RATIO_2_3);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
