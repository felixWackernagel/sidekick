package de.wackernagel.android.sidekick.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraHelper {
    private static final String TAG = "CameraHelper";

    private static final int REQUEST_CAPTURE_IMAGE = 82;

    private String currentMediaPath;
    private final String appName;

    /**
     * The helper handles camera launch and result handling.
     *
     * @param appName for media directory name
     */
    public CameraHelper( @NonNull final String appName ) {
        if( TextUtils.isEmpty( appName ) ) {
            throw new IllegalArgumentException( "The appName is empty but required as storage name!" );
        }
        this.appName = appName;
    }

    /**
     * @return path to the previous captured media or null.
     */
    public String getCurrentMediaPath() {
        return currentMediaPath;
    }

    /**
     * @param context to start media scanner
     * @return true if media was deleted otherwise false
     */
    public boolean deleteMedia(@NonNull final Context context) {
        if( currentMediaPath == null ) {
            return false;
        }

        final File media = new File(currentMediaPath);
        if( !media.delete() ) {
            media.deleteOnExit();
        }

        scanMediaForGallery(context, currentMediaPath);
        currentMediaPath = null;

        return !media.exists();
    }

    /**
     * Create a media file and launched the camera app.
     *
     * @param activity to start camera app
     */
    public void startCameraActivity( @NonNull final Activity activity) {
        File mediaFile = null;
        try {
            mediaFile = createMediaFile(".jpg");
            currentMediaPath = mediaFile.getAbsolutePath();
        } catch (Exception e) {
            Log.w( TAG, "startCameraActivity", e );
        }

        final Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        if( mediaFile != null ) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));
        }
        activity.startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
    }

    private File createMediaFile(@NonNull final String suffix) throws IOException {
        // Create a unique image file name
        final String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.getDefault() ).format( new Date() );
        final String imageFileName = "image" + "_" + timeStamp + suffix;

        final File directory = getPublicPictureDirectoryFor( appName );
        directory.mkdirs();
        if( directory.isDirectory() ) {
            File file = new File( directory, imageFileName);
            if( file.createNewFile() ) {
                return file;
            } else {
                throw new IOException( "Could not create image file: " + file.getPath() );
            }
        } else {
            throw new IOException( "Could not create image temp file dir: " + directory.getPath() );
        }
    }

    /**
     * @param context for media scanner
     * @param requestCode from launched camera app
     * @param resultCode from camera app
     * @param data from camera app
     */
    public void handleCameraResult(@NonNull final Context context, int requestCode, int resultCode, Intent data) {
        Log.d( TAG, "handleCameraResult(" + requestCode + ", " + resultCode + ", " + data + ")" );
        if( requestCode == REQUEST_CAPTURE_IMAGE ) {
            if( resultCode == Activity.RESULT_OK ) {
                Log.d(TAG, "> OK: add to gallery");
                scanMediaForGallery(context, currentMediaPath);
            } else {
                Log.d(TAG, "> CANCELED: delete previous created media file");
                deleteMedia(context);
            }
        }
    }

    /**
     * Display scaled version of the captured photo in the ImageView.
     *
     * @param imageView on which the photo is set
     */
    public void displayCapturedPhoto( final ImageView imageView ) {
        if( currentMediaPath == null || imageView == null ) {
            Log.d(TAG, "ImageView or media is NULL." );
            return;
        }

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Scale and display
        Bitmap bitmap = scaleBitmap(currentMediaPath, targetW, targetH);
        imageView.setImageBitmap(bitmap);
    }

    private static Bitmap scaleBitmap( @NonNull final String photoPath, final int targetWidth, final int targetHeight ) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( photoPath, bmOptions);
        int photoWidth = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min( photoWidth / targetWidth, photoHeight / targetHeight );

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        // Create scaled Bitmap
        return BitmapFactory.decodeFile( photoPath, bmOptions );
    }

    /**
     * @return directory in which camera photos are stored.
     */
    public File getAppPictureDirectory() {
        return getPublicPictureDirectoryFor( appName );
    }

    private static File getPublicPictureDirectoryFor( @NonNull final String appName ) {
        File pictureDirectory;
        if( Build.VERSION.SDK_INT >= 8 ) {
            pictureDirectory = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ), appName);
        } else {
            pictureDirectory = new File( Environment.getExternalStorageDirectory().getPath() + "/Pictures/" + appName );
        }
        if( isExternalStorageAccessible() ) {
            pictureDirectory.mkdirs();
        }
        return pictureDirectory;
    }

    /**
     * @return true if external storage mounted otherwise false
     */
    public static boolean isExternalStorageAccessible() {
        return Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() );
    }

    /**
     * Checks the number of cameras and the exists of a camera app.
     *
     * @param context to check feature and app
     * @return true if a all camera features available otherwise false
     */
    public static boolean hasCameraFeatures( @NonNull final Context context ) {
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final boolean hasPictureApp = takePictureIntent.resolveActivity( context.getPackageManager() ) != null;

        if( Build.VERSION.SDK_INT >= 9 ) {
            return hasPictureApp && Camera.getNumberOfCameras() > 0;
        } else {
            return hasPictureApp && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        }
    }

    /**
     * Starts the system media scanner to add or remove photos or videos to the gallery.
     *
     * @param context to start media scanner
     * @param mediaPath for media scanner
     */
    public static void scanMediaForGallery( @NonNull final Context context, @Nullable final String mediaPath ) {
        if( TextUtils.isEmpty( mediaPath ) ) {
            Log.d( TAG, "scanMediaForGallery(): mediaPath is empty." );
            return;
        }

        final Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE );
        final boolean hasMediaScanner = mediaScanIntent.resolveActivity( context.getPackageManager() ) != null;
        if( hasMediaScanner ) {
            Log.d( TAG, "scanMediaForGallery(): system has no media scanner." );
            return;
        }

        final File media = new File( mediaPath );
        final Uri contentUri = Uri.fromFile( media );
        mediaScanIntent.setData( contentUri );
        context.sendBroadcast(mediaScanIntent);
    }

}
