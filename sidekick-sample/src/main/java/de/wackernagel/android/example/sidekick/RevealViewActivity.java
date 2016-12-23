package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.wackernagel.android.sidekick.widgets.CircularRevealView;

public class RevealViewActivity extends AppCompatActivity {
    protected TextView textView;
    protected CircularRevealView revealView;
    protected Button toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reveal_view);

        textView = (TextView) findViewById(R.id.text);
        revealView = (CircularRevealView) findViewById(R.id.circularReveal);
        toggleButton = (Button) findViewById(R.id.button);

        revealView.setOnStateChangeListener(new CircularRevealView.OnStateChangeListener() {
            @Override
            public void onStateChange(int state) {
                switch( state ) {
                    case CircularRevealView.STATE_REVEAL_STARTED:
                        textView.setText( R.string.reveal_view_revealing );
                        break;
                    case CircularRevealView.STATE_REVEALED:
                        textView.setText(R.string.reveal_view_revealed);
                        break;
                    case CircularRevealView.STATE_CONCEAL_STARTED:
                        textView.setText(R.string.reveal_view_unrevealing);
                        break;
                    case CircularRevealView.STATE_CONCEALED:
                        textView.setText(R.string.reveal_view_unrevealed);
                        break;
                }
            }
        });

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] startLocation = new int[2];
                if ( revealView.isStateConcealed() ) {
                    revealView.getLocationInWindow(startLocation);
                    toggleButton.setText(R.string.reveal_view_unrevealed);
                    revealView.reveal(startLocation);
                } else {
                    toggleButton.setText(R.string.reveal_view_revealed);
                    startLocation[0] += revealView.getWidth();
                    startLocation[1] += revealView.getHeight();
                    revealView.conceal(startLocation);
                }
            }
        });

        findViewById(R.id.buttonRed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealView.setCircularColorResource(R.color.sidekick_text_error);
            }
        });

        findViewById(R.id.buttonGray).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealView.setCircularColorResource(R.color.sidekick_icon);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        textView.setText(revealView.isStateRevealed() ? R.string.reveal_view_revealed : R.string.reveal_view_unrevealed);
        toggleButton.setText(revealView.isStateConcealed() ?  R.string.reveal_reveal : R.string.reveal_conceal);
    }
}
