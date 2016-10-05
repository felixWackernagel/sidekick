package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import de.wackernagel.android.sidekick.widgets.IndicatorView;

public class IndicatorActivity extends AppCompatActivity {

    private boolean circle = true;
    private boolean animate = false;
    private static final long DURATION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indicator_view);

        final IndicatorView indicatorView = ( IndicatorView ) findViewById(R.id.indicator);
        final Button back = ( Button ) findViewById(R.id.button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicatorView.setSelectedIndex(
                        Math.max(0, indicatorView.getSelectedIndex() - 1),
                        animate ? DURATION : 0l );
            }
        });
        final Button next = ( Button ) findViewById(R.id.button2);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicatorView.setSelectedIndex(
                        Math.min(indicatorView.getCount() - 1, indicatorView.getSelectedIndex() + 1),
                        animate ? DURATION : 0l );
            }
        });

        final Button animation = ( Button ) findViewById(R.id.button3);
        animation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animate = !animate;
                animation.setText(animate ? "Disable animation" : "Enable animation");
            }
        });

        final Button increaseCount = ( Button ) findViewById(R.id.button4);
        increaseCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicatorView.setCount(indicatorView.getCount() + 1);
            }
        });

        final Button decreaseCount = ( Button ) findViewById(R.id.button5);
        decreaseCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                indicatorView.setCount( Math.max( indicatorView.getCount() - 1, 1 ) );
            }
        });

        final Button toggleIndicator = ( Button ) findViewById(R.id.button6);
        toggleIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circle = !circle;

                if( circle ) {
                    indicatorView.setSelectedDrawable( R.drawable.circle_selected );
                    indicatorView.setUnselectedDrawable( R.drawable.circle);
                } else {
                    indicatorView.setSelectedDrawable( R.drawable.square_selected );
                    indicatorView.setUnselectedDrawable( R.drawable.square );
                }
            }
        });
    }
}
