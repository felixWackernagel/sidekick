package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import de.wackernagel.android.sidekick.utils.ColorFilterUtils;

public class ColorFilterUtilsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private ImageView image;
    private SeekBar brightnessView;
    private SeekBar contrastView;
    private SeekBar saturationView;
    private SeekBar hueView;

    private int brightness = 0;
    private int contrast = 0;
    private int saturation = 0;
    private int hue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorfilterutils);

        image = ( ImageView ) findViewById(R.id.image);

        brightnessView = ( SeekBar ) findViewById(R.id.brightness);
        brightnessView.setOnSeekBarChangeListener(this);
        brightnessView.setMax(200);
        brightnessView.setProgress(100);

        contrastView = ( SeekBar ) findViewById(R.id.contrast);
        contrastView.setOnSeekBarChangeListener(this);
        contrastView.setMax(200);
        contrastView.setProgress(100);

        saturationView = ( SeekBar ) findViewById(R.id.saturation);
        saturationView.setOnSeekBarChangeListener(this);
        saturationView.setMax(200);
        saturationView.setProgress(100);

        hueView = ( SeekBar ) findViewById(R.id.hue);
        hueView.setOnSeekBarChangeListener(this);
        hueView.setMax(360);
        hueView.setProgress(180);
    }

    public void onResetClicked(View v) {
        brightnessView.setProgress(100);
        contrastView.setProgress(100);
        saturationView.setProgress(100);
        hueView.setProgress(180);
        brightness = contrast = saturation = hue = 0;
        image.setColorFilter( ColorFilterUtils.adjustColor( brightness, contrast, saturation, hue ) );
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch( seekBar.getId() ) {
            case R.id.brightness:
                brightness = progress - ( seekBar.getMax() / 2 );
                break;

            case R.id.contrast:
                contrast = progress - ( seekBar.getMax() / 2 );
                break;

            case R.id.saturation:
                saturation = progress - ( seekBar.getMax() / 2 );
                break;

            case R.id.hue:
                hue = progress - ( seekBar.getMax() / 2 );
                break;

            default:
                break;
        }

        image.setColorFilter( ColorFilterUtils.adjustColor( brightness, contrast, saturation, hue ) );
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
