package de.wackernagel.android.sidekick.frameworks.media;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaUtils {
    private static final String TAG = "MediaUtils";

    public static final int REQUEST_CAMERA_IMAGE = 456;
    public static final int REQUEST_PICK_IMAGE = 457;

    private MediaUtils() {
    }

    @TargetApi(11)
    public static void startCameraForResult(@NonNull final android.app.Fragment fragment, @NonNull final MediaConfig config ) {
        fragment.startActivityForResult( createImageCameraIntent( fragment.getActivity(), config ), REQUEST_CAMERA_IMAGE );
    }

    public static void startCameraForResult(@NonNull final Fragment fragment, @NonNull final MediaConfig config) {
        fragment.startActivityForResult( createImageCameraIntent( fragment.getActivity(), config ), REQUEST_CAMERA_IMAGE );
    }

    public static void startCameraForResult(@NonNull final Activity activity, @NonNull final MediaConfig config) {
        activity.startActivityForResult(createImageCameraIntent(activity, config), REQUEST_CAMERA_IMAGE);
    }

    private static Intent createImageCameraIntent( @NonNull final Context context, @NonNull final MediaConfig config ) {
        final Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
        final File mediaFile = createMediaFile( getFileExtension( config.getCompressFormat() ), config.getApplicationName() );
        if( mediaFile != null ) {
            final String mediaPath = mediaFile.getAbsolutePath();
            Uri mediaUri = Uri.fromFile( mediaFile );

            final ContentValues values = new ContentValues();
            values.put( MediaStore.Images.Media.TITLE, mediaUri.getLastPathSegment() );
            values.put( MediaStore.Images.Media.DATA, mediaPath );
            values.put( MediaStore.Images.Media.BUCKET_DISPLAY_NAME, config.getApplicationName() );

            mediaUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values );

            intent.putExtra( MediaStore.EXTRA_OUTPUT, mediaUri );
        }
        return intent;
    }

    @TargetApi(11)
    public static void startImagePickerForResult( @NonNull final android.app.Fragment fragment) {
        fragment.startActivityForResult(createImagePickerIntent(), REQUEST_PICK_IMAGE);
    }

    public static void startImagePickerForResult( @NonNull final Fragment fragment) {
        fragment.startActivityForResult(createImagePickerIntent(), REQUEST_PICK_IMAGE);
    }

    public static void startImagePickerForResult( @NonNull final Activity activity) {
        activity.startActivityForResult(createImagePickerIntent(), REQUEST_PICK_IMAGE);
    }

    private static Intent createImagePickerIntent() {
        final Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
        intent.setType("image/*");
        return intent;
    }

    @Nullable
    private static File createMediaFile( @NonNull final String fileExtension, @NonNull final String subDirectory ) {
        final File directory = getExternalStoragePictureDirectory( subDirectory );
        if( directory != null ) {

            final String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss", Locale.getDefault() ).format( new Date() );
            final String imageFileName = "image" + "_" + timeStamp + fileExtension;

            final File file = new File( directory, imageFileName);
            try {
                if( file.createNewFile() ) {
                    return file;
                } else {
                    Log.d( TAG, file.getName() + " already exist." );
                    return file;
                }
            } catch( IOException e ) {
                Log.e( TAG, "File can't be created.", e );
            }
        }
        return null;
    }

    /**
     * @param context for media scanner
     * @param requestCode from launched camera app
     * @param resultCode from camera app
     * @param data from camera app
     * @return {@link MediaResult} which contain the requested media state or null if the result doesn't belong to the {@link MediaUtils}
     */
    @Nullable
    public static MediaResult onActivityResult( @NonNull final Context context, @NonNull final MediaConfig config, final int requestCode, int resultCode, final Intent data ) {
        switch( requestCode ) {
            case REQUEST_CAMERA_IMAGE:
                return processCameraResult( context, config, resultCode );

            case REQUEST_PICK_IMAGE:
                return processImagePickerResult( context, config, resultCode, data );

            default:
                return null;
        }
    }

    @NonNull
    private static MediaResult processImagePickerResult(@NonNull final Context context, @NonNull final MediaConfig config, final int resultCode, final Intent data ) {
        final MediaResult.Builder builder = new MediaResult.Builder();
        if( resultCode == Activity.RESULT_OK ) {
            builder.setCanceled( false );

            final Uri mediaUri = data.getData();
            final Bitmap bitmap = getBitmap( context, mediaUri );
            if( bitmap != null ) {

                File mediaFile = createMediaFile( getFileExtension( config.getCompressFormat() ), config.getApplicationName());
                mediaFile = writeBitmapToFile(bitmap, mediaFile, config.getCompressFormat(), config.getCompressQuality() );
                bitmap.recycle();

                if( mediaFile != null ) {
                    final String mediaPath = mediaFile.getAbsolutePath();
                    builder.setMediaPath( mediaPath );
                    startMediaScanner( context, mediaPath );
                } else {
                    builder.setFailed( true );
                }
            } else {
                builder.setFailed(true);
            }
        } else {
            builder.setCanceled( true );
        }
        return builder.build();
    }

    @Nullable
    private static Bitmap getBitmap( Context context, Uri mediaUri ) {
        try {
            final InputStream mediaStream = context.getContentResolver().openInputStream( mediaUri );
            return BitmapFactory.decodeStream( mediaStream );
        } catch( FileNotFoundException e ) {
            Log.i( TAG, "No media found for Uri!", e );
            return null;
        }
    }

    @NonNull
    private static MediaResult processCameraResult(@NonNull final Context context, @NonNull final MediaConfig config, final int resultCode ) {
        final MediaResult.Builder builder = new MediaResult.Builder();
        if( resultCode == Activity.RESULT_OK ) {
            builder.setCanceled(false);

            final String mediaPath = queryLatestMediaPath(context, config.getApplicationName());
            if( mediaPath != null ) {
                startMediaScanner( context, mediaPath );
                builder.setMediaPath( mediaPath );
            } else {
                builder.setFailed( true );
            }
        } else {
            builder.setCanceled(true);

            final String mediaPath = queryLatestMediaPath(context, config.getApplicationName());
            if( mediaPath != null ) {
                deleteMediaFromPath( context, mediaPath );
            }
        }
        return builder.build();
    }

    @Nullable
    private static File writeBitmapToFile(@NonNull final Bitmap bitmap, @Nullable final File mediaFile, @NonNull final Bitmap.CompressFormat compressFormat, @IntRange( from = 0, to = 100 ) int quality) {
        try {
            if ( mediaFile != null ) {
                final FileOutputStream fos = new FileOutputStream( mediaFile );
                bitmap.compress( compressFormat, quality, fos );
                fos.close();
                return mediaFile;
            }
            return null;
        } catch (IOException e) {
            Log.e( TAG, "Unable to create a compressed bitmap version!", e );
            return null;
        }
    }

    /**
     * @param compressFormat to identify the file extension
     * @return file extension of the given compressFormat with leading '.'.
     */
    @NonNull
    private static String getFileExtension( Bitmap.CompressFormat compressFormat ) {
        if( Build.VERSION.SDK_INT >= 14 ) {
            switch( compressFormat ) {
                case WEBP:
                    return ".webp";

                case PNG:
                    return ".png";

                case JPEG:
                default:
                    return ".jpg";
            }
        } else {
            switch( compressFormat ) {
                case PNG:
                    return ".png";

                case JPEG:
                default:
                    return ".jpg";
            }
        }
    }

    public static Bitmap getScaledBitmap( @NonNull final Bitmap bitmap, final int targetWidth, final int targetHeight ) {
        int mediaWidth = bitmap.getWidth();
        int mediaHeight = bitmap.getHeight();

        Log.i(TAG, "Scale bitmap from " + mediaWidth + "x" + mediaHeight + " to " + targetWidth + "x" + targetHeight);

        float scaleFactorWidth = ( (float) targetWidth) / mediaWidth;
        float scaleFactorHeight = ( (float) targetHeight) / mediaHeight;

        final Matrix matrix = new Matrix();
        matrix.postScale( scaleFactorWidth, scaleFactorHeight );

        if (!bitmap.isMutable() && mediaWidth == bitmap.getWidth() && mediaHeight == bitmap.getHeight() && matrix.isIdentity()) {
            Log.e( TAG, "No scaling required because bitmap is identical!" );
            return bitmap;
        }

        final Bitmap resizedBitmap = Bitmap.createBitmap( bitmap, 0, 0, mediaWidth, mediaHeight, matrix, false );
        bitmap.recycle();

        return resizedBitmap;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static Bitmap getScaledBitmap(@NonNull final String mediaPath, final int targetWidth, final int targetHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(mediaPath, options);
        int mediaWidth = options.outWidth;
        int mediaHeight = options.outHeight;

        Log.i( TAG, "Scale bitmap from " + mediaWidth + "x" + mediaHeight + " to " + targetWidth + "x" + targetHeight );

        int scaleFactor = Math.min( mediaWidth / targetWidth, mediaHeight / targetHeight );
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        return BitmapFactory.decodeFile(mediaPath, options);
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static Bitmap getScaledBitmap(@NonNull final InputStream mediaStream, final int targetWidth, final int targetHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(mediaStream, null, options);
        int mediaWidth = options.outWidth;
        int mediaHeight = options.outHeight;

        Log.i( TAG, "Scale bitmap from " + mediaWidth + "x" + mediaHeight + " to " + targetWidth + "x" + targetHeight );

        int scaleFactor = Math.min( mediaWidth / targetWidth, mediaHeight / targetHeight );
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        return BitmapFactory.decodeStream(mediaStream, null, options);
    }

    /**
     * @param context to start media scanner
     * @param mediaPath for deletion
     * @return true if media is or was deleted otherwise false
     */
    public static boolean deleteMediaFromPath( @NonNull final Context context, @Nullable final String mediaPath ) {
        if( TextUtils.isEmpty( mediaPath ) ) {
            Log.e( TAG, "Path to media is empty or null!" );
            return false;
        }

        final File mediaFile = new File( mediaPath );
        if( mediaFile.exists() ) {
            if( mediaFile.delete() ) {
                int deleteCount =  context.getContentResolver().delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + "=?",
                    new String[] {
                        mediaPath
                    }
                );
                Log.i( TAG, "Delete " + deleteCount + " entries of MediaStore for media path " + mediaPath );
            }
        }

        if( queryMediaStoreCount(context, mediaPath) > 0 ) {
            startMediaScanner(context, mediaPath);
        }

        return !mediaFile.exists();
    }

    /**
     * @param subDirectory for organization
     * @return directory file or null if storage not accessible or directory not created
     */
    @Nullable
    private static File getExternalStoragePictureDirectory(@NonNull final String subDirectory) {
        if( isExternalStorageAccessible() ) {
            File pictureDirectory;
            if( Build.VERSION.SDK_INT >= 8 ) {
                pictureDirectory = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ), subDirectory);
            } else {
                pictureDirectory = new File( new File( Environment.getExternalStorageDirectory().getAbsolutePath(), "Pictures" ), subDirectory );
            }

            if( pictureDirectory.exists() ) {
                return pictureDirectory;
            } else if( pictureDirectory.mkdirs() || pictureDirectory.isDirectory() ) {
                return pictureDirectory;
            } else {
                Log.e( TAG, "Directory can't be created!" );
                return null;
            }
        }
        return null;
    }

    /**
     * @return true if external storage mounted otherwise false
     */
    public static boolean isExternalStorageAccessible() {
        return Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() );
    }

    /**
     * Checks the number of cameras and the existence of a camera app.
     *
     * @param context to check feature and applications
     * @return true if a camera exists
     */
    @SuppressWarnings("deprecation")
    public static boolean isCameraAvailable(@NonNull final Context context) {
        final boolean existCamera = context.getPackageManager().hasSystemFeature( PackageManager.FEATURE_CAMERA );

        final Intent cameraApplication = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final boolean existCameraApplication = cameraApplication.resolveActivity( context.getPackageManager() ) != null;

        int cameraCount = 1;
        if( Build.VERSION.SDK_INT >= 9 ) {
            cameraCount = Camera.getNumberOfCameras();
        }

        return existCamera && existCameraApplication && cameraCount > 0;
    }

    /**
     * Starts the system media scanner to add or remove photos and videos to the media store.
     *
     * @param context to start media scanner
     * @param mediaPath for media scanner
     */
    private static void startMediaScanner(@NonNull final Context context, @Nullable final String mediaPath) {
        if( TextUtils.isEmpty( mediaPath ) ) {
            Log.e( TAG, "No media path for media scanner!" );
            return;
        }

        final File mediaFile = new File( mediaPath );
        final Uri mediaUri = Uri.fromFile( mediaFile );
        final Intent mediaScanner = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE );
        mediaScanner.setData( mediaUri );
        context.sendBroadcast( mediaScanner );
    }

    /**
     * @param context to access media store provider
     * @param mediaPath to find entries with exact this media path
     * @return count of media store entries for the given media path or 0 for empty media path or error
     */
    private static int queryMediaStoreCount(@NonNull final Context context, @Nullable final String mediaPath) {
        if( TextUtils.isEmpty( mediaPath ) ) {
            Log.e( TAG, "No media path for media store entry!" );
            return 0;
        }

        final Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.DATA},
                MediaStore.Images.Media.DATA + "=?",
                new String[]{ mediaPath },
                null );

        if( cursor != null ) {
            int count = cursor.getCount();
            cursor.close();
            return count;
        }
        return 0;
    }

    /**
     * This method return the latest DATA field of a media store entry which contain the given subDirectory in its DATA value.
     *
     * @param context to access media store provider
     * @param subDirectory which is part of the media path
     * @return path to a media file in the given subDirectory or null
     */
    @Nullable
    private static String queryLatestMediaPath( @NonNull final Context context, @NonNull final String subDirectory ) {
        final Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{ MediaStore.Images.Media.DATA },
                MediaStore.Images.Media.DATA + " LIKE ?",
                new String[]{"%/" + subDirectory + "/%"},
                MediaStore.Images.Media._ID + " DESC");

        String mediaPath = null;
        if( cursor != null && cursor.moveToFirst() ) {
            mediaPath = cursor.getString( 0 );
        }

        if( cursor != null && !cursor.isClosed() ) {
            cursor.close();
        }
        return mediaPath;
    }
}