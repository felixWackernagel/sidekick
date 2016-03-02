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

    private String mCurrentPhotoPath;

    public boolean hasCamera( @NonNull final Context context ) {
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final boolean hasPictureApp = takePictureIntent.resolveActivity( context.getPackageManager() ) != null;

        if( Build.VERSION.SDK_INT >= 9 ) {
            return hasPictureApp && Camera.getNumberOfCameras() > 0;
        } else {
            return hasPictureApp && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        }
    }

    public boolean isExternalStorageAccessible() {
        return Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() );
    }

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }

    public boolean deletePhoto( final Activity activity ) {
        if( mCurrentPhotoPath == null ) {
            return false;
        }

        final File photo = new File( mCurrentPhotoPath );
        if( !photo.delete() ) {
            photo.deleteOnExit();
        }
        scanPhotoForGallery(activity, mCurrentPhotoPath);
        mCurrentPhotoPath = null;
        return !photo.exists();
    }

    public void startCapturePhotoActivity( @NonNull final Activity activity ) {
        final Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );

        File photoFile = null;
        try {
            photoFile = createPhotoFile( ".jpg" );
            mCurrentPhotoPath = photoFile.getAbsolutePath();
        } catch (Exception e) {
            Log.w( TAG, "startCapturePhotoActivity", e );
        }

        if( photoFile != null ) {
            intent.putExtra( MediaStore.EXTRA_OUTPUT, Uri.fromFile( photoFile ) );
        }

        activity.startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
    }

    private static File createPhotoFile( @NonNull final String suffix ) throws IOException {
        // Create a unique image file name
        final String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.getDefault() ).format( new Date() );
        final String imageFileName = "image" + "_" + timeStamp + suffix;

        final File directory = getPublicImageStorage();
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

    private static File getPublicImageStorage() {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ) {
            return new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ), "app");
        } else {
            return new File( Environment.getExternalStorageDirectory().getPath() + "/Pictures/app");
        }
    }

    public void handleResult( final Activity activity, int requestCode, int resultCode, Intent data ) {
        if( requestCode == REQUEST_CAPTURE_IMAGE ) {
            if( resultCode == Activity.RESULT_OK ) {
                scanPhotoForGallery( activity, mCurrentPhotoPath );
            } else {
                deletePhoto( activity );
            }
        }
    }

    public void displayCapturedPhoto( final ImageView imageView ) {
        if( mCurrentPhotoPath == null || imageView == null ) {
            return;
        }

        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min( photoW / targetW, photoH / targetH );

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    public static void scanPhotoForGallery( final Activity activity, final String photoPath ) {
        if( activity == null || TextUtils.isEmpty( photoPath ) ) {
            return;
        }

        final File photo = new File( photoPath );
        final Uri contentUri = Uri.fromFile( photo );
        final Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE );
        mediaScanIntent.setData( contentUri );
        activity.sendBroadcast( mediaScanIntent );
    }

}
