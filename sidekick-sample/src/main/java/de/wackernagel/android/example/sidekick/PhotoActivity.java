package de.wackernagel.android.example.sidekick;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.wackernagel.android.sidekick.frameworks.media.MediaConfig;
import de.wackernagel.android.sidekick.frameworks.media.MediaResult;
import de.wackernagel.android.sidekick.frameworks.media.MediaUtils;

public class PhotoActivity extends AppCompatActivity {
    static MediaConfig config = new MediaConfig( "Sidekick-Showcase" );

    private TextView text;
    private ImageView image;
    private Button takePhoto;
    private Button pickPhoto;
    Button deletePhoto;

    MediaResult lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        text = ( TextView ) findViewById(R.id.text);
        image = ( ImageView ) findViewById( R.id.image );
        takePhoto = ( Button ) findViewById(R.id.button);
        pickPhoto = ( Button ) findViewById(R.id.button2);
        deletePhoto = ( Button ) findViewById(R.id.button3);

        checkEnabled();

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaUtils.startCameraForResult(PhotoActivity.this, config);
            }
        });

        pickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaUtils.startImagePickerForResult(PhotoActivity.this);
            }
        });

        deletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( lastResult != null ) {
                    MediaUtils.deleteMediaFromPath(v.getContext(), lastResult.getMediaPath());
                    deletePhoto.setEnabled(false);
                    clear();
                }
            }
        });

        if( savedInstanceState != null && savedInstanceState.containsKey( "lastImage" ) ) {
            setMedia(( MediaResult ) savedInstanceState.getParcelable( "lastImage" ));
        }
    }

    void clear() {
        image.setImageBitmap( null );
        lastResult = null;
        text.setText("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if( lastResult != null ) {
            outState.putParcelable( "lastImage", lastResult );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MediaResult result = MediaUtils.onActivityResult(this, config, requestCode, resultCode, data);
        if( result != null ) {
            // MediaUtils processed the intent
            if( result.isCanceled() ) {
                // user canceled requested operation
                clear();
                text.setText(R.string.photo_canceled);
            } else if( result.isFailed() ) {
                // result processing failed
                clear();
                text.setText(R.string.photo_failed);
            } else {
                setMedia( result );
            }
        }
    }

    private void setMedia( MediaResult result ) {
        text.setText( getString(R.string.photo_path, result.getMediaPath()) );
        lastResult = result;
        deletePhoto.setEnabled( true );
        String filePath = result.getMediaPath();
        if( !TextUtils.isEmpty( filePath ) ) {
            image.setImageBitmap(MediaUtils.getScaledBitmap(filePath, 500, 500));
        }
    }

    private void checkEnabled() {
        final boolean isEnabled = MediaUtils.isExternalStorageAccessible() && MediaUtils.isCameraAvailable(this);
        takePhoto.setEnabled( isEnabled );
        pickPhoto.setEnabled( isEnabled );
        deletePhoto.setEnabled( isEnabled );
        text.setText( getString( R.string.photo_enabled, isEnabled ) );
    }
}
