package de.wackernagel.android.example.sidekick;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.wackernagel.android.sidekick.frameworks.media.MediaConfig;
import de.wackernagel.android.sidekick.frameworks.media.MediaResult;
import de.wackernagel.android.sidekick.frameworks.media.MediaUtils;

public class PhotoActivity extends AppCompatActivity {
    static MediaConfig config = MediaConfig.builder( "Sidekick-Showcase" ).build();

    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_IMAGE_PICKER = 2;
    private static final int REQUEST_CODE_PERMISSION = 3;

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
                checkCameraWrapper();
            }
        });

        pickPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaUtils.startImagePickerForResult(PhotoActivity.this, REQUEST_CODE_IMAGE_PICKER);
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

    void checkCameraWrapper() {
        final List<String> permissions = new ArrayList<>();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if( permissions.size() > 0 ) {
            ActivityCompat.requestPermissions( this, permissions.toArray( new String[permissions.size()] ), REQUEST_CODE_PERMISSION);
        } else {
            checkCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch( requestCode ) {
            case REQUEST_CODE_PERMISSION:

                final Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if( perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted
                    checkCamera();
                } else {
                    // permission denied
                    if( (ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.CAMERA ) && perms.get(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                            || (ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) ){
                        // retry permission dialog
                        showMessageOKCancel("Please allow all request to use your camera and store them on your device.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final List<String> permissions = new ArrayList<>();
                                        if(perms.get(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.shouldShowRequestPermissionRationale( PhotoActivity.this, Manifest.permission.CAMERA ) ) {
                                            permissions.add(Manifest.permission.CAMERA);
                                        }
                                        if(perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.shouldShowRequestPermissionRationale( PhotoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE )) {
                                            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                        }
                                        ActivityCompat.requestPermissions( PhotoActivity.this, permissions.toArray(new String[permissions.size()]), REQUEST_CODE_PERMISSION);
                                    }
                                });
                    } else {
                        // never ask again and denied
                        showMessageOKCancel("You denied one or more required requests and say never ask again. Sorry dude. Open your settings to change this.", null);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    private void checkCamera() {
        MediaUtils.startCameraForResult(PhotoActivity.this, config, REQUEST_CODE_CAMERA);
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
        MediaResult result = MediaUtils.onCameraResult(this, config, requestCode, resultCode, data, REQUEST_CODE_CAMERA);
        if( result == null ) {
            result = MediaUtils.onImagePickerResult(this, config, requestCode, resultCode, data, REQUEST_CODE_IMAGE_PICKER);
        }
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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
