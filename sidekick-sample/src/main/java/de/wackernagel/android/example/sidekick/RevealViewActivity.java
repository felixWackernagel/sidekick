package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.wackernagel.android.sidekick.widgets.CircularRevealView;

public class RevealViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reveal_view);

        final TextView text = (TextView) findViewById(R.id.text);

        final CircularRevealView revealViw = (CircularRevealView) findViewById(R.id.circularReveal);
        revealViw.setOnStateChangeListener(new CircularRevealView.OnStateChangeListener() {
            @Override
            public void onStateChange(int state) {
                switch( state ) {
                    case CircularRevealView.STATE_REVEAL_STARTED:
                        text.setText( R.string.reveal_view_revealing );
                        break;
                    case CircularRevealView.STATE_REVEALED:
                        text.setText(R.string.reveal_view_revealed);
                        break;
                    case CircularRevealView.STATE_CONCEAL_STARTED:
                        text.setText(R.string.reveal_view_unrevealing);
                        break;
                    case CircularRevealView.STATE_CONCEALED:
                        text.setText(R.string.reveal_view_unrevealed);
                        break;
                }
            }
        });

        text.setText( revealViw.getState() == CircularRevealView.STATE_REVEALED ? "revealed" : "concealed");

        final Button visibility = (Button) findViewById(R.id.button);
        visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] startLocation = new int[2];
                revealViw.getLocationInWindow(startLocation);
                if (revealViw.getState() == CircularRevealView.STATE_CONCEALED ) {
                    visibility.setText(R.string.reveal_view_unrevealed);
                    revealViw.reveal(startLocation);
                } else {
                    visibility.setText(R.string.reveal_view_revealed);
                    startLocation[0] += revealViw.getWidth();
                    startLocation[1] += revealViw.getHeight();
                    revealViw.conceal(startLocation);
                }
            }
        });

        final Button red= (Button) findViewById(R.id.buttonRed);
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealViw.setCircularColorResource(R.color.sidekick_text_error);
            }
        });

        final Button gray= (Button) findViewById(R.id.buttonGray);
        gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealViw.setCircularColorResource(R.color.sidekick_icon);
            }
        });
    }
}
